package codes.reason.fibre.processor;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.NameAllocator;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import com.palantir.javapoet.TypeVariableName;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@NullMarked
final class ProcessorManageGenerationStep {

    private static final ClassName MANAGES = ClassName.get("codes.reason.fibre.annotations", "Manages");
    private static final ClassName IDENTITY = ClassName.get("codes.reason.fibre.identity", "Identity");
    private static final ClassName BOUND_METHOD = ClassName.get("codes.reason.fibre.member", "BoundMethod");
    private static final ClassName BOUND_FIELD = ClassName.get("codes.reason.fibre.member", "BoundField");
    private static final ClassName CLASS_IDENTITY = ClassName.get("codes.reason.fibre.identity", "ClassIdentity");
    private static final ClassName TOKEN_IDENTITY = ClassName.get("codes.reason.fibre.identity", "TokenIdentity");

    private final Types types;
    private final ManagedClass managed;
    private final ClassName owner;
    private final NameAllocator names = new NameAllocator();
    // the parameter each supplied dependency is passed in as
    private final Map<ManagedClass.Dependency, String> parameters = new HashMap<>();

    private ProcessorManageGenerationStep(Types types, ManagedClass managed) {
        this.types = types;
        this.managed = managed;
        this.owner = ClassName.get(managed.type());
        this.names.newName("builder");
        this.names.newName("identity");
    }

    static Members members(ManagedClass managed) {
        NameAllocator names = new NameAllocator();
        names.newName(ProcessorHelper.CREATE);
        names.newName(ProcessorHelper.IDENTITY_FIELD);
        // dependencies and providers sharing an identity share its field
        Map<IdentityKey, String> identities = new HashMap<>();
        Map<ManagedClass.Dependency, String> dependencies = new LinkedHashMap<>();
        Stream.concat(managed.suppliedDependencies().stream(), managed.providerDependencies()).forEach(dependency -> {
            // a scope's own handle is taken from the builder, so has no identity
            if (dependency.isScopeHandle()) {
                return;
            }
            String name = dependency.element().getSimpleName().toString();
            dependencies.put(dependency, dependency.isList()
                    ? names.newName(name)
                    : identities.computeIfAbsent(IdentityKey.of(dependency.identityType(), dependency.identityName()), _ -> names.newName(constantCase(name))));
        });
        Map<ManagedClass.Provider, String> providers = new LinkedHashMap<>();
        for (ManagedClass.Provider provider : managed.providers()) {
            providers.put(provider, identities.computeIfAbsent(IdentityKey.of(provider.declaredType(), provider.name()),
                    _ -> names.newName(constantCase(provider.method().getSimpleName().toString()))));
        }
        return new Members(dependencies, providers);
    }

    static JavaFile generate(Types types, ManagedClass managed) {
        ClassName name = ProcessorHelper.managedName(managed.type());
        TypeSpec.Builder type = TypeSpec.classBuilder(name)
                .addAnnotation(AnnotationSpec.builder(MANAGES).addMember("value", "$S", managed.type().getQualifiedName()).build())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addOriginatingElement(managed.type());
        if (!managed.isGeneric()) {
            DeclaredType self = (DeclaredType) managed.type().asType();
            type.addField(identityField(ProcessorHelper.IDENTITY_FIELD, self, identity(self, managed.name())));
        }
        Members members = members(managed);
        Set<String> fields = new HashSet<>();
        members.dependencies().forEach((dependency, member) -> {
            if (dependency.isList()) {
                type.addMethod(list(member, dependency));
            } else if (fields.add(member)) {
                type.addField(identityField(member, dependency.identityType(), identity(dependency.identityType(), dependency.identityName())));
            }
        });
        members.providers().forEach((provider, member) -> {
            if (fields.add(member)) {
                type.addField(identityField(member, provider.declaredType(), identity(provider.declaredType(), provider.name())));
            }
        });
        type.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build())
                .addMethod(new ProcessorManageGenerationStep(types, managed).create());
        return JavaFile.builder(name.packageName(), type.build())
                .addFileComment("Automatically generated by Fibre. Do not edit this file, changes will be overridden.")
                .build();
    }

    private MethodSpec create() {
        List<TypeVariableName> variables = this.managed.type().getTypeParameters().stream().map(TypeVariableName::get).toList();
        TypeName type = variables.isEmpty() ? this.owner : ParameterizedTypeName.get(this.owner, variables.toArray(TypeName[]::new));
        MethodSpec.Builder create = MethodSpec.methodBuilder(ProcessorHelper.CREATE)
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unchecked").build())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariables(variables)
                .returns(type)
                .addParameter(ProcessorHelper.SCOPE_BUILDER, "builder")
                .addParameter(ParameterizedTypeName.get(IDENTITY, type), "identity");
        for (ManagedClass.Dependency dependency : this.managed.suppliedDependencies()) {
            String parameter = this.names.newName(dependency.element().getSimpleName().toString());
            this.parameters.put(dependency, parameter);
            create.addParameter(TypeName.get(dependency.type()), parameter);
        }

        String instance = this.names.newName(ProcessorHelper.decapitalize(this.owner.simpleName()));
        create.addStatement(variables.isEmpty() ? "$T $N = new $T($L)" : "$T $N = new $T<>($L)",
                type, instance, this.owner, this.arguments(this.managed.constructorDependencies()));
        if (this.managed.kind() == ManagedClass.Kind.MANAGED_MANY) {
            create.addStatement("builder.instance($N)", instance);
        } else {
            create.addStatement("builder.bind(identity, $N)", instance);
        }

        for (ManagedClass.Injection injection : this.managed.injections()) {
            String member = injection.member().getSimpleName().toString();
            CodeBlock values = this.arguments(injection.dependencies());
            if (injection.isField()) {
                create.addStatement("$N.$N = $L", instance, member, values);
            } else {
                create.addStatement("$N.$N($L)", instance, member, values);
            }
        }

        for (ExecutableElement method : this.managed.enterMethods()) {
            create.addStatement("builder.enter($N::$N)", instance, method.getSimpleName().toString());
        }
        for (ExecutableElement method : this.managed.exitMethods()) {
            create.addStatement("builder.exit($N::$N)", instance, method.getSimpleName().toString());
        }
        for (ManagedClass.Queried queried : this.managed.queried()) {
            CodeBlock annotations = queried.annotations().stream()
                    .map(annotation -> CodeBlock.of("$T.class", annotation))
                    .collect(CodeBlock.joining(", "));
            if (queried.member() instanceof ExecutableElement method) {
                create.addStatement("builder.method($L, $L)", this.boundMethod(instance, method), annotations);
            } else {
                create.addStatement("builder.field($L, $L)", this.boundField(instance, (VariableElement) queried.member()), annotations);
            }
        }
        return create.addStatement("return $N", instance).build();
    }

    private static FieldSpec identityField(String name, DeclaredType type, CodeBlock identity) {
        return FieldSpec.builder(ParameterizedTypeName.get(IDENTITY, TypeName.get(type)), name, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(identity)
                .build();
    }

    private static CodeBlock identity(DeclaredType type, @Nullable String name) {
        CodeBlock nameArgument = name != null ? CodeBlock.of("$S", name) : CodeBlock.of("");
        if (type.getTypeArguments().isEmpty()) {
            ClassName raw = ClassName.get((TypeElement) type.asElement());
            return name != null
                    ? CodeBlock.of("new $T<>($L, $T.class)", CLASS_IDENTITY, nameArgument, raw)
                    : CodeBlock.of("new $T<>($T.class)", CLASS_IDENTITY, raw);
        }
        return CodeBlock.of("new $T<$T>($L) {}", TOKEN_IDENTITY, TypeName.get(type), nameArgument);
    }

    private static MethodSpec list(String name, ManagedClass.Dependency dependency) {
        DeclaredType element = dependency.requireListElement();
        ClassName raw = ClassName.get((TypeElement) element.asElement());
        MethodSpec.Builder list = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.get(dependency.type()))
                .addParameter(ProcessorHelper.SCOPE_BUILDER, "builder");
        if (element.getTypeArguments().isEmpty()) {
            return list.addStatement("return builder.list($T.class)", raw).build();
        }
        return list.addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unchecked").build())
                .addStatement("return ($T) ($T<?>) builder.list($T.class)", TypeName.get(dependency.type()), List.class, raw)
                .build();
    }

    private CodeBlock arguments(List<ManagedClass.Dependency> dependencies) {
        return dependencies.stream()
                .map(dependency -> dependency.isIdentityOf(this.managed.type())
                        ? CodeBlock.of("identity")
                        : CodeBlock.of("$N", this.parameters.get(dependency)))
                .collect(CodeBlock.joining(", "));
    }

    private CodeBlock boundMethod(String instance, ExecutableElement method) {
        List<? extends VariableElement> parameters = method.getParameters();
        // the lookup only sees what the class declares itself, so look an inherited method up where it's declared
        CodeBlock.Builder lookup = CodeBlock.builder().add("$T.lookup($T.class, $S", BOUND_METHOD, method.getEnclosingElement(), method.getSimpleName());
        CodeBlock.Builder call = CodeBlock.builder().add("(($T) target).$N(", this.owner, method.getSimpleName().toString());
        for (int i = 0; i < parameters.size(); i++) {
            TypeName parameter = this.erasure(parameters.get(i).asType());
            lookup.add(", $T.class", parameter);
            call.add(i == 0 ? "($T) args[$L]" : ", ($T) args[$L]", parameter, i);
        }
        lookup.add(")");
        call.add(")");

        CodeBlock invoker = method.getReturnType().getKind() == TypeKind.VOID
                ? CodeBlock.of("(target, args) -> {\n$>$L;\nreturn null;\n$<}", call.build())
                : CodeBlock.of("(target, args) -> $L", call.build());
        return CodeBlock.of("new $T($N, $T.class, $L, $L)", BOUND_METHOD, instance, this.owner, lookup.build(), invoker);
    }

    private CodeBlock boundField(String instance, VariableElement field) {
        String name = field.getSimpleName().toString();
        CodeBlock setter = field.getModifiers().contains(Modifier.FINAL)
                ? CodeBlock.of("null")
                : CodeBlock.of("(target, value) -> (($T) target).$N = ($T) value", this.owner, name, this.erasure(field.asType()));
        return CodeBlock.of("new $T($N, $T.class, $T.lookup($T.class, $S), target -> (($T) target).$N, $L)",
                BOUND_FIELD, instance, this.owner, BOUND_FIELD, field.getEnclosingElement(), name, this.owner, name, setter);
    }

    private TypeName erasure(TypeMirror type) {
        return TypeName.get(this.types.erasure(type));
    }

    private static String constantCase(String name) {
        return name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toUpperCase(Locale.ROOT);
    }

    // the identity field or list method of each dependency, and the identity field each provider binds to
    private record IdentityKey(TypeName type, @Nullable String name) {

        static IdentityKey of(DeclaredType type, @Nullable String name) {
            return new IdentityKey(TypeName.get(type).withoutAnnotations(), name);
        }
    }

    record Members(Map<ManagedClass.Dependency, String> dependencies, Map<ManagedClass.Provider, String> providers) {
    }

}
