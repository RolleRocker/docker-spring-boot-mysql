package org.roland.controller;

import org.roland.model.Message;
import org.roland.model.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api")
//@CrossOrigin(origins = "*")
public class SimpleController {

    private final AtomicLong counter = new AtomicLong();

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello from Java Docker app!");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/counter")
    public ResponseEntity<Map<String, Long>> getCounter() {
        Map<String, Long> response = new HashMap<>();
        response.put("count", counter.incrementAndGet());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/messages")
    public ResponseEntity<Message> addMessage(@RequestBody Message message) {
        message.setTimestamp(LocalDateTime.now());
        Message savedMessage = messageRepository.save(message);
        return ResponseEntity.ok(savedMessage);
    }

    @GetMapping("/messages")
    public ResponseEntity<List<Message>> getMessages() {
        List<Message> messages = messageRepository.findAllByOrderByTimestampDesc();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("app", "simple-java-docker");
        info.put("version", "1.0.0");
        info.put("timestamp", LocalDateTime.now().toString());
        info.put("totalMessages", messageRepository.count());
        return ResponseEntity.ok(info);
    }
}