package com.sanosysalvos.msarchivos.test.service;

import com.sanosysalvos.msarchivos.exception.ArchivoInvalidoException;
import com.sanosysalvos.msarchivos.model.ArchivoFoto;
import com.sanosysalvos.msarchivos.repository.ArchivoFotoRepository;
import com.sanosysalvos.msarchivos.service.ArchivoFotoService;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArchivoFotoServiceTest {

    @Mock
    private ArchivoFotoRepository repository;

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private ArchivoFotoService service;

    @Test
    void testListarArchivosPorMascota() {
        ArchivoFoto archivo1 = new ArchivoFoto(1L, "foto1.jpg", "http://url1", "image/jpeg", 1024L, 1L, "obj1.jpg", LocalDateTime.now());
        ArchivoFoto archivo2 = new ArchivoFoto(2L, "foto2.jpg", "http://url2", "image/jpeg", 2048L, 1L, "obj2.jpg", LocalDateTime.now());
        when(repository.findByMascotaIdOrderBySubidoEnDesc(1L)).thenReturn(Arrays.asList(archivo1, archivo2));

        List<ArchivoFoto> result = service.listarArchivosPorMascota(1L);

        assertEquals(2, result.size());
        verify(repository).findByMascotaIdOrderBySubidoEnDesc(1L);
    }

    @Test
    void subirArchivo_tipoNoPermitido_lanzaArchivoInvalidoException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "malicioso.exe", "application/x-msdownload", new byte[]{1, 2, 3});

        assertThrows(ArchivoInvalidoException.class, () -> service.subirArchivo(file, 1L));
        verifyNoInteractions(repository);
    }

    @Test
    void subirArchivo_contenidoNoCoincideConTipoDeclarado_lanzaArchivoInvalidoException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "foto.png", "image/png", "esto no es un png".getBytes());

        assertThrows(ArchivoInvalidoException.class, () -> service.subirArchivo(file, 1L));
        verifyNoInteractions(repository);
    }

    @Test
    void subirArchivo_excedeTamanioMaximo_lanzaArchivoInvalidoException() {
        byte[] contenidoGrande = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "file", "foto.png", "image/png", contenidoGrande);

        assertThrows(ArchivoInvalidoException.class, () -> service.subirArchivo(file, 1L));
        verifyNoInteractions(repository);
    }

    @Test
    void subirArchivo_archivoVacio_lanzaArchivoInvalidoException() {
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", new byte[0]);

        assertThrows(ArchivoInvalidoException.class, () -> service.subirArchivo(file, 1L));
        verifyNoInteractions(repository);
    }
}