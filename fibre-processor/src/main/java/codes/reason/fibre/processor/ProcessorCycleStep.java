package codes.reason.fibre.processor;

import org.jspecify.annotations.NullMarked;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NullMarked
final class ProcessorCycleStep {

    private final Messager messager;
    private final DependencyGraph graph;
    private final ManagedClass root;
    // the classes from the root down to the one being visited
    private final List<ManagedClass> path = new ArrayList<>();
    // classes whose dependencies are known to have no cycle
    private final Set<ManagedClass> done = new HashSet<>();

    private ProcessorCycleStep(Messager messager, DependencyGraph graph, ManagedClass root) {
        this.messager = messager;
        this.graph = graph;
        this.root = root;
    }

    static boolean check(Messager messager, DependencyGraph graph, ManagedClass root) {
        return new ProcessorCycleStep(messager, graph, root).visit(root);
    }

    private boolean visit(ManagedClass managed) {
        if (this.done.contains(managed)) {
            return true;
        }
        int start = this.path.indexOf(managed);
        if (start >= 0) {
            this.reportCycle(start, managed);
            return false;
        }

        this.path.add(managed);
        for (ManagedClass dependency : this.graph.dependencies(managed, this.root)) {
            if (!this.visit(dependency)) {
                return false;
            }
        }
        this.path.removeLast();
        this.done.add(managed);
        return true;
    }

    private void reportCycle(int start, ManagedClass managed) {
        String path = Stream.concat(this.path.subList(start, this.path.size()).stream(), Stream.of(managed))
                .map(member -> member.type().getSimpleName().toString())
                .collect(Collectors.joining(" -> "));
        this.messager.printMessage(Diagnostic.Kind.ERROR,
                "Dependency cycle in %s: %s".formatted(this.root.type().getSimpleName(), path),
                managed.type());
    }

}
