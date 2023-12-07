package net.quantrax.citybuild.support.discordbot;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.quantrax.citybuild.support.SupportManager;

@Getter
public class DiscordBot {

    private final JDA jda;

    private final Guild guild;


    public DiscordBot(String token) {
        this.jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT) // enables explicit access to message.getContentDisplay()
                .build();
        this.jda.addEventListener(SupportManager.getInstance());

        this.guild = this.jda.getGuilds().get(0);
    }




}
