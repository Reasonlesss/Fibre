package cloud.emilys.fibre.core.scope.discovery;

import cloud.emilys.fibre.api.fact.Facts;
import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.binding.Binding;
import cloud.emilys.fibre.api.scope.lifecycle.ObjectInitializer;
import java.util.List;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record ScopeDefinition(
        ObjectKey key,
        Facts facts,
        Binding binding,
        List<ObjectInitializer> initializers,
        List<ObjectInitializer> activators,
        List<ObjectInitializer> postInitializers) {}
