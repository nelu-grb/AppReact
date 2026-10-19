package com.react.backend.msnofity.messaging;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ManualAckMessageProcessorTest {

    @Test
    void retriesRecoverableFailuresAndAcknowledgesAfterSuccess() throws IOException {
        Channel channel = mock(Channel.class);
        ManualAckMessageProcessor processor = new ManualAckMessageProcessor(3, 0, 0);
        AtomicInteger attempts = new AtomicInteger();

        processor.process("test", channel, 17, () -> {
            if (attempts.incrementAndGet() < 3) {
                throw new ResourceAccessException("temporary network error");
            }
            return null;
        });

        assertEquals(3, attempts.get());
        verify(channel).basicAck(17, false);
        verify(channel, never()).basicNack(17, false, false);
    }

    @Test
    void rejectsNonRecoverableFailuresWithoutRequeue() throws IOException {
        Channel channel = mock(Channel.class);
        ManualAckMessageProcessor processor = new ManualAckMessageProcessor(3, 0, 0);
        AtomicInteger attempts = new AtomicInteger();

        processor.process("test", channel, 23, () -> {
            attempts.incrementAndGet();
            throw new IllegalArgumentException("invalid payload");
        });

        assertEquals(1, attempts.get());
        verify(channel).basicNack(23, false, false);
        verify(channel, never()).basicAck(23, false);
    }

    @Test
    void rejectsRecoverableFailureAfterConfiguredAttemptLimit() throws IOException {
        Channel channel = mock(Channel.class);
        ManualAckMessageProcessor processor = new ManualAckMessageProcessor(3, 0, 0);
        AtomicInteger attempts = new AtomicInteger();

        processor.process("test", channel, 31, () -> {
            attempts.incrementAndGet();
            throw new ResourceAccessException("temporary network error");
        });

        assertEquals(3, attempts.get());
        verify(channel).basicNack(31, false, false);
        verify(channel, never()).basicAck(31, false);
    }
}
