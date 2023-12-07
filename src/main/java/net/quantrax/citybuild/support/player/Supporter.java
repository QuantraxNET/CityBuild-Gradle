package net.quantrax.citybuild.support.player;

import lombok.Getter;
import lombok.Setter;
import net.quantrax.citybuild.support.chat.Chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Setter
@Getter
public class Supporter {

    private final UUID uuid;
    private final long joinTime;
    private Optional<Chat> openChats = Optional.empty();

    public Supporter(UUID uuid, long joinTime) {
        this.uuid = uuid;
        this.joinTime = joinTime;
    }
}
