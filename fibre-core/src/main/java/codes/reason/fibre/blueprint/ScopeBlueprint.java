package codes.reason.fibre.blueprint;

import codes.reason.fibre.scope.ScopeBuilder;

public interface ScopeBlueprint {

    void apply(ScopeBuilder builder);

    static ScopeBlueprint of(Class<?> type) {
        return ScopeBlueprints.BLUEPRINTS.get(type);
    }
}
