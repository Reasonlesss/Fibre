package cloud.emilys.fibre.api.container;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Dynamic<T> {

    T get();

    void set(Class<? extends T> tClass);
}
