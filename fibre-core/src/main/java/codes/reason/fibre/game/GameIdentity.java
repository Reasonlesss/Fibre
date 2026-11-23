package codes.reason.fibre.game;

import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record GameIdentity(UUID id, Class<?> type) {

    public GameIdentity {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(type, "type must not be null");
    }

    static GameIdentity random(Class<?> type) {
        return new GameIdentity(UUID.randomUUID(), type);
    }

    @Override
    public String toString() {
        return "%s[%s]".formatted(this.type.getSimpleName(), this.id);
    }

}
