package net.dzikoysk.funnyguilds.feature.command.user;

import net.dzikoysk.funnycommands.stereotypes.FunnyCommand;
import net.dzikoysk.funnycommands.stereotypes.FunnyComponent;
import net.dzikoysk.funnyguilds.FunnyGuilds;
import net.dzikoysk.funnyguilds.guild.Guild;
import net.dzikoysk.funnyguilds.user.User;
import net.dzikoysk.funnyguilds.user.UserCache;
import net.dzikoysk.funnyguilds.feature.command.IsMember;
import net.dzikoysk.funnyguilds.config.MessageConfiguration;
import net.dzikoysk.funnyguilds.config.PluginConfiguration;
import net.dzikoysk.funnyguilds.shared.bukkit.ItemUtils;
import net.dzikoysk.funnyguilds.shared.bukkit.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static net.dzikoysk.funnyguilds.feature.command.DefaultValidation.when;

@FunnyComponent
public final class BaseCommand {

    @FunnyCommand(
        name = "${user.base.name}",
        aliases = "${user.base.aliases}",
        description = "${user.base.description}",
        permission = "funnyguilds.base",
        acceptsExceeded = true,
        playerOnly = true
    )
    public void execute(PluginConfiguration config, MessageConfiguration messages, Player player, @IsMember User user, Guild guild) {
        when (!config.regionsEnabled, messages.regionsDisabled);
        when (!config.baseEnable, messages.baseTeleportationDisabled);
        when (user.getCache().getTeleportation() != null, messages.baseIsTeleportation);

        List<ItemStack> requiredItems = player.hasPermission("funnyguilds.vip.base")
                ? Collections.emptyList()
                : config.baseItems;

        if (!ItemUtils.playerHasEnoughItems(player, requiredItems)) {
            return;
        }

        ItemStack[] items = ItemUtils.toArray(requiredItems);
        player.getInventory().removeItem(items);

        if (config.baseDelay.isZero()) {
            player.teleport(guild.getHome());
            player.sendMessage(messages.baseTeleport);
            return;
        }

        Duration time = player.hasPermission("funnyguilds.vip.baseTeleportTime")
                ? config.baseDelayVip
                : config.baseDelay;

        Location before = player.getLocation();
        Instant teleportStart = Instant.now();
        UserCache cache = user.getCache();

        cache.setTeleportation(Bukkit.getScheduler().runTaskTimer(FunnyGuilds.getInstance(), () -> {
            if (!player.isOnline()) {
                cache.getTeleportation().cancel();
                cache.setTeleportation(null);
                return;
            }
            
            if (!LocationUtils.equals(player.getLocation(), before)) {
                cache.getTeleportation().cancel();
                player.sendMessage(messages.baseMove);
                cache.setTeleportation(null);
                player.getInventory().addItem(items);
                return;
            }

            if (Duration.between(teleportStart, Instant.now()).compareTo(time) > 0) {
                cache.getTeleportation().cancel();
                player.sendMessage(messages.baseTeleport);
                player.teleport(guild.getHome());
                cache.setTeleportation(null);
            }
        }, 0L, 10L));

        player.sendMessage(messages.baseDontMove.replace("{TIME}", Long.toString(time.getSeconds())));
    }

}