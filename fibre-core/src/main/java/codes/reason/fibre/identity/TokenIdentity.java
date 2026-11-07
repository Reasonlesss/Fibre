package codes.reason.fibre.identity;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract class TokenIdentity<T> implements Identity<T> {

    private final Type type;
    private final Class<? super T> rawType;
    private final @Nullable String name;

    protected TokenIdentity() {
        this(null);
    }

    @SuppressWarnings("unchecked")
    protected TokenIdentity(@Nullable String name) {
        this.name = IdentityHelper.notBlank(name);
        Type superclass = this.getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType parameterized)
                || parameterized.getRawType() != TokenIdentity.class) {
            throw new IllegalStateException(
                    "TokenIdentity must be created as a direct anonymous subclass, e.g. new TokenIdentity<List<String>>() {}");
        }
        this.type = IdentityHelper.concrete(parameterized.getActualTypeArguments()[0]);
        this.rawType = (Class<? super T>) IdentityHelper.erase(this.type);
    }

    @Override
    public final Type type() {
        return this.type;
    }

    @Override
    public final Class<? super T> rawType() {
        return this.rawType;
    }

    @Override
    public final @Nullable String name() {
        return this.name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final T cast(Object object) {
        return (T) this.rawType.cast(object);
    }

    @Override
    public final boolean equals(Object o) {
        return IdentityHelper.equals(this, o);
    }

    @Override
    public final int hashCode() {
        return IdentityHelper.hashCode(this);
    }

    @Override
    public final String toString() {
        return IdentityHelper.toString(this);
    }
}
