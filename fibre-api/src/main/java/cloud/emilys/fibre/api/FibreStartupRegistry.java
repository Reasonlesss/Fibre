package cloud.emilys.fibre.api;

import cloud.emilys.fibre.api.fact.FactScanner;
import cloud.emilys.fibre.api.scope.discovery.ScopeContributor;
import cloud.emilys.fibre.api.scope.lifecycle.ScopeInitializer;
import cloud.emilys.fibre.api.type.TypeFinder;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface FibreStartupRegistry {

    void registerFactScanner(FactScanner factScanner);

    void registerScopeContributor(ScopeContributor contributor);

    void registerScopeInitializer(ScopeInitializer initializer);

    <S, T> void registerTypeFinder(Class<S> sourceType, Class<T> targetType, TypeFinder<S, T> finder);
}
