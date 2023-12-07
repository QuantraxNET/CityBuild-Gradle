package net.quantrax.citybuild.module.support.chat;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.kyori.adventure.audience.Audience;
import net.quantrax.citybuild.support.SupportManager;
import net.quantrax.citybuild.support.player.QueueMember;
import net.quantrax.citybuild.support.player.Supporter;
import net.quantrax.citybuild.utils.DiscordWebhook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
@Setter
public class Chat {

    private final UUID uuid = UUID.randomUUID();
    private final List<QueueMember> members;
    private final List<Supporter> teamlers;
    private Optional<String> webhook = Optional.empty();
    private Optional<Long> discordChannelID = Optional.empty();

    private boolean isClosed = false;

    public Chat(List<QueueMember> members, List<Supporter> teamlers) {
        this.members = new ArrayList<>(members);
        this.teamlers = new ArrayList<>(teamlers);
    }

    public void close() {
        this.isClosed = true;

        this.discordChannelID.ifPresent(id -> {
            TextChannel textChannelById = SupportManager.getInstance().getBot().getGuild().getTextChannelById(id);
            Preconditions.checkArgument(textChannelById != null, "Textchannel ist null - " + id);
            if (this.webhook.isPresent()) {
                CompletableFuture.supplyAsync(() -> {
                    DiscordWebhook webhook = new DiscordWebhook(this.getWebhook().get());
                    webhook.setUserName("SERVER");
                    webhook.setContent("Der User ist gequittet, der Channel wir bald entfernt");
                    try {
                        webhook.execute();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                    textChannelById.delete().queueAfter(5, TimeUnit.SECONDS);


                    return "";
                });
            }
        });


    }

    public boolean isTeamler(UUID uuid) {
        return this.teamlers.stream().map(Supporter::getUuid).toList().contains(uuid);
    }

    public boolean isInChat(UUID uuid) {
        final List<UUID> allMembers = new ArrayList<>(this.members.stream().map(QueueMember::getUuid).toList());
        allMembers.addAll(this.teamlers.stream().map(Supporter::getUuid).toList());

        return allMembers.contains(uuid);
    }

    public boolean shouldSee(Player source) {
        return allMembers().contains(source);
    }

    public Audience allMembersAsForwardingAudience() {
        return Audience.audience(Bukkit.getOnlinePlayers().stream().filter(player -> this.isInChat(player.getUniqueId())).collect(Collectors.toList()));
    }

    public List<Player> allMembers() {
        return Bukkit.getOnlinePlayers().stream().filter(player -> this.isInChat(player.getUniqueId())).collect(Collectors.toList());
    }

    public List<Audience> allMembersAsAudience() {
        return Bukkit.getOnlinePlayers().stream().filter(player -> this.isInChat(player.getUniqueId())).collect(Collectors.toList());
    }
}
