package net.dzikoysk.funnyguilds.event.guild;

import net.dzikoysk.funnyguilds.guild.Guild;
import net.dzikoysk.funnyguilds.user.User;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GuildBanEvent extends GuildEvent {

    private static final HandlerList handlers = new HandlerList();
    private final long time;
    private final String reason;

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public GuildBanEvent(EventCause eventCause, User doer, Guild guild, long time, String reason) {
        super(eventCause, doer, guild);
        this.time = time;
        this.reason = reason;
    }

    public long getTime() {
        return this.time;
    }

    public String getReason() {
        return this.reason;
    }

    @Override
    public String getDefaultCancelMessage() {
        return "[FunnyGuilds] Guild ban has been cancelled by the server!";
    }

}
