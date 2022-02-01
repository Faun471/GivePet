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
    private Tameable pet;
    private Player giver;
    private Player receiver;

    public PetTransferEvent() {

    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean cancel) {

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
