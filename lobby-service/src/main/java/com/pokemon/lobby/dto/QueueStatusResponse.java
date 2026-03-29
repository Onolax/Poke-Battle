package com.pokemon.lobby.dto;

public record QueueStatusResponse(String status, String battleId) {
    public static QueueStatusResponse waiting() {
        return new QueueStatusResponse("WAITING", null);
    }
    public static QueueStatusResponse matched(String battleId) {
        return new QueueStatusResponse("MATCHED", battleId);
    }
    public static QueueStatusResponse queued() {
        return new QueueStatusResponse("QUEUED", null);
    }
}
