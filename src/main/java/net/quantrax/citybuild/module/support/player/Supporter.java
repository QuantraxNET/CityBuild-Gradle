package net.quantrax.citybuild.module.support.player;

import lombok.Getter;
import net.quantrax.citybuild.module.support.chat.Chat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Supporter {

    private final UUID uuid;
    private final long joinTime;
    private final List<Chat> openChats = new ArrayList<>();

    public Supporter(UUID uuid, long joinTime) {
        this.uuid = uuid;
        this.joinTime = joinTime;
    }
}
