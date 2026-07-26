# Fenix Bakery 🥖

A bilingual, full-stack web application and content-management system for a Cuban bakery with three locations in New Jersey. Built with Spring Boot and deployed on AWS, it lets a non-technical owner manage the entire product catalog — including photo and video uploads — without touching code.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-green)
![MySQL](https://img.shields.io/badge/MySQL-RDS-blue)
![AWS](https://img.shields.io/badge/AWS-EB%20%7C%20S3%20%7C%20RDS%20%7C%20IAM-yellow)

---

## Overview

Fenix Bakery started as a static site and grew into a production CMS. The owner manages three separate catalogs (full menu, cakes, party trays) in both Spanish and English, uploading images and short looping videos that are stored in S3 and served to customers.

The interesting part of this project isn't just the features — it's the production engineering: solving IAM permissions, ephemeral storage, reverse-proxy upload limits, and secret management on real cloud infrastructure.

## Features

- **Three editable catalogs** — menu (65+ items, 9 categories), cakes (multi-image galleries), party trays — full CRUD from the admin panel.
- **S3-backed media uploads** — images and videos upload from the admin to AWS S3; old media is auto-cleaned on replace/delete.
- **Editable cake galleries** — add/remove individual photos without disturbing the rest.
- **Looping product videos** — silent auto-looping video (Instagram-style) with automatic photo fallback.
- **Contact messaging** — stored in the DB *and* emailed, with profanity filter, accordion inbox, unread badges, and bulk actions.
- **Full bilingual support** — every field and UI string in Spanish/English via Spring i18n.
- **SEO** — dynamic meta tags, Open Graph, sitemap, robots.txt, Schema.org data for all locations.
- **Security** — Spring Security admin, BCrypt credentials, externalized secrets, least-privilege IAM.

## Architecture

```
Browser
  │
  ▼
nginx (reverse proxy, on Elastic Beanstalk)
  │
  ▼
Spring Boot app (Java 17, Thymeleaf, Spring Security)
  │              │
  ▼              ▼
MySQL (RDS)    AWS S3 (images & videos)
```

- **App:** Spring Boot + Thymeleaf, server-rendered, bilingual via Spring i18n.
- **Data:** MySQL on RDS via Spring Data JPA; seeded on first boot.
- **Media:** uploaded to S3; only URLs stored in the database.
- **Hosting:** Elastic Beanstalk (single instance) running the packaged JAR behind nginx.
- **Secrets:** injected as environment variables — nothing sensitive in the artifact.

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot, Spring Security, Spring Data JPA |
| Templating | Thymeleaf |
| Database | MySQL (AWS RDS) |
| Media | AWS S3 |
| Hosting | AWS Elastic Beanstalk (nginx + JAR) |
| Access | AWS IAM (least-privilege) |
| Email | Spring Mail (SMTP) |
| Build | Maven |

## Engineering highlights

A few production problems I worked through — the kind that don't appear in tutorials:

- **Ephemeral storage → S3.** Local disk on Elastic Beanstalk is wiped on redeploy, so uploaded photos would vanish. Moved all media to S3, persisting only URLs.
- **IAM 403 on upload.** Wrote a least-privilege inline policy scoped to `s3:PutObject`/`s3:DeleteObject` on the specific bucket ARN.
- **nginx 413 (upload limit).** Diagnosed the reverse-proxy body-size limit sitting in front of the app, worked through platform config precedence on Amazon Linux 2023, and added a direct-to-S3 path for large files as a robust fallback.
- **Secret management.** Externalized all credentials to environment variables using Spring's `${VAR:default}` pattern.

See [`CASE_STUDY.md`](./CASE_STUDY.md) for the full write-up.

## Running locally

> Requires Java 17, Maven, and a local MySQL instance.

```bash
# Configure src/main/resources/application.properties with your local
# database, mail, and (optionally) AWS S3 credentials.

./mvnw spring-boot:run
```

The app seeds its catalogs on first boot. Visit `http://localhost:8080`.

## Project structure

```
src/main/
├── java/com/bakery/
│   ├── controller/   # Public + admin controllers
│   ├── model/        # JPA entities + repositories
│   ├── service/      # Business logic, S3, email, filtering
│   └── config/       # Security, seeders, i18n
└── resources/
    ├── templates/    # Thymeleaf (public/ + admin/)
    ├── static/       # CSS, JS, images
    └── messages_*.properties   # i18n bundles
```

---

*Designed, built, deployed, and maintained solo — from first commit to production.*
