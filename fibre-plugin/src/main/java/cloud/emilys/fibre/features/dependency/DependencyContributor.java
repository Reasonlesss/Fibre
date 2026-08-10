package cloud.emilys.fibre.features.dependency;

import cloud.emilys.fibre.api.fact.Facts;
import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.binding.Binding;
import cloud.emilys.fibre.api.scope.discovery.ScopeCollector;
import cloud.emilys.fibre.api.scope.discovery.ScopeContributor;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class DependencyContributor implements ScopeContributor {

    @Override
    public void contribute(ScopeCollector collector, ObjectKey key) {
        Facts facts = collector.getFactsFor(key);
        facts.get(DependencyFacts.CONSTRUCTOR)
                .ifPresent(constructor -> bind(collector, key, new ImplicitBinding(constructor)));
        facts.get(DependencyFacts.STATIC_PROVIDES_METHODS)
                .ifPresent(methods -> methods.forEach(method -> {
                    ObjectKey provides = ExplicitBinding.getProvidedKey(method);
                    bind(collector, provides, new ExplicitBinding(method));
                }));
        facts.get(DependencyFacts.INSTANCE_PROVIDES_METHODS)
                .ifPresent(methods -> methods.forEach(method -> {
                    ObjectKey provides = ExplicitBinding.getProvidedKey(method);
                    bind(collector, provides, new ExplicitBinding(key, method));
                }));
        facts.get(DependencyFacts.USE_FIELDS)
                .filter(fields -> !fields.isEmpty())
                .ifPresent(fields -> {
                    collector.initialize(key, new UseFieldInitializer(fields));
                    fields.stream()
                            .map(field -> ObjectKey.fromAnnotatedType(field.getAnnotatedType()))
                            .forEach(collector::visit);
                });
        facts.get(DependencyFacts.EXPOSE_FIELDS)
                .ifPresent(fields -> fields.forEach(field -> {
                    ObjectKey exposed = ObjectKey.fromAnnotatedType(field.getAnnotatedType());
                    bind(collector, exposed, new ExposeBinding(key, field));
                }));
    }

    private static void bind(ScopeCollector collector, ObjectKey key, Binding binding) {
        collector.bind(key, binding);
        collector.visit(key);
        binding.getDependencies().asSet().forEach(collector::visit);
    }
}
