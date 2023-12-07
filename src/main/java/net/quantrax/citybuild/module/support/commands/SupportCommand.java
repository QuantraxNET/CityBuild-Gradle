package net.quantrax.citybuild.module.support.commands;

import de.derioo.annotations.CommandProperties;
import de.derioo.interfaces.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@CommandProperties(name = "support", permission = "support.base")
public class SupportCommand extends Command {


    @Override
    public void onNoMappingFound(@NotNull CommandSender sender, String[] args) {
        
    }
}
