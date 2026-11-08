package codes.reason.fibre.processor;

import org.jspecify.annotations.NullMarked;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NullMarked
final class ProcessorCycleStep {

    private final Messager messager;
    private final DependencyGraph graph;
    private final ManagedClass root;
    // the classes from the root down to the one being visited
    private final List<DependencyGraph.Node> path = new ArrayList<>();
    // classes and providers whose dependencies are known to have no cycle
    private final Set<DependencyGraph.Node> done = new HashSet<>();
    // what handles refer to, checked from an empty path since they're built after whatever took the handle
    private final Queue<DependencyGraph.Node> handled = new ArrayDeque<>();

    private ProcessorCycleStep(Messager messager, DependencyGraph graph, ManagedClass root) {
        this.messager = messager;
        this.graph = graph;
        this.root = root;
    }

    static boolean check(Messager messager, DependencyGraph graph, ManagedClass root) {
        ProcessorCycleStep step = new ProcessorCycleStep(messager, graph, root);
        // every provider is called, even when nothing in the root scope uses it
        if (!step.visit(root) || !root.providers().stream().allMatch(step::visit)) {
            return false;
        }
        while (!step.handled.isEmpty()) {
            if (!step.visit(step.handled.remove())) {
                return false;
            }
        }
        return true;
    }

    private boolean visit(DependencyGraph.Node node) {
        if (this.done.contains(node)) {
            return true;
        }
        int start = this.path.indexOf(node);
        if (start >= 0) {
            this.reportCycle(start, node);
            return false;
        }

        this.path.add(node);
        for (DependencyGraph.Node dependency : this.graph.dependencies(node, this.root)) {
            if (!this.visit(dependency)) {
                return false;
            }
        }
        this.path.removeLast();
        this.done.add(node);
        for (DependencyGraph.Node handled : this.graph.handled(node, this.root)) {
            if (handled instanceof ManagedClass managed && managed.kind() == ManagedClass.Kind.MANAGED_MANY) {
                this.messager.printMessage(Diagnostic.Kind.ERROR,
                        "A Handle can't refer to %s since many of it are built, use a List instead".formatted(managed.describe()),
                        element(node));
                return false;
            }
            this.handled.add(handled);
        }
        return true;
    }

    private void reportCycle(int start, DependencyGraph.Node node) {
        String path = Stream.concat(this.path.subList(start, this.path.size()).stream(), Stream.of(node))
                .map(DependencyGraph.Node::describe)
                .collect(Collectors.joining(" -> "));
        this.messager.printMessage(Diagnostic.Kind.ERROR,
                "Dependency cycle in %s: %s".formatted(this.root.type().getSimpleName(), path),
                element(node));
    }

    private static Element element(DependencyGraph.Node node) {
        return switch (node) {
            case ManagedClass managed -> managed.type();
            case ManagedClass.Provider provider -> provider.method();
        };
    }

}
