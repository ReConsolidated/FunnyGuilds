package net.dzikoysk.funnyguilds.feature.command.user;

import net.dzikoysk.funnycommands.stereotypes.FunnyCommand;
import net.dzikoysk.funnycommands.stereotypes.FunnyComponent;
import net.dzikoysk.funnyguilds.concurrency.requests.prefix.PrefixGlobalRemovePlayerRequest;
import net.dzikoysk.funnyguilds.concurrency.requests.prefix.PrefixGlobalUpdatePlayer;
import net.dzikoysk.funnyguilds.event.FunnyEvent.EventCause;
import net.dzikoysk.funnyguilds.event.SimpleEventHandler;
import net.dzikoysk.funnyguilds.event.guild.member.GuildMemberKickEvent;
import net.dzikoysk.funnyguilds.feature.command.AbstractFunnyCommand;
import net.dzikoysk.funnyguilds.feature.command.CanManage;
import net.dzikoysk.funnyguilds.feature.command.UserValidation;
import net.dzikoysk.funnyguilds.guild.Guild;
import net.dzikoysk.funnyguilds.shared.FunnyFormatter;
import net.dzikoysk.funnyguilds.user.User;
import org.bukkit.entity.Player;

import static net.dzikoysk.funnyguilds.feature.command.DefaultValidation.when;

@FunnyComponent
public final class KickCommand extends AbstractFunnyCommand {

    @FunnyCommand(
            name = "${user.kick.name}",
            description = "${user.kick.description}",
            aliases = "${user.kick.aliases}",
            permission = "funnyguilds.kick",
            completer = "members:3",
            acceptsExceeded = true,
            playerOnly = true
    )
    public void execute(Player player, @CanManage User deputy, Guild guild, String[] args) {
        when(args.length < 1, this.messages.generalNoNickGiven);

        User formerUser = UserValidation.requireUserByName(args[0]);
        when(!formerUser.hasGuild(), this.messages.generalPlayerHasNoGuild);
        when(!guild.equals(formerUser.getGuild().get()), this.messages.kickOtherGuild);
        when(formerUser.isOwner(), this.messages.kickOwner);

        if (!SimpleEventHandler.handle(new GuildMemberKickEvent(EventCause.USER, deputy, guild, formerUser))) {
            return;
        }

        this.concurrencyManager.postRequests(new PrefixGlobalRemovePlayerRequest(this.individualPrefixManager, formerUser.getName()));

        guild.removeMember(formerUser);
        formerUser.removeGuild();

        if (formerUser.isOnline()) {
            this.concurrencyManager.postRequests(new PrefixGlobalUpdatePlayer(this.individualPrefixManager, player));
        }

        FunnyFormatter formatter = new FunnyFormatter()
                .register("{PLAYER}", formerUser.getName())
                .register("{GUILD}", guild.getName())
                .register("{TAG}", guild.getTag());

        deputy.sendMessage(formatter.format(this.messages.kickToOwner));
        this.broadcastMessage(formatter.format(this.messages.broadcastKick));
        formerUser.sendMessage(formatter.format(this.messages.kickToPlayer));
    }

}
