package codes.reason.fibre.plugin;

import java.lang.reflect.Type;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class ClassAttachment<T> implements Attachment<T> {

    private final Class<?> owner;
    private final @Nullable String name;
    private final Class<T> type;

    public ClassAttachment(Class<T> type) {
        this(null, type);
    }

    @SuppressWarnings("unchecked")
    public ClassAttachment(@Nullable String name, Class<T> type) {
        this.owner = AttachmentHelper.caller();
        this.name = AttachmentHelper.notBlank(name);
        this.type = (Class<T>) AttachmentHelper.box(type);
    }

    @Override
    public Class<?> owner() {
        return this.owner;
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
        return AttachmentHelper.equals(this, o);
    }

    @Override
    public int hashCode() {
        return AttachmentHelper.hashCode(this);
    }

    @Override
    public String toString() {
        return AttachmentHelper.toString(this);
    }
}
