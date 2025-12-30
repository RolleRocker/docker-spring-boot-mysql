
package org.roland.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.roland.dto.CounterResponse;
import org.roland.dto.HelloResponse;
import org.roland.dto.InfoResponse;
import org.roland.dto.MessageRequest;
import org.roland.dto.MessageResponse;
import org.roland.model.Message;
import org.roland.model.MessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class SimpleControllerTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private SimpleController simpleController;

    @Test
    void testHello() {
        // När
        ResponseEntity<HelloResponse> response = simpleController.hello();

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        HelloResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Hello from Java Docker app!", body.message());
        assertNotNull(body.timestamp());
    }

    @Test
    void testHelloResponseContainsTimestamp() {
        // När
        ResponseEntity<HelloResponse> response = simpleController.hello();

        // Då
        HelloResponse body = response.getBody();
        assertNotNull(body);
        String timestamp = body.timestamp();
        assertNotNull(timestamp);
        // Verifiera att timestamp är i rätt format (ISO LocalDateTime)
        assertDoesNotThrow(() -> LocalDateTime.parse(timestamp));
    }

    @Test
    void testGetCounter() {
        // När
        ResponseEntity<CounterResponse> response1 = simpleController.getCounter();
        ResponseEntity<CounterResponse> response2 = simpleController.getCounter();

        // Då
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        CounterResponse body1 = response1.getBody();
        assertNotNull(body1);
        assertEquals(1L, body1.count());

        assertEquals(HttpStatus.OK, response2.getStatusCode());
        CounterResponse body2 = response2.getBody();
        assertNotNull(body2);
        assertEquals(2L, body2.count());
    }

    @Test
    void testGetCounterMultipleCalls() {
        // Testa flera anrop för att verifiera att räknaren fortsätter att öka
        for (int i = 1; i <= 5; i++) {
            ResponseEntity<CounterResponse> response = simpleController.getCounter();
            assertEquals(HttpStatus.OK, response.getStatusCode());
            CounterResponse body = response.getBody();
            assertNotNull(body);
            assertEquals(i, body.count());
        }
    }

    @Test
    void testAddMessage() {
        // Givet
        MessageRequest request = new MessageRequest("Test meddelande");
        Message savedMessage = new Message("Test meddelande");
        savedMessage.setTimestamp(LocalDateTime.now());

        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        // När
        ResponseEntity<MessageResponse> response = simpleController.addMessage(request);

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        MessageResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Test meddelande", body.content());
        assertNotNull(body.timestamp());

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testAddMessageWithNullContent() {
        // Givet
        MessageRequest request = new MessageRequest(null);
        Message savedMessage = new Message();
        savedMessage.setContent(null);
        savedMessage.setTimestamp(LocalDateTime.now());

        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        // När
        ResponseEntity<MessageResponse> response = simpleController.addMessage(request);

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        MessageResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(null, body.content());
        assertNotNull(body.timestamp());

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testAddMessageWithEmptyContent() {
        // Givet
        MessageRequest request = new MessageRequest("");
        Message savedMessage = new Message("");
        savedMessage.setTimestamp(LocalDateTime.now());

        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        // När
        ResponseEntity<MessageResponse> response = simpleController.addMessage(request);

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        MessageResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("", body.content());
        assertNotNull(body.timestamp());

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testAddMessageWithLongContent() {
        // Givet
        String longContent = "A".repeat(1000); // Testar med max längd enligt @Column(length = 1000)
        MessageRequest request = new MessageRequest(longContent);
        Message savedMessage = new Message(longContent);
        savedMessage.setTimestamp(LocalDateTime.now());

        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        // När
        ResponseEntity<MessageResponse> response = simpleController.addMessage(request);

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        MessageResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(longContent, body.content());

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testAddMessageSetsTimestamp() {
        // Givet
        MessageRequest request = new MessageRequest("Test");
        LocalDateTime beforeCall = LocalDateTime.now().minusSeconds(1);

        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.<Message>getArgument(0));

        // När
        ResponseEntity<MessageResponse> response = simpleController.addMessage(request);
        LocalDateTime afterCall = LocalDateTime.now().plusSeconds(1);

        // Då
        MessageResponse body = response.getBody();
        assertNotNull(body);
        LocalDateTime timestamp = body.timestamp();
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
        ResponseEntity<List<MessageResponse>> response = simpleController.getMessages();

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<MessageResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals(2, body.size());
        assertEquals("Första meddelandet", body.get(0).content());
        assertEquals("Andra meddelandet", body.get(1).content());

        verify(messageRepository).findAllByOrderByTimestampDesc();
    }

    @Test
    void testGetMessagesWhenEmpty() {
        // Givet
        when(messageRepository.findAllByOrderByTimestampDesc()).thenReturn(Collections.emptyList());

        // När
        ResponseEntity<List<MessageResponse>> response = simpleController.getMessages();

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<MessageResponse> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isEmpty());

        verify(messageRepository).findAllByOrderByTimestampDesc();
    }

    @Test
    void testGetMessagesSingleMessage() {
        // Givet
        Message message = new Message("Enda meddelandet");
        when(messageRepository.findAllByOrderByTimestampDesc()).thenReturn(Collections.singletonList(message));

        // När
        ResponseEntity<List<MessageResponse>> response = simpleController.getMessages();

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<MessageResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals("Enda meddelandet", body.getFirst().content());
    }

    @Test
    void testInfo() {
        // Givet
        when(messageRepository.count()).thenReturn(1L);

        simpleController.getCounter(); // ökar räknaren till 1

        // När
        ResponseEntity<InfoResponse> response = simpleController.getInfo();

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        InfoResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("simple-java-docker", body.app());
        assertEquals("1.0.0", body.version());
        assertEquals(1L, body.totalMessages());
        assertNotNull(body.timestamp());

        verify(messageRepository).count();
    }

    @Test
    void testInfoWithZeroMessages() {
        // Givet
        when(messageRepository.count()).thenReturn(0L);

        // När
        ResponseEntity<InfoResponse> response = simpleController.getInfo();

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        InfoResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(0L, body.totalMessages());
        assertEquals("simple-java-docker", body.app());
        assertEquals("1.0.0", body.version());

        verify(messageRepository).count();
    }

    @Test
    void testInfoWithManyMessages() {
        // Givet
        when(messageRepository.count()).thenReturn(100L);

        // När
        ResponseEntity<InfoResponse> response = simpleController.getInfo();

        // Då
        assertEquals(HttpStatus.OK, response.getStatusCode());
        InfoResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(100L, body.totalMessages());

        verify(messageRepository).count();
    }

    @Test
    void testInfoTimestampFormat() {
        // Givet
        when(messageRepository.count()).thenReturn(0L);

        // När
        ResponseEntity<InfoResponse> response = simpleController.getInfo();

        // Då
        InfoResponse body = response.getBody();
        assertNotNull(body);
        String timestamp = body.timestamp();
        assertNotNull(timestamp);
        // Verifiera att timestamp är i rätt format
        assertDoesNotThrow(() -> LocalDateTime.parse(timestamp));
    }

    @Test
    void testInfoContainsAllRequiredFields() {
        // Givet
        when(messageRepository.count()).thenReturn(5L);

        // När
        ResponseEntity<InfoResponse> response = simpleController.getInfo();

        // Då
        InfoResponse body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.app());
        assertNotNull(body.version());
        assertNotNull(body.timestamp());
        assertNotNull(body.totalMessages());
        // Verify all fields have correct values
        assertEquals("simple-java-docker", body.app());
        assertEquals("1.0.0", body.version());
        assertEquals(5L, body.totalMessages());
    }

    @Test
    void testCounterIndependenceFromOtherMethods() {
        // Testa att räknaren är oberoende av andra metod-anrop

        // Anropa andra metoder
        simpleController.hello();
        simpleController.getMessages();
        simpleController.getInfo();

        // Räknaren ska fortfarande vara 0
        ResponseEntity<CounterResponse> response = simpleController.getCounter();
        CounterResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(1L, body.count());
    }

    @Test
    void testMultipleMessageOperations() {
        // Testa en sekvens av operationer
        Message msg1 = new Message("Första");
        Message msg2 = new Message("Andra");
        MessageRequest req1 = new MessageRequest("Första");
        MessageRequest req2 = new MessageRequest("Andra");

        when(messageRepository.save(any(Message.class))).thenReturn(msg1, msg2);
        when(messageRepository.findAllByOrderByTimestampDesc()).thenReturn(Arrays.asList(msg2, msg1));
        when(messageRepository.count()).thenReturn(2L);

        // Lägg till meddelanden
        simpleController.addMessage(req1);
        simpleController.addMessage(req2);

        // Hämta meddelanden
        ResponseEntity<List<MessageResponse>> messagesResponse = simpleController.getMessages();
        List<MessageResponse> messagesBody = messagesResponse.getBody();
        assertNotNull(messagesBody);
        assertEquals(2, messagesBody.size());

        // Kontrollera info
        ResponseEntity<InfoResponse> infoResponse = simpleController.getInfo();
        InfoResponse infoBody = infoResponse.getBody();
        assertNotNull(infoBody);
        assertEquals(2L, infoBody.totalMessages());

        verify(messageRepository, times(2)).save(any(Message.class));
        verify(messageRepository).findAllByOrderByTimestampDesc();
        verify(messageRepository).count();
    }
}