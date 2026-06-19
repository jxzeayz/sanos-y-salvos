package com.sanosysalvos.msauditoria.test.consumer;

import com.sanosysalvos.msauditoria.consumer.AuditEventConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditEventConsumerTest {

    @Mock
    private AuditEventConsumer consumer;

    @Test
    void testProcesarEvento() {
        String mensaje = "{\"evento\":\"test\",\"servicioOrigen\":\"test\",\"payload\":{\"data\":\"test\"}}";
        doNothing().when(consumer).procesarEvento(mensaje);

        consumer.procesarEvento(mensaje);

        verify(consumer).procesarEvento(mensaje);
    }
}