package com.pokemon.user.client;

public class GameDataUnavailableException extends RuntimeException {
    public GameDataUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
