package org.roland.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.roland.dto.CounterResponse;
import org.roland.dto.HelloResponse;
import org.roland.dto.InfoResponse;
import org.roland.dto.MessageRequest;
import org.roland.dto.MessageResponse;
import org.roland.model.Message;
import org.roland.model.MessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
//@CrossOrigin(origins = "*")
public class SimpleController {

    private final AtomicLong counter = new AtomicLong();
    private final MessageRepository messageRepository;

    public SimpleController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @GetMapping("/hello")
    public ResponseEntity<HelloResponse> hello() {
        return ResponseEntity.ok(
            new HelloResponse("Hello from Java Docker app!", LocalDateTime.now().toString())
        );
    }

    @GetMapping("/counter")
    public ResponseEntity<CounterResponse> getCounter() {
        return ResponseEntity.ok(new CounterResponse(counter.incrementAndGet()));
    }

    @PostMapping("/messages")
    public ResponseEntity<MessageResponse> addMessage(@Valid @RequestBody MessageRequest request) {
        Message message = new Message(request.content());
        message.setTimestamp(LocalDateTime.now());
        Message savedMessage = messageRepository.save(message);
        return ResponseEntity.ok(MessageResponse.fromEntity(savedMessage));
    }

    @GetMapping("/messages")
    public ResponseEntity<List<MessageResponse>> getMessages() {
        List<MessageResponse> messages = messageRepository.findAllByOrderByTimestampDesc()
            .stream()
            .map(MessageResponse::fromEntity)
            .toList();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/info")
    public ResponseEntity<InfoResponse> getInfo() {
        return ResponseEntity.ok(
            new InfoResponse(
                "simple-java-docker",
                "1.0.0",
                LocalDateTime.now().toString(),
                messageRepository.count()
            )
        );
    }
}