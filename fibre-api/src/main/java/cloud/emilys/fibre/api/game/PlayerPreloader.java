package cloud.emilys.fibre.api.game;

import org.jspecify.annotations.NullMarked;

@FunctionalInterface
@NullMarked
public interface PlayerPreloader {

    void preload(PlayerJoinToken token);
}
