package net.quantrax.citybuild.module.support.discordbot;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.quantrax.citybuild.module.support.SupportManager;

@Getter
public class DiscordBot {

    private final JDA jda;

    private final Guild guild;


    public DiscordBot(String token) {
        try {
            this.jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT) // enables explicit access to message.getContentDisplay()
                    .build().awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.jda.addEventListener(SupportManager.getInstance());

        this.guild = this.jda.getGuildById("1126216975375401021");


    }




}
