package com.pokemon.lobby.controller;

import com.pokemon.lobby.dto.QueueRequest;
import com.pokemon.lobby.dto.QueueStatusResponse;
import com.pokemon.lobby.service.LobbyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lobby")
public class LobbyController {

    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @PostMapping("/queue")
    public ResponseEntity<QueueStatusResponse> joinQueue(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody QueueRequest request) {
        lobbyService.enqueue(userId, request.teamId(), request.format());
        return ResponseEntity.ok(QueueStatusResponse.queued());
    }

    @GetMapping("/status")
    public ResponseEntity<QueueStatusResponse> getStatus(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(lobbyService.getStatus(userId));
    }

    @DeleteMapping("/queue")
    public ResponseEntity<Void> leaveQueue(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "GEN9OU") String format) {
        lobbyService.dequeue(userId, format);
        return ResponseEntity.noContent().build();
    }
}
