package codes.reason.fibre.api;

import codes.reason.fibre.api.fact.FactScanner;
import codes.reason.fibre.api.game.PlayerPreloader;
import codes.reason.fibre.api.scope.discovery.ScopeContributor;
import codes.reason.fibre.api.scope.lifecycle.ScopeInitializer;
import codes.reason.fibre.api.type.TypeFinder;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface FibreStartupRegistry {

    void registerPlayerPreloader(PlayerPreloader preloader);

    void registerFactScanner(FactScanner factScanner);

    void registerScopeContributor(ScopeContributor contributor);

    void registerScopeInitializer(ScopeInitializer initializer);

    <S, T> void registerTypeFinder(Class<S> sourceType, Class<T> targetType, TypeFinder<S, T> finder);
}
