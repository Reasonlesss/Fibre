package codes.reason.fibre.core.scope.discovery;

import codes.reason.fibre.api.fact.Facts;
import codes.reason.fibre.api.scope.ObjectKey;
import codes.reason.fibre.api.scope.binding.Binding;
import codes.reason.fibre.api.scope.lifecycle.ObjectInitializer;
import java.util.List;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record ScopeDefinition(
        ObjectKey key,
        Facts facts,
        Binding binding,
        List<ObjectInitializer> initializers,
        List<ObjectInitializer> postInitializers) {}
