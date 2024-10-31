package kr.toxicity.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import kr.toxicity.command.annotation.*;
import kr.toxicity.command.exception.NotLastParameterException;
import kr.toxicity.command.exception.NotSerializerRegisteredException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.HoverEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

class MethodExecutor<W extends BetterCommandSource> implements CommandArgument<W> {

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_REFERENCE = Map.ofEntries(
            Map.entry(Integer.TYPE, Integer.class),
            Map.entry(Boolean.TYPE, Boolean.class),
            Map.entry(Double.TYPE, Double.class),
            Map.entry(Character.TYPE, Character.class),
            Map.entry(Float.TYPE, Float.class),
            Map.entry(Short.TYPE, Short.class),
            Map.entry(Byte.TYPE, Byte.class),
            Map.entry(Long.TYPE, Long.class)
    );
    private static final Pattern VALUE_PATTERN = Pattern.compile("\\[value]");

    private final BetterCommand root;
    private final String name;
    private final CommandMessage description;
    private final String permission;
    private final CommandListener obj;
    private final Method method;

    private final String[] aliases;
    private List<UsageGetter<W>> usage = Collections.emptyList();

    MethodExecutor(@NotNull BetterCommand root, @NotNull CommandListener obj, @NotNull Method method) {
        try {
            method.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.root = root;
        name = method.getName();
        var description = Objects.requireNonNull(
                method.getDeclaredAnnotation(Description.class),
                "@Description annotation not found in method " + name + "."
        );
        this.description = new CommandMessage(description.key(), root.serializer.deserialize(description.defaultValue()));
        this.permission = Optional.ofNullable(method.getDeclaredAnnotation(Permission.class)).map(Permission::value).orElse(null);

        var aliasesAnnotation = method.getAnnotation(Aliases.class);
        aliases = aliasesAnnotation != null ? aliasesAnnotation.aliases() : new String[0];
        this.method = method;
        this.obj = obj;
    }

    public @NotNull <S> List<LiteralArgumentBuilder<S>> build(@NotNull Function<S, W> mapper) {
        usage = new ArrayList<>();
        var typeAnnotation = method.getAnnotation(Sender.class);
        var type = EnumSet.copyOf(Arrays.asList(typeAnnotation != null ? typeAnnotation.type() : SenderType.values()));

        var lists = new ArrayList<String>(1 + aliases.length);
        lists.add(name);
        lists.addAll(Arrays.asList(aliases));

        var index = 0;
        List<RequiredArgumentBuilder<S, ?>> commandTree = new ArrayList<>();
        var valueList = new ArrayList<ContextParser<S>>();
        var lastParameter = method.getParameters().length - 1;
        var parameterIndex = 0;
        var option = false;
        for (Parameter parameter : method.getParameters()) {
            var clazz = parameter.getType();
            option = parameter.getAnnotation(Option.class) != null;
            var vararg = parameter.getAnnotation(Vararg.class) != null;
            var canBeNull = !option && parameter.getAnnotation(CanBeNull.class) != null;
            if ((option || vararg) && parameterIndex < lastParameter) throw new NotLastParameterException("@Option or @Vararg can work only last parameter.");
            parameterIndex++;
            if (BetterCommandSource.class.isAssignableFrom(clazz) && parameter.getAnnotation(Source.class) != null) {
                valueList.add(new ContextParser<>() {
                    @Override
                    public boolean canBeNull() {
                        return canBeNull;
                    }

                    @Override
                    public @NotNull String key(@NotNull CommandContext<S> context) {
                        return "";
                    }

                    @Override
                    public void nullMessage(@NotNull CommandContext<S> context, @NotNull String value) {

                    }

                    @Override
                    public @Nullable Object parse(@NotNull CommandContext<S> context) {
                        return mapper.apply(context.getSource());
                    }
                });
                continue;
            }
            var ref = PRIMITIVE_TO_REFERENCE.get(clazz);
            var finalClazz = ref != null ? ref : clazz;
            var serializer = root.find(finalClazz);
            if (serializer == null) throw new NotSerializerRegisteredException("A serializer for " + finalClazz.getSimpleName() + " not found.");
            var key = parameter.getName();
            commandTree.add(RequiredArgumentBuilder.<S, String>argument(key, vararg ? StringArgumentType.greedyString() : StringArgumentType.string())
                    .suggests((context, builder1) -> {
                        for (String suggest : serializer.suggests(mapper.apply(context.getSource()))) {
                            builder1.suggest(suggest);
                        }
                        return builder1.buildFuture();
                    }));
            usage.add(new UsageGetter<>(option ? serializer.optional() : serializer.required(), serializer::suggests));
            valueList.add(new ContextParser<>() {
                @Override
                public boolean canBeNull() {
                    return canBeNull;
                }

                @Override
                public void nullMessage(@NotNull CommandContext<S> context, @NotNull String value) {
                    var wrapper = mapper.apply(context.getSource());
                    wrapper.audience().sendMessage(
                            root.registry.find(wrapper, serializer.nullMessage()).replaceText(TextReplacementConfig.builder()
                                    .match(VALUE_PATTERN)
                                    .replacement((r, b) -> Component.text(value))
                                    .build())
                    );
                }

                @Override
                public @NotNull String key(@NotNull CommandContext<S> context) {
                    return context.getArgument(key, String.class);
                }

                @Override
                public @Nullable Object parse(@NotNull CommandContext<S> context) {
                    return serializer.deserialize(mapper.apply(context.getSource()), key(context));
                }
            });
        }
        Command<S> command = context -> {
            try {
                var array = new Object[valueList.size()];
                var i = 0;
                for (ContextParser<S> parser : valueList) {
                    var value = parser.parse(context);
                    if (!parser.canBeNull() && value == null) {
                        parser.nullMessage(context, parser.key(context));
                        return 0;
                    }
                    array[i++] = value;
                }
                method.invoke(obj, array);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return 0;
        };
        List<LiteralArgumentBuilder<S>> nodes = new ArrayList<>(lists.size());
        for (String s : lists) {
            var node = LiteralArgumentBuilder.<S>literal(s)
                    .requires(source -> {
                        W wrapper;
                        try {
                            wrapper = mapper.apply(source);
                            if (wrapper == null) return false;
                        } catch (Exception e) {
                            return false;
                        }
                        if (permission != null && !wrapper.hasPermission(permission)) return false;
                        return type.contains(wrapper.type());
                    });
            var treeIndex = commandTree.size() - 1;
            if (option) {
                var previous = treeIndex - 1 >= 0 ? commandTree.get(treeIndex - 1) : node;
                previous.executes(context -> {
                    try {
                        var array = new Object[valueList.size()];
                        var i = 0;
                        for (ContextParser<S> parser : valueList.subList(0, valueList.size() - 1)) {
                            var value = parser.parse(context);
                            if (!parser.canBeNull() && value == null) {
                                parser.nullMessage(context, parser.key(context));
                                return 0;
                            }
                            array[i++] = value;
                        }
                        array[i] = null;
                        method.invoke(obj, array);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return 0;
                });
            }
            if (commandTree.isEmpty()) {
                nodes.add(node.executes(command));
            } else {
                commandTree.get(treeIndex).executes(command);
                while (treeIndex >= 0) {
                    var previous = treeIndex - 1 >= 0 ? commandTree.get(treeIndex - 1) : node;
                    previous.then(commandTree.get(treeIndex));
                    treeIndex--;
                }
                nodes.add(node);
            }
        }
        return nodes;
    }

    private interface ContextParser<T> {
        boolean canBeNull();
        void nullMessage(@NotNull CommandContext<T> context, @NotNull String value);
        @NotNull String key(@NotNull CommandContext<T> context);
        @Nullable Object parse(@NotNull CommandContext<T> context);
    }

    private record UsageGetter<T>(@NotNull CommandMessage message, @NotNull Function<T, List<String>> suggests) {}

    @Override
    public @NotNull String name() {
        return name;
    }

    @Override
    public @NotNull String[] aliases() {
        return aliases;
    }

    @Override
    public @Nullable String permission() {
        return permission;
    }

    @Override
    public @NotNull MessageFunction<W> description() {
        return new MessageFunction<>(description);
    }

    @Override
    public @Nullable Component usage(@NotNull W w) {
        if (usage.isEmpty()) return null;
        var builder = Component.text();
        var index = 0;
        var last = usage.size() - 1;
        for (UsageGetter<W> commandMessage : usage) {
            builder.append(root.registry.find(w, commandMessage.message)
                    .hoverEvent(HoverEvent.showText(Component.text(String.join(", ", commandMessage.suggests.apply(w))))));
            if (index++ < last) builder.append(Component.space());
        }
        return builder.build();
    }
}
