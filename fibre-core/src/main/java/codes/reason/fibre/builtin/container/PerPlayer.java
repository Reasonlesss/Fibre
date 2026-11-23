package codes.reason.fibre.builtin.container;

import codes.reason.fibre.game.GameEvents;
import codes.reason.fibre.game.GamePlayers;
import codes.reason.fibre.event.EventOrder;
import codes.reason.fibre.event.GamePlayerAddEvent;
import codes.reason.fibre.event.GamePlayerRemoveEvent;
import codes.reason.fibre.player.PlayerAdapter;
import codes.reason.fibre.scope.Handle;
import codes.reason.fibre.scope.Scope;
import codes.reason.fibre.scope.annotations.Enter;
import codes.reason.fibre.scope.annotations.Exit;
import codes.reason.fibre.scope.annotations.Managed;
import codes.reason.fibre.scope.annotations.Use;
import codes.reason.fibre.scope.identity.Identity;
import codes.reason.fibre.scope.identity.TokenIdentity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Managed(many = true)
public final class PerPlayer<T> implements Iterable<T> {

    public static final Identity<PlayerAdapter<?>> PLAYER = new TokenIdentity<>() {
    };

    private final ScopedValues<T> values;
    private final GamePlayers players;
    private final GameEvents events;
    private final List<GameEvents.Registration<?>> registrations = new ArrayList<>();

    @Use
    PerPlayer(Identity<PerPlayer<T>> identity, Handle<Scope> owner, GamePlayers players, GameEvents events) {
        this.values = new ScopedValues<>(identity, owner);
        this.players = players;
        this.events = events;
    }

    public T get(PlayerAdapter<?> player) {
        return this.values.get(player.id()).orElseThrow(() -> new IllegalArgumentException(
                "%s is not in the game of PerPlayer<%s>".formatted(player.username(), this.values.typeName())));
    }

    @Override
    public Iterator<T> iterator() {
        return this.values.all().iterator();
    }

    @Enter
    void enter() {
        this.registrations.add(this.events.register(GamePlayerAddEvent.class, EventOrder.FIRST, event -> this.add(event.player())));
        this.registrations.add(this.events.register(GamePlayerRemoveEvent.class, EventOrder.LAST, event -> this.values.remove(event.player().id())));
        this.players.all().forEach(this::add);
    }

    @Exit
    void exit() {
        for (GameEvents.Registration<?> registration : this.registrations) {
            registration.close();
        }
        this.registrations.clear();
        this.values.clear();
    }

    private void add(PlayerAdapter<?> player) {
        this.values.add(player.id(), options -> options.bind(PLAYER, player));
    }

}
