package me.faun.givepet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

    public Player getReceiverAsPlayer() {
        return Bukkit.getPlayer(receiver);
    }

    public Player getSenderAsPlayer() {
        return Bukkit.getPlayer(sender);
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
