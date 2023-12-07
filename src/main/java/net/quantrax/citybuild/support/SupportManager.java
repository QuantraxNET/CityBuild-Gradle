package net.quantrax.citybuild.support;

import de.derioo.manager.CommandFramework;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.quantrax.citybuild.support.chat.Chat;
import net.quantrax.citybuild.support.commands.SupportCommand;
import net.quantrax.citybuild.support.discordbot.DiscordBot;
import net.quantrax.citybuild.support.player.QueueMember;
import net.quantrax.citybuild.support.player.Supporter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class SupportManager extends ListenerAdapter implements Listener {


    public final static String TEMP_CHANNEL_CATEGORY = "1182361623340515489";

    public static final String DISCORD_BOT_TOKEN = "MTA3NDA3NjQzMDkzNTI3NzYzOQ.G1cyrl.lqU8eO2cwf6idC8k_DxGrCZcOWB9zW4mcwbZ2Y";


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


        //CommandFramework.register(new SupportCommand());
    }

    public static SupportManager getInstance() {
        return instance;
    }

    public void createChat(QueueMember player, Supporter supporter) {
        System.out.println("TEst");

        Chat chat = new Chat(List.of(player), List.of(supporter));
        player.setChat(Optional.of(chat));
        supporter.getOpenChats().add(chat);


        AtomicLong id = new AtomicLong();

        this.bot.getGuild().createTextChannel(player.getName() + "-" + chat.getUuid(),
                this.bot.getGuild().getCategoryById(SupportManager.TEMP_CHANNEL_CATEGORY)).queue(channel -> {
            id.set(channel.getIdLong());
        });

        chat.setDiscordChannelID(Optional.of(id.get()));


        this.chats.add(chat);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getChannelType().equals(ChannelType.TEXT)) return;
        long id = event.getChannel().getIdLong();
        Chat chat = this.chats.stream().filter(c -> !c.isClosed() && c.getDiscordChannelID().isPresent()
                && c.getDiscordChannelID().get() == id).findFirst().orElse(null);
        if (chat == null) return;

        chat.allMembersAsForwardingAudience()
                .sendMessage(getFormat(event.getAuthor().getName(), Component.text(event.getMessage().getContentRaw())));

    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Set<Audience> viewers = event.viewers();
        Player source = event.getPlayer();

        if (!this.isInAnyChat(source)) return;

        viewers.clear();

        this.chats.stream()
                .filter(chat -> !chat.isClosed())
                .forEach(chat -> {
                    if (chat.shouldSee(source)) {
                        viewers.add(chat.allMembersAsForwardingAudience());
                    }
                });

        event.renderer((source1, sourceDisplayName, message, viewer) -> getFormat(source.getName(), message));

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        this.createChat(this.queueMemberManager.createPlayer(e.getPlayer().getUniqueId()), this.supportManager.createPlayer(e.getPlayer().getUniqueId()));
    }

    private Component getFormat(String name, Component message) {
        return MiniMessage.miniMessage().deserialize("<red>Support | <name> -> <message>",
                Placeholder.component("name", Component.text(name)),
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
