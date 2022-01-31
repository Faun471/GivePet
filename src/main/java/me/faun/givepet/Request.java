package me.faun.givepet;

import java.util.UUID;

public class Request {
    private final UUID sender;
    private final UUID receiver;
    private String accepted = "pending";
    private final String time;

    public Request(UUID sender, UUID receiver, String time) {
        this.sender = sender;
        this.receiver = receiver;
        this.time = time;
    }

    public UUID getSender() {
        return sender;
    }

    public UUID getReceiver() {
        return receiver;
    }

    public String getAccepted() {
        return accepted;
    }

    public void setAccepted(String accepted) {
        this.accepted = accepted;
    }

    public String getTime() {
        return time;
    }
}
