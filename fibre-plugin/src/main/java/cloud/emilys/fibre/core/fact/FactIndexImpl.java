package cloud.emilys.fibre.core.fact;

import cloud.emilys.fibre.api.fact.FactIndex;
import cloud.emilys.fibre.api.fact.FactScanException;
import cloud.emilys.fibre.api.fact.FactScanner;
import cloud.emilys.fibre.api.fact.Facts;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class FactIndexImpl implements FactIndex {

    private final List<FactScanner> scanners;
    private final ClassValue<Facts> cache = new ClassValue<>() {
        @Override
        protected Facts computeValue(Class<?> type) {
            FactScanContextImpl context = new FactScanContextImpl(type);
            for (FactScanner scanner : FactIndexImpl.this.scanners) {
                scanner.collect(context);
            }
            if (!context.isValid()) {
                String details = context.diagnostics.stream()
                        .map(FactDiagnostic::format)
                        .map(message -> " - " + message)
                        .collect(Collectors.joining(System.lineSeparator()));
                throw new FactScanException("Encountered %s issues while loading %s:\n\n%s"
                        .formatted(context.diagnostics.size(), type.getSimpleName(), details));
            }
            return new FactsImpl(context.data);
        }
    };

    public FactIndexImpl(List<? extends FactScanner> scanners) {
        Objects.requireNonNull(scanners, "scanners");
        this.scanners = List.copyOf(scanners);
    }

    @Override
    public Facts get(Class<?> type) {
        Objects.requireNonNull(type, "type");
        return this.cache.get(type);
    }
}
