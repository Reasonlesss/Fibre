package cloud.emilys.fibre.api.fact;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface FactIndex {

    Facts get(Class<?> type);
}
