package me.faun.givepet.events;

import me.faun.givepet.request.Request;
import me.faun.givepet.request.State;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class PetRequestEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final State state;
    private final Request request;

    public PetRequestEvent(@NotNull Request request, @NotNull State state) {
        this.state = state;
        this.request = request;
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

    public @NotNull State getState() {
        return state;
    }

    public @NotNull Request getRequest() {
        return request;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
