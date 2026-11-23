package codes.reason.fibre.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import codes.reason.fibre.builtin.filter.FilterPlugin;
import codes.reason.fibre.builtin.event.EventPlugin;
import codes.reason.fibre.builtin.tick.TickPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PluginManager {

    private final List<ScopePlugin> plugins = new ArrayList<>();

    public PluginManager() {
        // built-in plugins; AudiencePlugin needs Adventure, so the platform installs it
        this.install(new FilterPlugin());
        this.install(new EventPlugin());
        this.install(new TickPlugin());
    }

    public boolean install(ScopePlugin plugin) {
        Objects.requireNonNull(plugin, "plugin must not be null");
        for (ScopePlugin installed : this.plugins) {
            if (installed == plugin) {
                return false;
            }
        }
        this.plugins.add(plugin);
        return true;
    }

    public boolean uninstall(ScopePlugin plugin) {
        Objects.requireNonNull(plugin, "plugin must not be null");
        return this.plugins.removeIf(installed -> installed == plugin);
    }

    public List<ScopePlugin> plugins() {
        return List.copyOf(this.plugins);
    }

}
