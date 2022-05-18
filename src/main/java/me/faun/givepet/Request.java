package me.faun.givepet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Request {
    private final UUID sender;
    private final UUID receiver;
    private boolean accepted;
    private final long time;

    public Request(UUID sender, UUID receiver, long time) {
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

    public boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public long getTime() {
        return time;
    }
}
