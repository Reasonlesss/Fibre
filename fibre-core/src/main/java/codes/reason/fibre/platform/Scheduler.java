package codes.reason.fibre.platform;

import codes.reason.fibre.Registration;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Scheduler {

    Registration run(Runnable task);

    Registration runLater(long delayTicks, Runnable task);

    Registration runRepeating(long delayTicks, long periodTicks, Runnable task);

}
