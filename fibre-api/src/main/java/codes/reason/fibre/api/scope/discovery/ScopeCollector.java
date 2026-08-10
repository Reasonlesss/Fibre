package codes.reason.fibre.api.scope.discovery;

import codes.reason.fibre.api.fact.Facts;
import codes.reason.fibre.api.scope.ObjectKey;
import codes.reason.fibre.api.scope.binding.Binding;
import codes.reason.fibre.api.scope.lifecycle.ObjectInitializer;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ScopeCollector {

    Facts getFactsFor(ObjectKey object);

    void bind(ObjectKey object, Binding binding);

    void initialize(ObjectKey object, ObjectInitializer initializer);

    void postInitialize(ObjectKey object, ObjectInitializer initializer);

    void visit(ObjectKey object);
}
