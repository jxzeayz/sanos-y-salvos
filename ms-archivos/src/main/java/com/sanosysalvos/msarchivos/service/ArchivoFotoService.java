package com.sanosysalvos.msarchivos.service;

import com.sanosysalvos.msarchivos.exception.ArchivoInvalidoException;
import com.sanosysalvos.msarchivos.exception.ArchivoNoEncontradoException;
import com.sanosysalvos.msarchivos.model.ArchivoFoto;
import com.sanosysalvos.msarchivos.repository.ArchivoFotoRepository;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ArchivoFotoService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long TAMANIO_MAXIMO_BYTES = 5L * 1024 * 1024;

    private final ArchivoFotoRepository repository;
    private final MinioClient minioClient;
    private final String bucketName = "archivos";

    public ArchivoFotoService(ArchivoFotoRepository repository, MinioClient minioClient) {
        this.repository = repository;
        this.minioClient = minioClient;
    }

    public ArchivoFoto subirArchivo(MultipartFile file, Long mascotaId) {
        byte[] contenido = validarYLeerArchivo(file);

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String objectName = UUID.randomUUID().toString() + extension;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(contenido), contenido.length, -1)
                            .contentType(file.getContentType())
                            .build()
            );

            String urlPublica = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(60 * 60 * 24)
                            .build()
            );

            ArchivoFoto archivo = new ArchivoFoto();
            archivo.setNombreArchivo(originalFilename);
            archivo.setUrlPublica(urlPublica);
            archivo.setMimeType(file.getContentType());
            archivo.setTamanioBytes(file.getSize());
            archivo.setMascotaId(mascotaId);
            archivo.setObjectName(objectName);
            archivo.setSubidoEn(LocalDateTime.now());

            return repository.save(archivo);

        } catch (ArchivoInvalidoException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al subir archivo a MinIO: " + e.getMessage(), e);
        }
    }

    private byte[] validarYLeerArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ArchivoInvalidoException("El archivo está vacío");
        }
        if (file.getSize() > TAMANIO_MAXIMO_BYTES) {
            throw new ArchivoInvalidoException("El archivo excede el tamaño máximo permitido (5 MB)");
        }
        String contentType = file.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType.toLowerCase())) {
            throw new ArchivoInvalidoException("Tipo de archivo no permitido: " + contentType);
        }

        byte[] contenido;
        try {
            contenido = file.getBytes();
        } catch (Exception e) {
            throw new ArchivoInvalidoException("No se pudo leer el archivo");
        }

        if (!coincideConTipoDeclarado(contenido, contentType.toLowerCase())) {
            throw new ArchivoInvalidoException("El contenido del archivo no coincide con el tipo declarado");
        }
        return contenido;
    }

    private boolean coincideConTipoDeclarado(byte[] contenido, String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> tieneFirma(contenido, 0xFF, 0xD8, 0xFF);
            case "image/png" -> tieneFirma(contenido, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            case "image/gif" -> tieneFirma(contenido, 0x47, 0x49, 0x46, 0x38);
            case "image/webp" -> contenido.length >= 12
                    && tieneFirma(contenido, 0x52, 0x49, 0x46, 0x46)
                    && contenido[8] == 0x57 && contenido[9] == 0x45 && contenido[10] == 0x42 && contenido[11] == 0x50;
            default -> false;
        };
    }

    private boolean tieneFirma(byte[] contenido, int... firma) {
        if (contenido.length < firma.length) return false;
        for (int i = 0; i < firma.length; i++) {
            if ((contenido[i] & 0xFF) != firma[i]) return false;
        }
        return true;
    }

    public void eliminarArchivo(Long id) {
        ArchivoFoto archivo = repository.findById(id)
                .orElseThrow(() -> new ArchivoNoEncontradoException("Archivo no encontrado"));

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(resolverObjectName(archivo))
                            .build()
            );

            repository.delete(archivo);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar archivo de MinIO: " + e.getMessage(), e);
        }
    }

    public String obtenerUrlTemporal(Long id) {
        ArchivoFoto archivo = repository.findById(id)
                .orElseThrow(() -> new ArchivoNoEncontradoException("Archivo no encontrado"));

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(resolverObjectName(archivo))
                            .method(Method.GET)
                            .expiry(60 * 60 * 24)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error al generar URL temporal: " + e.getMessage(), e);
        }
    }

    public List<ArchivoFoto> listarArchivosPorMascota(Long mascotaId) {
        return repository.findByMascotaIdOrderBySubidoEnDesc(mascotaId);
    }

    public ArchivoFoto obtenerArchivo(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ArchivoNoEncontradoException("Archivo no encontrado"));
    }

    public InputStream descargarStream(Long id) {
        ArchivoFoto archivo = repository.findById(id)
                .orElseThrow(() -> new ArchivoNoEncontradoException("Archivo no encontrado"));

        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(resolverObjectName(archivo))
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error al descargar archivo de MinIO: " + e.getMessage(), e);
        }
    }

    /**
     * Los registros creados antes de agregar la columna objectName no la tienen;
     * para esos se re-deriva desde la URL guardada como fallback.
     */
    private String resolverObjectName(ArchivoFoto archivo) {
        if (archivo.getObjectName() != null) {
            return archivo.getObjectName();
        }
        String path = archivo.getUrlPublica().split("\\?")[0];
        return path.substring(path.lastIndexOf("/") + 1);
    }
}
