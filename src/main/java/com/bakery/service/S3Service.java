package com.bakery.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    private S3Client s3;

    @PostConstruct
    public void init() {
        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            // Opción B: credenciales explícitas
            s3 = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
        } else {
            // Si no hay credenciales explícitas, usa la cadena por defecto (rol de instancia en EB)
            s3 = S3Client.builder()
                .region(Region.of(region))
                .build();
        }
    }

    /**
     * Sube un archivo a S3 y devuelve la URL pública.
     * La carpeta dentro del bucket (ej: "menu", "cakes") organiza las fotos.
     */
    public String subirArchivo(MultipartFile archivo, String carpeta) throws IOException {
        String extension = obtenerExtension(archivo.getOriginalFilename());
        String key = carpeta + "/" + UUID.randomUUID() + extension;

        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(archivo.getContentType())
            .build();

        s3.putObject(request, RequestBody.fromBytes(archivo.getBytes()));

        // URL pública del objeto
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    /**
     * Elimina un archivo de S3 a partir de su URL pública.
     */
    public void eliminarArchivo(String url) {
        if (url == null || !url.contains(".amazonaws.com/")) return;
        try {
            String key = url.substring(url.indexOf(".amazonaws.com/") + ".amazonaws.com/".length());
            s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (Exception e) {
            // No interrumpir el flujo si falla la eliminación de la foto vieja
            System.err.println("[S3Service] No se pudo eliminar la foto: " + e.getMessage());
        }
    }

    private String obtenerExtension(String nombre) {
        if (nombre == null || !nombre.contains(".")) return ".jpg";
        return nombre.substring(nombre.lastIndexOf("."));
    }
}
