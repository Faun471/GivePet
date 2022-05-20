package me.faun.givepet.events;

import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Handler;

public final class PetTransferEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Tameable pet;
    private final Player sender;
    private final Player receiver;

    public PetTransferEvent(@NotNull Tameable pet, @NotNull Player giver , @NotNull Player receiver) {
        this.pet = pet;
        this.sender = giver;
        this.receiver = receiver;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public @NotNull Tameable getPet() {
        return pet;
    }

    public @NotNull Player getSender() {
        return sender;
    }

    public @NotNull Player getReceiver() {
        return receiver;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
