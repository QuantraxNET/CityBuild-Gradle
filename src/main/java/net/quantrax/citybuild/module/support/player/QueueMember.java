package net.quantrax.citybuild.module.support.player;

import lombok.Getter;
import lombok.Setter;
import net.quantrax.citybuild.module.support.chat.Chat;

import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
public class QueueMember {

    private final UUID uuid;
    private final long queueJoinTime;

    private Optional<Chat> chat = Optional.empty();

    public QueueMember(UUID uuid, long queueJoinTime) {
        this.uuid = uuid;
        this.queueJoinTime = queueJoinTime;
    }

    public boolean isWaiting() {
        return this.chat.isEmpty();
    }

    public String getName() {
       return "Derio"; // TODO: 07.12.2023 HARDCODE
    }

}
