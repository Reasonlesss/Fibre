package cloud.emilys.fibre.core;

import cloud.emilys.fibre.api.FibreStartupRegistry;
import cloud.emilys.fibre.api.fact.FactScanner;
import cloud.emilys.fibre.api.scope.discovery.ScopeContributor;
import cloud.emilys.fibre.api.scope.lifecycle.ScopeInitializer;
import cloud.emilys.fibre.api.type.TypeFinder;
import cloud.emilys.fibre.core.type.RegisteredTypeFinder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class FibreStartupRegistryImpl implements FibreStartupRegistry {

    private List<FactScanner> factScanners = new ArrayList<>();
    private List<ScopeContributor> scopeContributors = new ArrayList<>();
    private List<ScopeInitializer> scopeInitializers = new ArrayList<>();
    private Map<TypeFinderKey, RegisteredTypeFinder<?, ?>> typeFinders = new LinkedHashMap<>();
    private boolean frozen;

    @Override
    public void registerFactScanner(FactScanner factScanner) {
        this.assertMutable();
        this.factScanners.add(Objects.requireNonNull(factScanner, "factScanner"));
    }

    @Override
    public void registerScopeContributor(ScopeContributor contributor) {
        this.assertMutable();
        this.scopeContributors.add(Objects.requireNonNull(contributor, "contributor"));
    }

    @Override
    public void registerScopeInitializer(ScopeInitializer initializer) {
        this.assertMutable();
        this.scopeInitializers.add(Objects.requireNonNull(initializer, "initializer"));
    }

    @Override
    public <S, T> void registerTypeFinder(Class<S> sourceType, Class<T> targetType, TypeFinder<S, T> finder) {
        this.assertMutable();
        RegisteredTypeFinder<S, T> registration = new RegisteredTypeFinder<>(sourceType, targetType, finder);
        TypeFinderKey key = new TypeFinderKey(sourceType, targetType);
        if (this.typeFinders.putIfAbsent(key, registration) != null) {
            throw new IllegalArgumentException("A type finder is already registered for %s to %s"
                    .formatted(sourceType.getTypeName(), targetType.getTypeName()));
        }
    }

    List<FactScanner> getFactScanners() {
        return List.copyOf(this.factScanners);
    }

    List<ScopeContributor> getScopeContributors() {
        return List.copyOf(this.scopeContributors);
    }

    List<ScopeInitializer> getScopeInitializers() {
        return List.copyOf(this.scopeInitializers);
    }

    List<RegisteredTypeFinder<?, ?>> getTypeFinders() {
        return List.copyOf(this.typeFinders.values());
    }

    public void freeze() {
        if (this.frozen) {
            return;
        }
        this.factScanners = List.copyOf(this.factScanners);
        this.scopeContributors = List.copyOf(this.scopeContributors);
        this.scopeInitializers = List.copyOf(this.scopeInitializers);
        this.typeFinders = Collections.unmodifiableMap(new LinkedHashMap<>(this.typeFinders));
        this.frozen = true;
    }

    private void assertMutable() {
        if (this.frozen) {
            throw new IllegalStateException("Fibre registry is frozen");
        }
    }

    @NullMarked
    private record TypeFinderKey(Class<?> sourceType, Class<?> targetType) {

        private TypeFinderKey {
            Objects.requireNonNull(sourceType, "sourceType");
            Objects.requireNonNull(targetType, "targetType");
        }
    }
}
