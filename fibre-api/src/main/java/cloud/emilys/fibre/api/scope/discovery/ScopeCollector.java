package cloud.emilys.fibre.api.scope.discovery;

import cloud.emilys.fibre.api.fact.Facts;
import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.binding.Binding;
import cloud.emilys.fibre.api.scope.lifecycle.ObjectInitializer;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ScopeCollector {

    Facts getFactsFor(ObjectKey object);

    void bind(ObjectKey object, Binding binding);

    void initialize(ObjectKey object, ObjectInitializer initializer);

    void activate(ObjectKey object, ObjectInitializer initializer);

    void postInitialize(ObjectKey object, ObjectInitializer initializer);

    void visit(ObjectKey object);
}
