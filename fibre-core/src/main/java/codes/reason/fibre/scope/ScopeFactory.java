package codes.reason.fibre.scope;

import codes.reason.fibre.blueprint.ScopeBlueprint;
import codes.reason.fibre.identity.Identity;
import codes.reason.fibre.plugin.ScopePlugin;
import java.util.Objects;
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
final class ScopeFactory {

    private ScopeFactory() {}

    @SuppressWarnings("unchecked")
    static Scope create(@Nullable Scope parent, Class<?> type, Consumer<? super ScopeOptions> configure) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(configure, "options must not be null");
        ScopeBlueprint blueprint = ScopeBlueprint.of(type);

        ScopeOptions options = new ScopeOptions();
        configure.accept(options);
        options.end();

        ScopeBuilder builder = new ScopeBuilder(parent);
        options.bindings().forEach((identity, value) -> builder.bind((Identity<Object>) identity, value));

        ScopeInject inject = new ScopeInject(builder);
        for (ScopePlugin plugin : options.plugins()) {
            try {
                plugin.inject(inject);
            } catch (Throwable t) {
                inject.end();
                throw new IllegalStateException("Failed to inject plugin %s".formatted(plugin), t);
            }
        }
        inject.end();

        blueprint.apply(builder);
        Scope scope = builder.build();

        ScopeSetup setup = new ScopeSetup(scope);
        for (ScopePlugin plugin : options.plugins()) {
            try {
                plugin.setup(setup);
            } catch (Throwable t) {
                setup.end();
                // the scope is never entered, so this only closes what the earlier plugins attached
                throw closeAfter(scope, new IllegalStateException("Failed to set up plugin %s".formatted(plugin), t));
            }
        }
        setup.end();

        // before entering, so @Enter methods can already use it
        scope.handle.set(scope);
        scope.entered = true;
        for (Runnable method : scope.enterMethods) {
            try {
                method.run();
            } catch (Throwable t) {
                throw closeAfter(scope, new IllegalStateException("Failed to enter scope", t));
            }
        }
        return scope;
    }

    private static IllegalStateException closeAfter(Scope scope, IllegalStateException exception) {
        try {
            scope.close();
        } catch (Exception e) {
            exception.addSuppressed(e);
        }
        return exception;
    }
}
