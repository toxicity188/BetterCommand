package kr.toxicity.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import kr.toxicity.command.annotation.Command;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@RequiredArgsConstructor(access = AccessLevel.MODULE)
public final class CommandModule<S, W extends BetterCommandSource> implements CommandArgument<S, W> {

    public static final CommandMessage REQUIRED_ARGUMENT = new CommandMessage("internal.required_argument", Component.text()
            .content("    ")
            .append(Component.text().content("<value>").color(NamedTextColor.RED))
            .append(Component.text().content(" - ").color(NamedTextColor.GRAY))
            .append(Component.text("Required").color(NamedTextColor.WHITE))
            .build());
    public static final CommandMessage OPTIONAL_ARGUMENT = new CommandMessage("internal.optional_argument", Component.text()
            .content("    ")
            .append(Component.text().content("[value]").color(NamedTextColor.DARK_AQUA))
            .append(Component.text().content(" - ").color(NamedTextColor.GRAY))
            .append(Component.text("Optional").color(NamedTextColor.WHITE))
            .build());

    public static final CommandMessage CHILDREN = new CommandMessage("internal.type.children", Component.text("<children>").color(NamedTextColor.GREEN));
    public static final CommandMessage ALIASES = new CommandMessage("internal.aliases", Component.text("Aliases:").color(NamedTextColor.DARK_AQUA));
    public static final CommandMessage PERMISSIONS = new CommandMessage("internal.permissions", Component.text("Permissions:").color(NamedTextColor.DARK_AQUA));
    public static final CommandMessage CLICK_MESSAGE = new CommandMessage("internal.click_message", Component.text("Click to suggest command."));
    public static final CommandMessage UNKNOWN_DESCRIPTION = new CommandMessage("internal.unknown_command", Component.text("Unknown description."));

    private final BetterCommand root;
    private final String name;
    private final Function<S, W> mapper;

    private String helpName;
    private String[] aliases;
    private String permission;
    private Predicate<? super W> predicate = w -> true;
    private MessageFunction<W> description;
    private SenderType[] type = SenderType.values();

    private final List<CommandArgument<S, W>> arguments = new ArrayList<>();

    @Override
    public @NotNull String name() {
        return name;
    }

    public @NotNull CommandModule<S, W> aliases(@Nullable String[] aliases) {
        this.aliases = aliases;
        return this;
    }
    public @NotNull String[] aliases() {
        return aliases != null ? aliases : new String[0];
    }

    public @NotNull CommandModule<S, W> description(@NotNull String key, @NotNull Component component) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(component, "component");
        return description(new CommandMessage(key, component));
    }
    public @NotNull CommandModule<S, W> description(@NotNull CommandMessage description) {
        Objects.requireNonNull(description, "description");
        return description(new MessageFunction<>(description));
    }
    public @NotNull CommandModule<S, W> description(@Nullable MessageFunction<W> description) {
        this.description = description;
        return this;
    }
    public @NotNull MessageFunction<W> description() {
        return description != null ? description : new MessageFunction<>(UNKNOWN_DESCRIPTION);
    }

    public @NotNull CommandModule<S, W> permission(@Nullable String permission) {
        this.permission = permission;
        return this;
    }
    public @Nullable String permission() {
        return permission;
    }

    public @NotNull CommandModule<S, W> andPredicate(@NotNull Predicate<? super W> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        var old = this.predicate;
        this.predicate = w -> old.test(w) && predicate.test(w);
        return this;
    }
    public @NotNull CommandModule<S, W> orPredicate(@NotNull Predicate<? super W> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        var old = this.predicate;
        this.predicate = w -> old.test(w) || predicate.test(w);
        return this;
    }

    public @NotNull CommandModule<S, W> children(@NotNull String name, @NotNull Consumer<CommandModule<S, W>> consumer) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(consumer, "consumer");
        var child = root.module(name, mapper);
        child.helpName(helpName() + " " + name);
        consumer.accept(child);
        arguments.add(child);
        return this;
    }

    public @NotNull CommandModule<S, W> type(@NotNull SenderType[] type) {
        Objects.requireNonNull(type, "type");
        this.type = type;
        return this;
    }
    public @NotNull SenderType[] type() {
        return type;
    }

    public @NotNull String helpName() {
        return helpName != null ? helpName : name;
    }
    public @NotNull CommandModule<S, W> helpName(@NotNull String helpName) {
        Objects.requireNonNull(helpName, "helpName");
        this.helpName = helpName;
        return this;
    }

    @Override
    public @NotNull Component usage(@NotNull W w) {
        return component(w, CHILDREN)
                .hoverEvent(HoverEvent.showText(Component.text(arguments.stream().map(CommandArgument::name).collect(Collectors.joining(", ")))));
    }

    public @NotNull CommandModule<S, W> executes(@NotNull CommandListener executor) {
        for (Method method : executor.getClass().getMethods()) {
            if (method.getModifiers() != Modifier.PUBLIC) continue;
            if (method.getAnnotation(Command.class) == null) continue;
            arguments.add(new MethodExecutor<>(
                    root,
                    mapper,
                    executor,
                    method
            ));
        }
        return this;
    }

    private int maxPage() {
        return (int) Math.ceil((double) arguments.size() / 6);
    }
    private void showHelp(int page, W source) {
        var info = component(source, root.prefix().info());

        var audience = source.audience();
        var maxPage = maxPage();
        page = Math.max(Math.min(page, maxPage), 1);
        audience.sendMessage(
                info.append(Component.text()
                        .content("----------< " + page + " / " + maxPage + " >----------")
                        .color(NamedTextColor.GRAY))
        );
        audience.sendMessage(info);
        audience.sendMessage(Component.text().append(info).append(component(source, REQUIRED_ARGUMENT)));
        audience.sendMessage(Component.text().append(info).append(component(source, OPTIONAL_ARGUMENT)));
        audience.sendMessage(info);
        for (CommandArgument<S, W> args : arguments.subList((page - 1) * 6, Math.min(page * 6, arguments.size()))) {
            var hover = Component.text();
            var commandName = "/" + helpName() + " " + args.name();
            var first = false;
            if (args.aliases().length > 0) {
                first = true;
                hover.append(component(source, ALIASES))
                        .append(Component.newline())
                        .append(Component.text(String.join(", ", args.aliases())));
            }
            var perm = args.permission();
            if (perm != null) {
                if (first) hover.append(Component.newline()).append(Component.newline());
                else first = true;
                hover.append(component(source, PERMISSIONS))
                        .append(Component.newline())
                        .append(Component.text(perm));
            }
            if (first) hover.append(Component.newline()).append(Component.newline());
            hover.append(component(source, CLICK_MESSAGE));
            var builder = Component.text()
                    .append(info)
                    .append(Component.text()
                            .content(commandName)
                            .color(NamedTextColor.GOLD)
                            .hoverEvent(HoverEvent.showText(hover))
                    );
            var usageComponent = args.usage(source);
            if (usageComponent != null) {
                builder.append(Component.space()).append(usageComponent);
            }
            audience.sendMessage(builder
                    .append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(component(source, args.description().find(source)))
                    .clickEvent(ClickEvent.suggestCommand(commandName)));
        }
        audience.sendMessage(info);
        audience.sendMessage(
                info.append(Component.text()
                        .content("------------------------------")
                        .color(NamedTextColor.GRAY))
        );
    }

    private @NotNull Component component(W source, @NotNull CommandMessage message) {
        return root.registry.find(source, message);
    }

    public @NotNull List<LiteralArgumentBuilder<S>> build() {
        var aliases = aliases();
        var lists = new ArrayList<String>(1 + aliases.length);
        lists.add(name);
        lists.addAll(Arrays.asList(aliases));
        var set = EnumSet.copyOf(Arrays.asList(type));
        return lists.stream().map(s -> {
            var builder = LiteralArgumentBuilder.<S>literal(s)
                    .requires(source -> {
                        W wrapper;
                        try {
                            wrapper = mapper.apply(source);
                            if (wrapper == null) return false;
                        } catch (Exception e) {
                            return false;
                        }
                        if (!predicate.test(wrapper)) return false;
                        if (permission != null && !wrapper.hasPermission(permission)) return false;
                        return set.contains(wrapper.type());
                    });
            builder.then(LiteralArgumentBuilder.<S>literal("help")
                            .requires(source -> {
                                W wrapper;
                                try {
                                    wrapper = mapper.apply(source);
                                    if (wrapper == null) return false;
                                } catch (Exception e) {
                                    return false;
                                }
                                if (!predicate.test(wrapper)) return false;
                                if (permission != null && !wrapper.hasPermission(permission + ".help")) return false;
                                return set.contains(wrapper.type());
                            }).then(RequiredArgumentBuilder.<S, Integer>argument("page", IntegerArgumentType.integer(1, maxPage()))
                                    .executes(context -> {
                                        showHelp(context.getArgument("page", int.class), mapper.apply(context.getSource()));
                                        return 0;
                                    }))
                            .executes(context -> {
                                showHelp(1, mapper.apply(context.getSource()));
                                return 0;
                            })
                    )
                    .executes(context -> {
                        showHelp(0, mapper.apply(context.getSource()));
                        return 0;
                    });
            for (CommandArgument<S, W> argument : arguments) {
                for (LiteralArgumentBuilder<S> subBuilder : argument.build()) {
                    builder.then(subBuilder);
                }
            }
            return builder;
        }).toList();
    }
}
