package net.quantrax.citybuild.support.commands;

import de.derioo.annotations.CommandProperties;
import de.derioo.annotations.Mapping;
import de.derioo.annotations.Possibilities;
import de.derioo.interfaces.Command;
import de.derioo.objects.CommandBody;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.quantrax.citybuild.support.SupportManager;
import net.quantrax.citybuild.support.player.QueueMember;
import net.quantrax.citybuild.support.player.Supporter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;


@CommandProperties(name = "support", permission = SupportCommand.BASE_PERMISSION)
public class SupportCommand extends Command {

    public static final String BASE_PERMISSION = "support.base";

    private final JavaPlugin plugin;
    private final SupportManager manager;
    SupportManager.PlayerGetter<QueueMember> queueManager;

    SupportManager.PlayerGetter<Supporter> teamManager;

    public SupportCommand(JavaPlugin plugin, SupportManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.queueManager = manager.getQueueMemberManager();
        this.teamManager = manager.getSupportManager();
    }


    @Override
    public void onNoMappingFound(@NotNull CommandSender sender, String[] args) {
        this.onNoMappingFound((Player) sender, args);
    }

    @Override
    public void onNoMappingFound(@NotNull Player player, String[] args) {
        if (!player.hasPermission(SupportCommand.BASE_PERMISSION)) {
            //No permission
            player.sendMessage(MiniMessage.miniMessage().deserialize("Ey du keine Rechte"));// TODO: 07.12.2023
            return;
        }
        //Format wrong
        player.sendMessage(MiniMessage.miniMessage().deserialize("Ey bitte nutz mal den command richtig"));
    }

    @Mapping(args = "join queue")
    public void joinQueue(CommandBody body) {

        QueueMember player = this.queueManager.getPlayer(body.player().getUniqueId());
        if (player == null) {
            player = this.queueManager.createPlayer(body.player().getUniqueId());
        }
        if (this.manager.getQueue().contains(player)) {
            //Already joined
            body.player().sendMessage(MiniMessage.miniMessage().deserialize("Ey du bist schon queue"));
            return;
        }
        body.player().sendMessage(MiniMessage.miniMessage().deserialize("Du bist nun in der queue"));
        this.manager.getQueue().add(player);
        this.manager.createChat(player, Optional.empty());
        player.getChat().orElseThrow().getMembers().remove(player);
    }

    @Mapping(args = "leave queue")
    public void leaveQueue(CommandBody body) {

        QueueMember player = this.queueManager.getPlayer(body.player().getUniqueId());
        if (player == null) {
            player = this.queueManager.createPlayer(body.player().getUniqueId());
        }
        if (!this.manager.getQueue().contains(player)) {
            //Not even in queue
            body.player().sendMessage(MiniMessage.miniMessage().deserialize("Ey du bist gar nicht queue"));
            return;
        }
        body.player().sendMessage(MiniMessage.miniMessage().deserialize("Du bist nun nicht mehr in der queue"));
        this.manager.getQueue().remove(player);
    }

    @Mapping(args = "leave chat")
    public void leaveChat(CommandBody body) {

        QueueMember player = this.queueManager.getPlayer(body.player().getUniqueId());
        if (player == null) {
            player = this.queueManager.createPlayer(body.player().getUniqueId());
        }
        if (player.getChat().isEmpty()) {
            //Doesnt have a chat
            body.player().sendMessage(MiniMessage.miniMessage().deserialize("Ey du hast ja gar keinen chat"));
            return;
        }
        if (!player.getChat().get().getMembers().contains(player)) {
            //Is not in his chat
            body.player().sendMessage(MiniMessage.miniMessage().deserialize("Ey du bist gar nicht in deinem chat du "));
            return;
        }
        body.player().sendMessage(MiniMessage.miniMessage().deserialize("Du siehst jetzt nicht mehr deinen chat"));
        player.getChat().get().close();
        player.getChat().get().getMembers().remove(player);
    }

    @Mapping(args = "join chat")
    public void joinChat(CommandBody body) {

        QueueMember player = this.queueManager.getPlayer(body.player().getUniqueId());
        if (player == null) {
            player = this.queueManager.createPlayer(body.player().getUniqueId());
        }
        if (player.getChat().isPresent()) {
            //Has chat
            body.player().sendMessage(MiniMessage.miniMessage().deserialize("Ey du hast ja gar keinen chat"));
            return;
        }
        body.player().sendMessage(MiniMessage.miniMessage().deserialize("Du siehst jetzt deinen chat"));
        player.getChat().orElseThrow().getMembers().add(player);
    }

    @Mapping(args = "support join", extraPermission = true, permission = "support.supporter")
    public void joinSupport(CommandBody body) {
        Supporter supporter = this.teamManager.getPlayer(body.player().getUniqueId());
        if (supporter == null) {
            supporter = this.teamManager.createPlayer(body.player().getUniqueId());
        }
        if (this.manager.getActiveTeam().contains(supporter)) {
            //Already joined
            body.player().sendMessage(MiniMessage.miniMessage().deserialize("Ey du bist schon gejoint"));
            return;
        }
        this.manager.getActiveTeam().add(supporter);
        body.player().sendMessage(MiniMessage.miniMessage().deserialize("Ey du bist nun gejoint"));
    }

    @Mapping(args = "support leave", extraPermission = true, permission = "support.supporter")
    public void leaveSupport(CommandBody body) {
        Supporter supporter = this.teamManager.getPlayer(body.player().getUniqueId());
        if (supporter == null) {
            supporter = this.teamManager.createPlayer(body.player().getUniqueId());
        }
        if (!this.manager.getActiveTeam().contains(supporter)) {
            //Already joined
            body.player().sendMessage(MiniMessage.miniMessage().deserialize("Ey du bist nicht gejoint"));
            return;
        }
        this.manager.getActiveTeam().remove(supporter);
        body.player().sendMessage(MiniMessage.miniMessage().deserialize("Ey du bist nun geleaved"));
    }

    @Mapping(args = "support list", extraPermission = true, permission = "support.supporter")
    public void listSupports(CommandBody body) {
        body.player().sendMessage(MiniMessage.miniMessage().deserialize("Hier sind alle QueuePlayer"));
        this.manager.getQueue().forEach(player -> {
            //Format: <players> <joinDiff> <id> (Chat id if present)
            body.player().sendMessage(MiniMessage.miniMessage().deserialize(
                    "<click:copy_to_clipboard:<id>><player> -> <joindiff> ms (Klicke um die Chat ID zu kopieren)</click>" +
                            " Klicke <click:run_command:/support joinchat <id>><gold>hier</gold</click> um dem Chat zu joinen",
                    Placeholder.component("player", Component.text(player.getName())),
                    Placeholder.component("joindiff", Component.text(System.currentTimeMillis() - player.getQueueJoinTime())),
                    Placeholder.parsed("id", (player.getChat().isPresent() ?
                            player.getChat().get().getUuid().toString() :
                            "There is not chat of the player"))));
        });
    }

    @Mapping(args = "joinchat {id}", extraPermission = true, permission = "support.supporter")
    @Possibilities(args = "{id}->~all~")
    public void joinChatAsSupporter(CommandBody body) {
        Supporter supporter = this.teamManager.getPlayer(body.player().getUniqueId());
        if (supporter == null) {
            supporter = this.teamManager.createPlayer(body.player().getUniqueId());
        }

        if (!this.manager.getActiveTeam().contains(supporter)) {
            //Already joined
            body.player().sendMessage(MiniMessage.miniMessage().deserialize("Ey du bist nicht in den support gejoint"));
            return;
        }


        UUID chatID;
        try {
            chatID = UUID.fromString(body.get("id"));
        } catch (Exception e) {
            //Invaild UUID
            body.player().sendMessage(MiniMessage.miniMessage().deserialize("Dings das keine richtige UUID"));
            return;
        }

        Optional<QueueMember> optionalQueueMember = this.manager.getQueue().stream().filter(player -> player.getChat().isPresent() && !player.getChat().get().isClosed() && player.getChat().get().getUuid().equals(chatID)).findAny();
        if (optionalQueueMember.isEmpty()) {
            //uuid doesnt "exists"
            body.player().sendMessage(MiniMessage.miniMessage().deserialize("Ey dings ehm dieser Chat exestiert nicht"));
            return;
        }
        optionalQueueMember.get().getChat().orElseThrow().getTeamlers().add(supporter);
        body.player().sendMessage(MiniMessage.miniMessage().deserialize("Du bist dem Chat gejoint"));
    }
}
