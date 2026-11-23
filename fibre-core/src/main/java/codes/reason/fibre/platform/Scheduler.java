package codes.reason.fibre.platform;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Scheduler {

    PlatformRegistration run(Runnable task);

    PlatformRegistration runLater(long delayTicks, Runnable task);

    PlatformRegistration runRepeating(long delayTicks, long periodTicks, Runnable task);

}
