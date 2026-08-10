package codes.reason.fibre.api.fact;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface FactScanner {

    void collect(FactScanContext context);
}
