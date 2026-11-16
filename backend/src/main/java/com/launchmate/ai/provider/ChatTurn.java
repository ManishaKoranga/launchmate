package com.launchmate.ai.provider;

public record ChatTurn(Role role, String text) {

    public enum Role { USER, ASSISTANT }

    public static ChatTurn user(String text) {
        return new ChatTurn(Role.USER, text);
    }

    public static ChatTurn assistant(String text) {
        return new ChatTurn(Role.ASSISTANT, text);
    }
}
