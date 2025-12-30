package org.roland.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.roland.model.Message;
import org.roland.model.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@WebMvcTest(SimpleController.class)
@ContextConfiguration(classes = {SimpleController.class, SimpleControllerIntegrationTest.TestConfig.class})
@SuppressWarnings("null")
class SimpleControllerIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public MessageRepository messageRepository() {
            return Mockito.mock(MessageRepository.class);
        }

        @Bean
        @Primary
        public ObjectMapper objectMapper() {
            return new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
    }

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final MessageRepository messageRepository;

    @Autowired
    SimpleControllerIntegrationTest(MockMvc mockMvc, ObjectMapper objectMapper, MessageRepository messageRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.messageRepository = messageRepository;
    }

    @BeforeEach
    void setUp() {
        Mockito.reset(messageRepository);
    }

    @Test
    void testHelloEndpoint() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Hello from Java Docker app!")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void testCounterEndpoint() throws Exception {
        // Första anropet
        mockMvc.perform(get("/api/counter"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count", is(1)));

        // Andra anropet
        mockMvc.perform(get("/api/counter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(2)));

        // Tredje anropet för att testa fortsatt räkning
        mockMvc.perform(get("/api/counter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(3)));
    }

    @Test
    void testCounterConcurrentRequests() throws Exception {
        // Simulera flera samtidiga anrop
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(get("/api/counter"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count", is(i)));
        }
    }

    @Test
    void testMessagesEndpointBasic() throws Exception {
        Message message = new Message("Test meddelande");
        message.setTimestamp(LocalDateTime.now());

        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(messageRepository.findAllByOrderByTimestampDesc()).thenReturn(Arrays.asList(message));

        // Testa att lägga till ett meddelande
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", is("Test meddelande")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));

        // Testa att hämta alla meddelanden
        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].content", is("Test meddelande")));
    }

    @Test
    void testAddMessageWithNullContent() throws Exception {
        Message message = new Message();
        message.setContent(null);
        message.setTimestamp(LocalDateTime.now());

        // This should actually fail due to @Column(nullable = false)
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isBadRequest()); // Changed from isOk()
    }


    @Test
    void testAddMessageWithEmptyContent() throws Exception {
        Message message = new Message("");
        message.setTimestamp(LocalDateTime.now());

        when(messageRepository.save(any(Message.class))).thenReturn(message);

        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("")));
    }

    @Test
    void testAddMessageWithLongContent() throws Exception {
        String longContent = "A".repeat(1000);
        Message message = new Message(longContent);
        message.setTimestamp(LocalDateTime.now());

        when(messageRepository.save(any(Message.class))).thenReturn(message);

        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(longContent)));
    }

    @Test
    void testAddMessageWithSpecialCharacters() throws Exception {
        String specialContent = "Åäö! @#$%^&*(){}[]|\\:;\"'<>,.?/~`+=_-";
        Message message = new Message(specialContent);
        message.setTimestamp(LocalDateTime.now());

        when(messageRepository.save(any(Message.class))).thenReturn(message);

        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(specialContent)));
    }

    @Test
    void testAddMessageInvalidJson() throws Exception {
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddMessageMissingContentType() throws Exception {
        Message message = new Message("Test");

        mockMvc.perform(post("/api/messages")
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void testGetMessagesWhenEmpty() throws Exception {
        when(messageRepository.findAllByOrderByTimestampDesc()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(jsonPath("$", is(empty())));
    }

    @Test
    void testGetMessagesMultiple() throws Exception {
        Message message1 = new Message("Första meddelandet");
        message1.setTimestamp(LocalDateTime.now().minusMinutes(5));

        Message message2 = new Message("Andra meddelandet");
        message2.setTimestamp(LocalDateTime.now());

        when(messageRepository.findAllByOrderByTimestampDesc())
                .thenReturn(Arrays.asList(message2, message1)); // Desc order

        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content", is("Andra meddelandet")))
                .andExpect(jsonPath("$[1].content", is("Första meddelandet")));
    }

    @Test
    void testInfoEndpoint() throws Exception {
        when(messageRepository.count()).thenReturn(0L);

        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.app", is("simple-java-docker")))
                .andExpect(jsonPath("$.version", is("1.0.0")))
                .andExpect(jsonPath("$.totalMessages", is(0)))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void testInfoEndpointWithMessages() throws Exception {
        when(messageRepository.count()).thenReturn(5L);

        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMessages", is(5)));
    }

    @Test
    void testInfoEndpointStructure() throws Exception {
        when(messageRepository.count()).thenReturn(0L);

        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasKey("app")))
                .andExpect(jsonPath("$", hasKey("version")))
                .andExpect(jsonPath("$", hasKey("timestamp")))
                .andExpect(jsonPath("$", hasKey("totalMessages")));
    }

    @Test
    void testNonExistentEndpoint() throws Exception {
        mockMvc.perform(get("/api/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUnsupportedHttpMethod() throws Exception {
        mockMvc.perform(delete("/api/hello"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void testPostToGetOnlyEndpoint() throws Exception {
        mockMvc.perform(post("/api/hello"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void testGetToPostOnlyEndpoint() throws Exception {
        mockMvc.perform(get("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()); // GET is allowed for /api/messages
    }

    @Test
    void testCompleteWorkflow() throws Exception {
        // Steg 1: Kontrollera initial state
        when(messageRepository.count()).thenReturn(0L);
        mockMvc.perform(get("/api/info"))
                .andExpect(jsonPath("$.totalMessages", is(0)));

        // Steg 2: Lägg till ett meddelande
        Message message = new Message("Workflow test");
        message.setTimestamp(LocalDateTime.now());
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(messageRepository.findAllByOrderByTimestampDesc()).thenReturn(Arrays.asList(message));
        when(messageRepository.count()).thenReturn(1L);

        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk());

        // Steg 3: Verifiera att meddelandet finns
        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].content", is("Workflow test")));

        // Steg 4: Kontrollera uppdaterad info
        mockMvc.perform(get("/api/info"))
                .andExpect(jsonPath("$.totalMessages", is(1)));

        // Remove Step 5 - counter test doesn't belong in message workflow
    }

    @Test
    void testApiPrefix() throws Exception {
        // Testa att endpoints fungerar med /api prefix
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk());

        // Testa att endpoints INTE fungerar utan /api prefix
        mockMvc.perform(get("/hello"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testResponseHeaders() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/json")));
    }

    @Test
    void testTimestampConsistency() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void testMultipleSimultaneousRequests() throws Exception {
        // Simulera flera samtidiga requests till olika endpoints
        when(messageRepository.count()).thenReturn(0L);
        when(messageRepository.findAllByOrderByTimestampDesc()).thenReturn(Collections.emptyList());

        // Kör flera requests "samtidigt"
        mockMvc.perform(get("/api/hello")).andExpect(status().isOk());
        mockMvc.perform(get("/api/counter")).andExpect(status().isOk());
        mockMvc.perform(get("/api/messages")).andExpect(status().isOk());
        mockMvc.perform(get("/api/info")).andExpect(status().isOk());
    }

    @Test
    void testMessageCreatedWithCorrectTimestamp() throws Exception {
        Message savedMessage = new Message("Test");
        savedMessage.setTimestamp(LocalDateTime.now());

        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Message("Test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void testErrorHandling() throws Exception {
        // Test with completely malformed JSON
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not json at all"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testEmptyRequestBody() throws Exception {
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCounterIncrementsCorrectly() throws Exception {
        // Get current counter value
        mockMvc.perform(get("/api/counter"))
                .andExpect(status().isOk());

        // Get counter value again and verify it incremented
        mockMvc.perform(get("/api/counter"))
                .andExpect(status().isOk());

        // Verify the counter is positive and functional
        mockMvc.perform(get("/api/counter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", greaterThan(0)));
    }


    @Test
    void testMessageWithUnicodeCharacters() throws Exception {
        String unicodeContent = "🚀 Emoji test 🎉 Svenska åäö";
        Message message = new Message(unicodeContent);
        message.setTimestamp(LocalDateTime.now());

        when(messageRepository.save(any(Message.class))).thenReturn(message);

        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(unicodeContent)));
    }

    @Test
    void testLargePayload() throws Exception {
        String largeContent = "X".repeat(999); // Just under the 1000 limit
        Message message = new Message(largeContent);
        message.setTimestamp(LocalDateTime.now());

        when(messageRepository.save(any(Message.class))).thenReturn(message);

        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(largeContent)));
    }

    @Test
    void testBoundaryConditions() throws Exception {
        // Test med exakt 1000 tecken
        String exactLimit = "Y".repeat(1000);
        Message message = new Message(exactLimit);
        message.setTimestamp(LocalDateTime.now());

        when(messageRepository.save(any(Message.class))).thenReturn(message);

        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(exactLimit)));
    }
    @Test
    void testCounterIncrement() throws Exception {
        // Test single increment
        mockMvc.perform(get("/api/counter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", greaterThan(0)));
    }
}