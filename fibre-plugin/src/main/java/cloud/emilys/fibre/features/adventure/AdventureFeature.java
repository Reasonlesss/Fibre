package cloud.emilys.fibre.features.adventure;

import cloud.emilys.fibre.api.Fibre;
import cloud.emilys.fibre.api.scope.ObjectKey;
import net.kyori.adventure.audience.Audience;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class AdventureFeature {

    private static final ObjectKey AUDIENCE_KEY = ObjectKey.fromType(Audience.class);

    private AdventureFeature() {
        throw new UnsupportedOperationException();
    }

    // This is slightly hacky, but it's whatever.
    public static void install(Fibre fibre, PlayerAudienceMapping mapping) {
        fibre.getStartupRegistry().registerScopeContributor((collector, key) -> {
            if (AUDIENCE_KEY.equals(key)) {
                collector.bind(key, new AudienceBinding(mapping));
            }
        });
    }
}
