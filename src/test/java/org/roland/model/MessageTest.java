package org.roland.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {

    @Test
    void testDefaultConstructor() {
        // När
        Message message = new Message();
        
        // Då
        assertNull(message.getContent());
        assertNotNull(message.getTimestamp());
    }

    @Test
    void testParameterizedConstructor() {
        // När
        Message message = new Message("Test meddelande");
        
        // Då
        assertEquals("Test meddelande", message.getContent());
        assertNotNull(message.getTimestamp());
    }

    @Test
    void testSetContent() {
        // Givet
        Message message = new Message();
        
        // När
        message.setContent("Nytt innehåll");
        
        // Då
        assertEquals("Nytt innehåll", message.getContent());
    }

    @Test
    void testSetTimestamp() {
        // Givet
        Message message = new Message();
        LocalDateTime now = LocalDateTime.now();
        
        // När
        message.setTimestamp(now);
        
        // Då
        assertEquals(now, message.getTimestamp());
    }
}