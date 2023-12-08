package net.quantrax.citybuild.module.support;

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
import net.quantrax.citybuild.module.support.commands.SupportCommand;
import net.quantrax.citybuild.module.support.discordbot.DiscordBot;
import net.quantrax.citybuild.module.support.discordbot.utils.DiscordWebhook;
import net.quantrax.citybuild.module.support.chat.Chat;
import net.quantrax.citybuild.module.support.player.QueueMember;
import net.quantrax.citybuild.module.support.player.Supporter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class SupportManager extends ListenerAdapter implements Listener {


    public final static String TEMP_CHANNEL_CATEGORY = "1182361623340515489"; // TODO: 07.12.2023 HARDCODE

    public static final String DISCORD_BOT_TOKEN = "MTA3NDA3NjQzMDkzNTI3NzYzOQ.GNK0IM.cpma7-hTEJryDy6_wP7R79jHuz81pAwnAZ2fkc";


    private static SupportManager instance;

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
        this.bot = new DiscordBot(DISCORD_BOT_TOKEN);

        Bukkit.getPluginManager().registerEvents(this, this.plugin);

        CommandFramework.register(new SupportCommand(this.plugin, this));

    }

    public static SupportManager getInstance() {
        return instance;
    }

    public void createChat(QueueMember player, Optional<Supporter> supporter) {
        List<Supporter> sups = List.of();
        supporter.ifPresent(sup -> sups.add(sup));
        Chat chat = new Chat(List.of(player), sups);
        player.setChat(Optional.of(chat));
        supporter.ifPresent(sup -> sup.setOpenChats(Optional.of(chat)));


        CompletableFuture.supplyAsync(() -> {
            String channelName = player.getName() + "-" + chat.getUuid(); // TODO: 07.12.2023 HARDCODE
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


        Component message = getFormat(event.getAuthor().getName(), "DISCORD", Component.text(event.getMessage().getContentRaw())); // TODO: 07.12.2023 HARDCODE
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
                                        chat.isTeamler(source.getUniqueId()) ? "TEAM" : "USER", message)); // TODO: 07.12.2023 HARDCODE

                        if (chat.getWebhook().isEmpty()) return;
                        CompletableFuture.supplyAsync(() -> {
                            DiscordWebhook webhook = new DiscordWebhook(chat.getWebhook().get());
                            webhook.setUserName(source.getName()); // TODO: 07.12.2023 HARDCODE
                            webhook.setContent(PlainTextComponentSerializer.plainText().serialize(event.message())); // TODO: 07.12.2023 HARDCODE
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

    @EventHandler // TODO: 07.12.2023 HARDCODE
    public void onQuit(PlayerQuitEvent e) {
        this.chats.stream().filter(chat -> chat.isInChat(e.getPlayer().getUniqueId())).findFirst()
                .ifPresent(chat -> {
                    chat.close();
                    this.chats.remove(chat);
                });
    }
    private Component getFormat(String name, String rank, Component message) {
        return MiniMessage.miniMessage().deserialize("<red>Support | <rank> <name>  -> <message>", // TODO: 07.12.2023 HARDCODE
                Placeholder.component("name", Component.text(name)),
                Placeholder.component("rank", Component.text(rank)),
                Placeholder.component("message", message));
    }

    private boolean isInAnyChat(Player source) {
        AtomicBoolean isInChat = new AtomicBoolean(false);

        this.chats.stream().filter(chat -> !chat.isClosed()).forEach(chat -> {
            if (chat.allMembers().contains(source)) isInChat.set(true);
        });

        return isInChat.get();
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
