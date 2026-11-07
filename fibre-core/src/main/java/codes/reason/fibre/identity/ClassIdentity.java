package codes.reason.fibre.identity;

import java.lang.reflect.Type;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class ClassIdentity<T> implements Identity<T> {

    private final @Nullable String name;
    private final Class<T> type;

    public ClassIdentity(Class<T> type) {
        this(null, type);
    }

    @SuppressWarnings("unchecked")
    public ClassIdentity(@Nullable String name, Class<T> type) {
        this.name = IdentityHelper.notBlank(name);
        this.type = (Class<T>) IdentityHelper.box(type);
    }

    @Override
    public Type type() {
        return this.type;
    }

    @Override
    public Class<? super T> rawType() {
        return this.type;
    }

    @Override
    public @Nullable String name() {
        return this.name;
    }

    @Override
    public T cast(Object object) {
        return this.type.cast(object);
    }

    @Override
    public boolean equals(Object o) {
        return IdentityHelper.equals(this, o);
    }

    @Override
    public int hashCode() {
        return IdentityHelper.hashCode(this);
    }

    @Override
    public String toString() {
        return IdentityHelper.toString(this);
    }
}
