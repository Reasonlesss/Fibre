package cloud.emilys.fibre.api.scope.binding;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record BindingPriority(int precedence) {

    public static final BindingPriority LOWEST = new BindingPriority(100);
    public static final BindingPriority LOW = new BindingPriority(200);
    public static final BindingPriority NORMAL = new BindingPriority(300);
    public static final BindingPriority HIGH = new BindingPriority(400);
    public static final BindingPriority HIGHEST = new BindingPriority(500);

    public boolean isGreaterThan(BindingPriority priority) {
        return this.precedence > priority.precedence;
    }
}
