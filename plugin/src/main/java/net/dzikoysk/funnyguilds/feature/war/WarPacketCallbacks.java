package net.dzikoysk.funnyguilds.feature.war;

import net.dzikoysk.funnyguilds.FunnyGuilds;
import net.dzikoysk.funnyguilds.concurrency.requests.war.WarAttackRequest;
import net.dzikoysk.funnyguilds.concurrency.requests.war.WarInfoRequest;
import net.dzikoysk.funnyguilds.nms.api.packet.PacketCallbacks;
import net.dzikoysk.funnyguilds.user.User;

public class WarPacketCallbacks implements PacketCallbacks {

    private final FunnyGuilds plugin;
    private final User user;

    public WarPacketCallbacks(FunnyGuilds plugin, User user) {
        this.plugin = plugin;
        this.user = user;
    }

    @Override
    public void handleRightClickEntity(int entityId, boolean isMainHand) {
        this.plugin.getConcurrencyManager().postRequests(
                new WarInfoRequest(this.plugin, this.plugin.getGuildEntityHelper(), this.user, entityId)
        );
    }

    @Override
    public void handleAttackEntity(int entityId, boolean isMainHand) {
        if (!isMainHand) {
            return;
        }

        this.plugin.getConcurrencyManager().postRequests(
                new WarAttackRequest(this.plugin.getFunnyServer(), this.plugin.getGuildEntityHelper(), this.user, entityId)
        );
    }

}
