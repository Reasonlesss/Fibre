package codes.reason.fibre.builtin.event;

import codes.reason.fibre.Fibre;
import codes.reason.fibre.game.GameHolder;
import codes.reason.fibre.game.GameManager;
import codes.reason.fibre.player.PlayerAdapter;
import codes.reason.fibre.platform.PlatformEvents;
import java.lang.ref.WeakReference;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
final class EventOwners {

    private @Nullable WeakReference<Object> event;
    private Origin origin = new Origin(null, null);

    Origin origin(Fibre fibre, Object event) {
        // each handler gets the same event object in turn, so remembering the last one is enough
        WeakReference<Object> last = this.event;
        if (last == null || !last.refersTo(event)) {
            this.origin = resolve(fibre, event);
            this.event = new WeakReference<>(event);
        }
        GameHolder game = this.origin.game();
        return game != null && game.lifecycle().isClosed() ? new Origin(this.origin.player(), null) : this.origin;
    }

    private static Origin resolve(Fibre fibre, Object event) {
        PlatformEvents events = fibre.platform().events();
        GameManager games = fibre.games();
        PlayerAdapter<?> player = events.player(event).orElse(null);
        if (player != null) {
            return new Origin(player, games.findByPlayer(player.id()).orElse(null));
        }
        return new Origin(null, events.position(event).flatMap(games::findByPosition).orElse(null));
    }

    record Origin(@Nullable PlayerAdapter<?> player, @Nullable GameHolder game) {}

}
