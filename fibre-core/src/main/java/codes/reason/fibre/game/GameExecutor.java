package codes.reason.fibre.game;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class GameExecutor {

    private final Logger logger;

    GameExecutor(Logger logger) {
        this.logger = logger;
    }

    // false if the task threw, which is logged
    public boolean execute(Task task) {
        Objects.requireNonNull(task, "task must not be null");
        try {
            task.run();
            return true;
        } catch (Throwable t) {
            this.logger.log(Level.SEVERE, "An exception was thrown while executing a game task", t);
            return false;
        }
    }

    @FunctionalInterface
    public interface Task {

        void run() throws Throwable;

    }

}
