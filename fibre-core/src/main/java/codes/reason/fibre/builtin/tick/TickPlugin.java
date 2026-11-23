package codes.reason.fibre.builtin.tick;

import codes.reason.fibre.Fibre;
import codes.reason.fibre.game.GameExecutor;
import codes.reason.fibre.plugin.ScopePlugin;
import codes.reason.fibre.scope.ScopeSetup;
import codes.reason.fibre.scope.member.BoundMethod;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class TickPlugin implements ScopePlugin {

    @Override
    public void setup(Fibre fibre, ScopeSetup setup) {
        GameExecutor executor = setup.require(GameExecutor.class);

        for (BoundMethod method : setup.localMethods(Tick.class)) {
            setup.closeable(fibre.platform().scheduler()
                    .runRepeating(0L, 1L, () -> {
                        executor.execute(method::invoke);
                    }));
        }
    }

}
