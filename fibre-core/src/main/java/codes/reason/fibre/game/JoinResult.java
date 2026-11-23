package codes.reason.fibre.game;

import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public sealed interface JoinResult {

    default boolean joined() {
        return this instanceof Joined;
    }

    record Joined() implements JoinResult {}

    record AlreadyIn() implements JoinResult {}

    // a remove listener of the previous game moved the player somewhere else, and that move won
    record Redirected() implements JoinResult {}

    record Denied(String reason) implements JoinResult {

        public Denied {
            Objects.requireNonNull(reason, "reason must not be null");
        }

    }

}
