package codes.reason.fibre.features.event;

import codes.reason.fibre.api.fact.FactKey;
import java.util.List;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class EventFacts {

    public static final FactKey<List<EventMethod>> METHODS = FactKey.of("fibre", "event_listeners", List.class);

    private EventFacts() {
        throw new UnsupportedOperationException();
    }
}
