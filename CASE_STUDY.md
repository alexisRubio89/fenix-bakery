# Fenix Bakery — Full-Stack Web Application (Case Study)

**Role:** Sole developer (design, backend, frontend, cloud infrastructure, deployment)
**Type:** Production web application for a real business (Cuban bakery, 3 locations in New Jersey)
**Live stack:** Java · Spring Boot · Thymeleaf · MySQL · AWS (Elastic Beanstalk, RDS, S3, IAM)

---

## Summary

Fenix Bakery is a bilingual (Spanish/English), production-deployed website and admin platform I built end-to-end for a family-owned Cuban bakery with three locations. It started as a static informational site and grew into a full content-management system where the owner manages the entire product catalog — menu items, cakes, and party trays — including photo and video uploads, without touching code.

The project is deployed on AWS and serves real customers. Beyond the features, what this project demonstrates is the ability to take a real requirement, choose an appropriate architecture, and solve the messy production problems that come with running software on cloud infrastructure — IAM permissions, ephemeral storage, reverse-proxy limits, and credential management.

---

## The problem

The bakery needed an online presence that:

- Showcased three separate product catalogs (full menu, cakes, party menu) in both Spanish and English.
- Let the owner — a non-developer — add, edit, and remove products and their images without asking a developer every time.
- Handled customer inquiries through a contact form that reached the business reliably.
- Looked premium (the brand is positioned as high-end) and worked well on mobile.

The core challenge was building a **self-service admin system** robust enough for a non-technical owner, backed by cloud infrastructure that could store growing amounts of media reliably.

---

## Architecture

**Application layer**
- **Spring Boot** (Java 17) serving server-rendered **Thymeleaf** templates.
- **Spring Security** for the admin area, with BCrypt-hashed credentials and role-based access.
- **Spring i18n** (message bundles) for full Spanish/English localization across every page.

**Data layer**
- **MySQL on AWS RDS** as the primary database.
- JPA/Hibernate entities for the three catalogs (menu items, cakes, party items), contact messages, and inventory.
- Database seeding on first boot so the environment is reproducible from empty.

**Media storage**
- **AWS S3** for all product photos and videos, served publicly via bucket policy.
- Uploaded files are stored in S3 (not on the server), with only the resulting URLs persisted in the database.

**Infrastructure**
- **AWS Elastic Beanstalk** (single-instance) running the packaged JAR behind an nginx reverse proxy.
- **AWS IAM** user with a least-privilege policy scoped to the specific S3 bucket.
- All secrets (database, mail, admin credentials, AWS keys) injected via **environment variables**, never hard-coded in the deployed artifact.

---

## Key features

- **Three fully editable catalogs** — menu (65+ items across 9 categories), cakes (with multi-image galleries), and party trays — each with full create/read/update/delete from the admin panel.
- **Image and video upload** — the owner uploads media directly from the admin; files go to S3, old files are cleaned up automatically on replacement or deletion.
- **Editable multi-image galleries** for cakes, where individual photos can be removed and new ones added without disturbing the rest.
- **Short looping video support** — products can show a silent auto-looping video (Instagram-style) instead of a static photo, with the photo as an automatic fallback.
- **Contact messaging system** — customer messages are stored in the database *and* emailed to the business, with a profanity filter, an accordion-style inbox in the admin, unread badges, and bulk actions (mark read, delete selected, delete all) with an elegant confirmation modal.
- **Bilingual everything** — every product field and UI string exists in Spanish and English.
- **SEO** — dynamic meta tags, Open Graph, sitemap, robots.txt, and Schema.org structured data for all three locations.

---

## Engineering challenges (and how I solved them)

This is the part I'm most proud of, because these are the problems that don't show up in tutorials.

### 1. Ephemeral storage vs. persistent media
**Problem:** My first instinct was to save uploaded images to the server's local disk. But Elastic Beanstalk instances are ephemeral — every redeploy or instance replacement wipes local files. Product photos would silently disappear.
**Solution:** I moved all media to **S3**, storing only the public URLs in the database. Media now survives redeploys, scales independently, and is served directly from S3. This also cleanly separated the concern of "where files live" from "what the app knows about them."

### 2. IAM permissions (403 on upload)
**Problem:** After wiring up S3, uploads failed with `AccessDenied: not authorized to perform s3:PutObject`. The IAM user could read but not write.
**Solution:** I wrote a **least-privilege inline IAM policy** granting only `s3:PutObject` and `s3:DeleteObject` on the specific bucket ARN — not full S3 access. This fixed uploads while keeping the security surface minimal.

### 3. nginx upload limit (413 Request Entity Too Large)
**Problem:** Uploading a ~10 MB video to production failed with a 413 error from nginx — even though Spring's own multipart limit was raised to 20 MB. Elastic Beanstalk puts an nginx reverse proxy in front of the app, and *its* default body limit (1 MB) was rejecting the request before it ever reached the application.
**What I learned:** In Elastic Beanstalk on Amazon Linux 2023, nginx config layers matter — a directive at the `http` level can be overridden by the `server` block that the platform generates. I worked through platform hooks, config precedence, and a deployment packaging issue (a ZIP built on Windows used backslash path separators, which Linux couldn't interpret). This was a deep, multi-layer debugging exercise across the application, the proxy, and the deployment pipeline.
**Pragmatic outcome:** To unblock the business quickly, I also added a direct-to-S3 upload path (upload the file to S3 in the console, paste the URL into the admin), decoupling large media uploads from the proxy entirely — a pattern used widely in production systems for large files.

### 4. Credential management
**Problem:** Early on, secrets lived in the properties file. That's fine locally but dangerous for a deployed/version-controlled artifact.
**Solution:** I moved every secret — database password, Gmail app password, admin credentials, AWS keys — to **environment variables**, using Spring's `${VAR:default}` syntax so the app runs locally with defaults but reads real secrets from the Elastic Beanstalk environment in production. I also documented credential rotation as part of the deployment process.

### 5. Local/production data parity
**Problem:** Media uploaded while developing locally wrote URLs into the *local* database, so it wouldn't appear in production (which reads from RDS) — even though the files were in the shared S3 bucket.
**Solution:** I understood and documented the distinction: files live in shared S3, but the *references* live in whichever database the app is pointed at. This informed a clear workflow for where to make content changes.

---

## What this project demonstrates

- **End-to-end ownership:** requirements, architecture, implementation, deployment, and production support — solo.
- **Cloud fundamentals in practice:** real use of EC2/Elastic Beanstalk, RDS, S3, and IAM, including the failure modes and how to debug them.
- **Security-conscious defaults:** least-privilege IAM, externalized secrets, hashed credentials, input filtering.
- **Pragmatism under constraints:** when a "correct" fix (proxy config) risked blocking the business, I shipped a robust alternative and kept the door open to the ideal solution.
- **Building for non-technical users:** the entire point was a system the owner could operate alone — that shaped every admin-side decision.

---

## Tech stack reference

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot, Spring Security, Spring Data JPA |
| Templating | Thymeleaf |
| Localization | Spring i18n (message bundles) |
| Database | MySQL (AWS RDS) |
| Media storage | AWS S3 |
| Hosting | AWS Elastic Beanstalk (nginx + JAR) |
| Access control | AWS IAM (least-privilege) |
| Email | Spring Mail (SMTP) |
| Build | Maven |

---

*Built and maintained solo, from first commit to production deployment and ongoing feature work.*
