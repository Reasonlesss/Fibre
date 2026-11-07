package codes.reason.fibre.plugin;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract class TokenAttachment<T> implements Attachment<T> {

    private final Class<?> owner;
    private final Type type;
    private final Class<? super T> rawType;
    private final @Nullable String name;

    protected TokenAttachment() {
        this(null);
    }

    @SuppressWarnings("unchecked")
    protected TokenAttachment(@Nullable String name) {
        this.owner = AttachmentHelper.caller();
        this.name = AttachmentHelper.notBlank(name);
        Type superclass = this.getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType parameterized)
                || parameterized.getRawType() != TokenAttachment.class) {
            throw new IllegalStateException(
                    "TokenAttachment must be created as a direct anonymous subclass, e.g. new TokenAttachment<List<String>>() {}");
        }
        this.type = AttachmentHelper.concrete(parameterized.getActualTypeArguments()[0]);
        this.rawType = (Class<? super T>) AttachmentHelper.erase(this.type);
    }

    @Override
    public final Class<?> owner() {
        return this.owner;
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
        return AttachmentHelper.equals(this, o);
    }

    @Override
    public final int hashCode() {
        return AttachmentHelper.hashCode(this);
    }

    @Override
    public final String toString() {
        return AttachmentHelper.toString(this);
    }
}
