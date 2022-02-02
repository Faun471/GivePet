package me.faun.givepet.Events;

import me.faun.givepet.PetManager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class PetTransferEvent extends Event implements Cancellable {

    PetManager petManager = new PetManager();

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final Tameable pet;
    private final Player giver;
    private final Player receiver;

    public PetTransferEvent(@NotNull Tameable pet, @NotNull Player giver , @NotNull Player receiver) {
        this.pet = pet;
        this.giver = giver;
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

    public Tameable getPet() {
        return pet;
    }

    public Player getGiver() {
        return giver;
    }

    public Player getReceiver() {
        return receiver;
    }
}
