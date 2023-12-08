package net.quantrax.citybuild.module.support;

import com.google.common.util.concurrent.AtomicDouble;
import de.derioo.manager.CommandFramework;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.quantrax.citybuild.config.Config;
import net.quantrax.citybuild.module.support.chat.Chat;
import net.quantrax.citybuild.module.support.commands.SupportCommand;
import net.quantrax.citybuild.module.support.discordbot.DiscordBot;
import net.quantrax.citybuild.module.support.player.QueueMember;
import net.quantrax.citybuild.module.support.player.Supporter;
import net.quantrax.citybuild.module.support.discordbot.utils.DiscordWebhook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class SupportManager extends ListenerAdapter implements Listener, Config {


    public static String TEMP_CHANNEL_CATEGORY;

    public static String DISCORD_BOT_TOKEN;


    private static SupportManager instance;


    private final UUID configID;
    private final DiscordBot bot;

    @NotNull
    private final JavaPlugin plugin;
    @NotNull
    private final List<QueueMember> queue = new ArrayList<>();
    @NotNull
    private final List<Supporter> activeTeam = new ArrayList<>();
    @NotNull
    private final List<Chat> chats = new ArrayList<>();

    @NotNull
    private final PlayerGetter<QueueMember> queueMemberManager = new PlayerGetter<>(QueueMember::getUuid, QueueMember::new);
    @NotNull
    private final PlayerGetter<Supporter> supportManager = new PlayerGetter<>(Supporter::getUuid, Supporter::new);

    public SupportManager(@NotNull JavaPlugin plugin) {
        instance = this;

        this.plugin = plugin;
        this.configID = this.loadConfig(null, "config.yml", plugin);

        SupportManager.TEMP_CHANNEL_CATEGORY = this.get("support-system.discord.temp-channel-category-id").toString();
        SupportManager.DISCORD_BOT_TOKEN = this.get("support-system.discord.token").toString();

        this.bot = new DiscordBot(DISCORD_BOT_TOKEN, this.get("support-system.discord.guild-id").toString());

        Bukkit.getPluginManager().registerEvents(this, this.plugin);


    }

    public static SupportManager getInstance() {
        return instance;
    }

    public void createChat(QueueMember player, Supporter supporter) {

        Chat chat = new Chat(List.of(player), List.of(supporter));
        player.setChat(Optional.of(chat));
        supporter.getOpenChats().add(chat);


        CompletableFuture.supplyAsync(() -> {
            String channelName = get("support-syste.discord.discord-channel-name").toString()
                    .replace("<player>", player.getName())
                    .replace("<id>", chat.getUuid().toString());
            TextChannel channel = this.bot.getGuild().createTextChannel(channelName,
                    this.bot.getGuild().getCategoryById(SupportManager.TEMP_CHANNEL_CATEGORY)).complete();


            chat.setDiscordChannelID(Optional.of(channel.getIdLong()));

            chat.setWebhook(Optional.of(channel.createWebhook("webhook").complete().getUrl()));

            this.chats.add(chat);

            return "";
        });
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getChannelType().equals(ChannelType.TEXT) || event.getAuthor().isBot()) return;
        long id = event.getChannel().getIdLong();
        Chat chat = this.chats.stream().filter(c -> !c.isClosed() && c.getDiscordChannelID().isPresent()
                && c.getDiscordChannelID().get() == id).findFirst().orElse(null);


        if (chat == null) return;


        Component message = getFormat(event.getAuthor().getName(), get("support-system.ranks.discord").toString(), Component.text(event.getMessage().getContentRaw()));
        chat.allMembersAsForwardingAudience()
                .sendMessage(message);

    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Set<Audience> viewers = event.viewers();
        Player source = event.getPlayer();

        if (!this.isInAnyChat(source)) return;

        viewers.clear();

        this.chats.stream()
                .filter(chat -> !chat.isClosed() && chat.isInChat(source.getUniqueId()))
                .findFirst().ifPresent(chat -> {
                    if (chat.shouldSee(source)) {
                        viewers.add(chat.allMembersAsForwardingAudience());

                        event.renderer((source1, sourceDisplayName, message, viewer) ->
                                getFormat(source.getName(),
                                        chat.isTeamler(source.getUniqueId()) ? get("support-system.ranks.team").toString() : get("support-system.ranks.user").toString(), message));

                        if (chat.getWebhook().isEmpty()) return;
                        CompletableFuture.supplyAsync(() -> {
                            DiscordWebhook webhook = new DiscordWebhook(chat.getWebhook().get());
                            webhook.setUserName(PlainTextComponentSerializer.plainText().serialize(
                                    getMessage("support-system-discord.minecraft-integration.discord.webhook-name" +
                                            source.getName(), event.message())
                            ));
                            webhook.setContent(PlainTextComponentSerializer.plainText().serialize(
                                    getMessage("support-system-discord.minecraft-integration.discord.webhook-content" +
                                            source.getName(), event.message())
                            ));
                            try {
                                webhook.execute();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return "";
                        });
                    }
                });


    }


    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        this.chats.stream().filter(chat -> chat.isInChat(e.getPlayer().getUniqueId())).findFirst()
                .ifPresent(chat -> {
                    chat.close();
                    this.chats.remove(chat);
                });
    }

    private Component getFormat(String name, String rank, Component message) {
        return getMessage("support-system.discord.minecraft-integration.minecraft.format", name, rank, message);
    }

    private boolean isInAnyChat(Player source) {
        AtomicBoolean isInChat = new AtomicBoolean(false);

        this.chats.stream().filter(chat -> !chat.isClosed()).forEach(chat -> {
            if (chat.allMembers().contains(source)) isInChat.set(true);
        });

        return isInChat.get();
    }

    @Override
    public UUID getUUID() {
        return this.configID;
    }

    public static class PlayerGetter<T> {

        @Getter
        private final List<T> players = new ArrayList<T>();
        private final CustomSupplier<UUID, T> supplier;
        private final BiCustomSupplier<T, UUID, Long> constructor;

        public PlayerGetter(CustomSupplier<UUID, T> supplier, BiCustomSupplier<T, UUID, Long> constructor) {
            this.supplier = supplier;
            this.constructor = constructor;
        }


        @Nullable
        public T getPlayer(UUID uuid) {
            return this.players.stream().filter(queueMember -> supplier.get(queueMember).equals(uuid)).findFirst().orElse(null);
        }

        @NotNull
        public T createPlayer(UUID uuid) {
            T player = this.constructor.get(uuid, System.currentTimeMillis());

            this.players.add(player);


            return player;
        }

    }

    @FunctionalInterface
    public interface CustomSupplier<R, A> {

        R get(A argument);


    }


    @FunctionalInterface
    public interface BiCustomSupplier<R, A, B> {

        R get(A argument, B argument2);


    }


}
