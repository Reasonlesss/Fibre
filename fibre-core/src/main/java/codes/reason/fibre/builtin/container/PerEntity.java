package codes.reason.fibre.builtin.container;

import codes.reason.fibre.entity.EntityAdapter;
import codes.reason.fibre.scope.Handle;
import codes.reason.fibre.scope.Scope;
import codes.reason.fibre.scope.annotations.Exit;
import codes.reason.fibre.scope.annotations.Managed;
import codes.reason.fibre.scope.annotations.Use;
import codes.reason.fibre.scope.identity.Identity;
import codes.reason.fibre.scope.identity.TokenIdentity;
import java.util.Iterator;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Managed(many = true)
public final class PerEntity<T> implements Iterable<T> {

    public static final Identity<EntityAdapter<?>> ENTITY = new TokenIdentity<>() {
    };

    private final ScopedValues<T> values;

    @Use
    PerEntity(Identity<PerEntity<T>> identity, Handle<Scope> owner) {
        this.values = new ScopedValues<>(identity, owner);
    }

    public T get(EntityAdapter<?> entity) {
        return this.values.get(entity.id()).orElseThrow(() -> new IllegalArgumentException(
                "%s isn't tracked by PerEntity<%s>".formatted(entity.id(), this.values.typeName())));
    }

    public void track(EntityAdapter<?> entity) {
        this.values.add(entity.id(), options -> options.bind(ENTITY, entity));
    }

    public void untrack(EntityAdapter<?> entity) {
        this.values.remove(entity.id());
    }

    @Override
    public Iterator<T> iterator() {
        return this.values.all().iterator();
    }

    @Exit
    void exit() {
        this.values.clear();
    }

}
