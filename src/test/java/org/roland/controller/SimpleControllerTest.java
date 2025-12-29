
package org.roland.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.roland.model.Message;
import org.roland.model.MessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class SimpleControllerTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private SimpleController simpleController;

    @Test
    void testHello() {
        // När
        ResponseEntity<Map<String, String>> response = simpleController.hello();

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Hello from Java Docker app!", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void testHelloResponseContainsTimestamp() {
        // När
        ResponseEntity<Map<String, String>> response = simpleController.hello();

        // Då
        assertNotNull(response.getBody());
        String timestamp = response.getBody().get("timestamp");
        assertNotNull(timestamp);
        // Verifiera att timestamp är i rätt format (ISO LocalDateTime)
        assertDoesNotThrow(() -> LocalDateTime.parse(timestamp));
    }

    @Test
    void testGetCounter() {
        // När
        ResponseEntity<Map<String, Long>> response1 = simpleController.getCounter();
        ResponseEntity<Map<String, Long>> response2 = simpleController.getCounter();

        // Då
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertNotNull(response1.getBody());
        assertEquals(1L, response1.getBody().get("count"));

        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertNotNull(response2.getBody());
        assertEquals(2L, response2.getBody().get("count"));
    }

    @Test
    void testGetCounterMultipleCalls() {
        // Testa flera anrop för att verifiera att räknaren fortsätter att öka
        for (int i = 1; i <= 5; i++) {
            ResponseEntity<Map<String, Long>> response = simpleController.getCounter();
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(i, response.getBody().get("count"));
        }
    }

    @Test
    void testAddMessage() {
        // Givet
        Message inputMessage = new Message("Test meddelande");
        Message savedMessage = new Message("Test meddelande");
        savedMessage.setTimestamp(LocalDateTime.now());

        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        // När
        ResponseEntity<Message> response = simpleController.addMessage(inputMessage);

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test meddelande", response.getBody().getContent());
        assertNotNull(response.getBody().getTimestamp());

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testAddMessageWithNullContent() {
        // Givet
        Message inputMessage = new Message();
        inputMessage.setContent(null);
        Message savedMessage = new Message();
        savedMessage.setContent(null);
        savedMessage.setTimestamp(LocalDateTime.now());

        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        // När
        ResponseEntity<Message> response = simpleController.addMessage(inputMessage);

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getContent());
        assertNotNull(response.getBody().getTimestamp());

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testAddMessageWithEmptyContent() {
        // Givet
        Message inputMessage = new Message("");
        Message savedMessage = new Message("");
        savedMessage.setTimestamp(LocalDateTime.now());

        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        // När
        ResponseEntity<Message> response = simpleController.addMessage(inputMessage);

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("", response.getBody().getContent());
        assertNotNull(response.getBody().getTimestamp());

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testAddMessageWithLongContent() {
        // Givet
        String longContent = "A".repeat(1000); // Testar med max längd enligt @Column(length = 1000)
        Message inputMessage = new Message(longContent);
        Message savedMessage = new Message(longContent);
        savedMessage.setTimestamp(LocalDateTime.now());

        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        // När
        ResponseEntity<Message> response = simpleController.addMessage(inputMessage);

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(longContent, response.getBody().getContent());

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testAddMessageSetsTimestamp() {
        // Givet
        Message inputMessage = new Message("Test");
        LocalDateTime beforeCall = LocalDateTime.now().minusSeconds(1);

        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.<Message>getArgument(0));

        // När
        ResponseEntity<Message> response = simpleController.addMessage(inputMessage);
        LocalDateTime afterCall = LocalDateTime.now().plusSeconds(1);

        // Då
        assertNotNull(response.getBody());
        LocalDateTime timestamp = response.getBody().getTimestamp();
        assertTrue(timestamp.isAfter(beforeCall) && timestamp.isBefore(afterCall));
    }

    @Test
    void testGetMessages() {
        // Givet
        Message message1 = new Message("Första meddelandet");
        Message message2 = new Message("Andra meddelandet");
        List<Message> messages = Arrays.asList(message1, message2);

        when(messageRepository.findAllByOrderByTimestampDesc()).thenReturn(messages);

        // När
        ResponseEntity<List<Message>> response = simpleController.getMessages();

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Första meddelandet", response.getBody().get(0).getContent());
        assertEquals("Andra meddelandet", response.getBody().get(1).getContent());

        verify(messageRepository).findAllByOrderByTimestampDesc();
    }

    @Test
    void testGetMessagesWhenEmpty() {
        // Givet
        when(messageRepository.findAllByOrderByTimestampDesc()).thenReturn(Collections.emptyList());

        // När
        ResponseEntity<List<Message>> response = simpleController.getMessages();

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(messageRepository).findAllByOrderByTimestampDesc();
    }

    @Test
    void testGetMessagesSingleMessage() {
        // Givet
        Message message = new Message("Enda meddelandet");
        when(messageRepository.findAllByOrderByTimestampDesc()).thenReturn(Collections.singletonList(message));

        // När
        ResponseEntity<List<Message>> response = simpleController.getMessages();

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Enda meddelandet", response.getBody().getFirst().getContent());
    }

    @Test
    void testInfo() {
        // Givet
        when(messageRepository.count()).thenReturn(1L);

        simpleController.getCounter(); // ökar räknaren till 1

        // När
        ResponseEntity<Map<String, Object>> response = simpleController.getInfo();

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("simple-java-docker", response.getBody().get("app"));
        assertEquals("1.0.0", response.getBody().get("version"));
        assertEquals(1L, response.getBody().get("totalMessages"));
        assertNotNull(response.getBody().get("timestamp"));

        verify(messageRepository).count();
    }

    @Test
    void testInfoWithZeroMessages() {
        // Givet
        when(messageRepository.count()).thenReturn(0L);

        // När
        ResponseEntity<Map<String, Object>> response = simpleController.getInfo();

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0L, response.getBody().get("totalMessages"));
        assertEquals("simple-java-docker", response.getBody().get("app"));
        assertEquals("1.0.0", response.getBody().get("version"));

        verify(messageRepository).count();
    }

    @Test
    void testInfoWithManyMessages() {
        // Givet
        when(messageRepository.count()).thenReturn(100L);

        // När
        ResponseEntity<Map<String, Object>> response = simpleController.getInfo();

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().get("totalMessages"));

        verify(messageRepository).count();
    }

    @Test
    void testInfoTimestampFormat() {
        // Givet
        when(messageRepository.count()).thenReturn(0L);

        // När
        ResponseEntity<Map<String, Object>> response = simpleController.getInfo();

        // Då
        assertNotNull(response.getBody());
        String timestamp = (String) response.getBody().get("timestamp");
        assertNotNull(timestamp);
        // Verifiera att timestamp är i rätt format
        assertDoesNotThrow(() -> LocalDateTime.parse(timestamp));
    }

    @Test
    void testInfoContainsAllRequiredFields() {
        // Givet
        when(messageRepository.count()).thenReturn(5L);

        // När
        ResponseEntity<Map<String, Object>> response = simpleController.getInfo();

        // Då
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("app"));
        assertTrue(body.containsKey("version"));
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body.containsKey("totalMessages"));
        assertEquals(4, body.size()); // Kontrollera att det inte finns extra fält
    }

    @Test
    void testCounterIndependenceFromOtherMethods() {
        // Testa att räknaren är oberoende av andra metod-anrop

        // Anropa andra metoder
        simpleController.hello();
        simpleController.getMessages();
        simpleController.getInfo();

        // Räknaren ska fortfarande vara 0
        ResponseEntity<Map<String, Long>> response = simpleController.getCounter();
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().get("count"));
    }

    @Test
    void testMultipleMessageOperations() {
        // Testa en sekvens av operationer
        Message msg1 = new Message("Första");
        Message msg2 = new Message("Andra");

        when(messageRepository.save(any(Message.class))).thenReturn(msg1, msg2);
        when(messageRepository.findAllByOrderByTimestampDesc()).thenReturn(Arrays.asList(msg2, msg1));
        when(messageRepository.count()).thenReturn(2L);

        // Lägg till meddelanden
        simpleController.addMessage(msg1);
        simpleController.addMessage(msg2);

        // Hämta meddelanden
        ResponseEntity<List<Message>> messagesResponse = simpleController.getMessages();
        assertNotNull(messagesResponse.getBody());
        assertEquals(2, messagesResponse.getBody().size());

        // Kontrollera info
        ResponseEntity<Map<String, Object>> infoResponse = simpleController.getInfo();
        assertNotNull(infoResponse.getBody());
        assertEquals(2L, infoResponse.getBody().get("totalMessages"));

        verify(messageRepository, times(2)).save(any(Message.class));
        verify(messageRepository).findAllByOrderByTimestampDesc();
        verify(messageRepository).count();
    }
}