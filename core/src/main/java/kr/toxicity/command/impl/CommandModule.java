package kr.toxicity.command.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import kr.toxicity.command.BetterCommandSource;
import kr.toxicity.command.CommandArgument;
import kr.toxicity.command.CommandListener;
import kr.toxicity.command.SenderType;
import kr.toxicity.command.impl.annotation.Command;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Main command module can include many sub-command
 * @param <W> wrapper class of command source
 */
@SuppressWarnings("unused")
@RequiredArgsConstructor(access = AccessLevel.MODULE)
public final class CommandModule<W extends BetterCommandSource> implements CommandArgument<W> {

    /**
     * Required argument message
     */
    public static final CommandMessage REQUIRED_ARGUMENT = new CommandMessage("internal.required_argument", Component.text()
            .content("    ")
            .append(Component.text().content("(value)").color(NamedTextColor.RED))
            .append(Component.text().content(" - ").color(NamedTextColor.GRAY))
            .append(Component.text("Required").color(NamedTextColor.WHITE))
            .build());

    /**
     * Optional argument message
     */
    public static final CommandMessage OPTIONAL_ARGUMENT = new CommandMessage("internal.optional_argument", Component.text()
            .content("    ")
            .append(Component.text().content("[value]").color(NamedTextColor.DARK_AQUA))
            .append(Component.text().content(" - ").color(NamedTextColor.GRAY))
            .append(Component.text("Optional").color(NamedTextColor.WHITE))
            .build());

    /**
     * Sub-command argument message
     */
    public static final CommandMessage CHILDREN = new CommandMessage("internal.type.children", Component.text("<children>").color(NamedTextColor.GREEN));

    /**
     * Aliases message
     */
    public static final CommandMessage ALIASES = new CommandMessage("internal.aliases", Component.text("Aliases:").color(NamedTextColor.DARK_AQUA));

    /**
     * Permission message
     */
    public static final CommandMessage PERMISSIONS = new CommandMessage("internal.permissions", Component.text("Permissions:").color(NamedTextColor.DARK_AQUA));

    /**
     * Help command click message
     */
    public static final CommandMessage CLICK_MESSAGE = new CommandMessage("internal.click_message", Component.text("Click to suggest command."));

    /**
     * Unknown description message
     */
    public static final CommandMessage UNKNOWN_DESCRIPTION = new CommandMessage("internal.unknown_command", Component.text("Unknown description."));

    private final BetterCommand root;
    private final String name;

    private String helpName;
    private String[] aliases;
    private String permission;
    private Predicate<? super W> predicate = w -> true;
    private MessageFunction<W> description;
    private SenderType[] type = SenderType.values();

    private final List<CommandArgument<W>> arguments = new ArrayList<>();

    @Override
    public @NotNull String name() {
        return name;
    }

    @Override
    public @NotNull String[] aliases() {
        return aliases != null ? aliases : new String[0];
    }

    /**
     * Sets command aliases
     * @param aliases target aliases
     * @return self
     */
    public @NotNull CommandModule<W> aliases(@Nullable String[] aliases) {
        this.aliases = aliases;
        return this;
    }

    @Override
    public @NotNull MessageFunction<W> description() {
        return description != null ? description : new MessageFunction<>(UNKNOWN_DESCRIPTION);
    }

    /**
     * Sets command description
     * @param key translatable key
     * @param component default component if lang file is not found
     * @return self
     */
    public @NotNull CommandModule<W> description(@NotNull String key, @NotNull Component component) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(component, "component");
        return description(new CommandMessage(key, component));
    }

    /**
     * Sets command description
     * @param description description message
     * @return self
     */
    public @NotNull CommandModule<W> description(@NotNull CommandMessage description) {
        Objects.requireNonNull(description, "description");
        return description(new MessageFunction<>(description));
    }

    /**
     * Sets command description
     * @param description message function
     * @return self
     */
    public @NotNull CommandModule<W> description(@Nullable MessageFunction<W> description) {
        this.description = description;
        return this;
    }

    @Override
    public @Nullable String permission() {
        return permission;
    }

    /**
     * Sets command permission
     * @param permission target permission
     * @return self
     */
    public @NotNull CommandModule<W> permission(@Nullable String permission) {
        this.permission = permission;
        return this;
    }

    /**
     * Sets a condition to and gate
     * @param predicate condition
     * @return self
     */
    public @NotNull CommandModule<W> andPredicate(@NotNull Predicate<? super W> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        var old = this.predicate;
        this.predicate = w -> old.test(w) && predicate.test(w);
        return this;
    }

    /**
     * Sets a condition to and gate
     * @param predicate condition
     * @return self
     */
    public @NotNull CommandModule<W> orPredicate(@NotNull Predicate<? super W> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        var old = this.predicate;
        this.predicate = w -> old.test(w) || predicate.test(w);
        return this;
    }

    /**
     * Creates children module of parent
     * @param name name of sub-command
     * @param consumer consumer of children module
     * @return self
     */
    public @NotNull CommandModule<W> children(@NotNull String name, @NotNull Consumer<CommandModule<? super W>> consumer) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(consumer, "consumer");
        var child = root.<W>module(name);
        child.helpName(helpName() + " " + name);
        consumer.accept(child);
        arguments.add(child);
        return this;
    }

    /**
     * Gets all applicable type
     * @return all type
     */
    public @NotNull SenderType[] type() {
        return type;
    }

    /**
     * Sets applicable type
     * @param type type
     * @return self
     */
    public @NotNull CommandModule<W> type(@NotNull SenderType[] type) {
        Objects.requireNonNull(type, "type");
        this.type = type;
        return this;
    }

    /**
     * Gets name in help command
     * @return name in help command
     */
    public @NotNull String helpName() {
        return helpName != null ? helpName : name;
    }

    /**
     * Sets name in help command
     * @param helpName name
     * @return self
     */
    public @NotNull CommandModule<W> helpName(@NotNull String helpName) {
        Objects.requireNonNull(helpName, "helpName");
        this.helpName = helpName;
        return this;
    }

    @Override
    public @NotNull Component usage(@NotNull W w) {
        String args;
        var size = arguments.size();
        if (size <= 6) {
            args = arguments.stream().map(CommandArgument::name).collect(Collectors.joining("\n"));
        } else {
            args = arguments.subList(0, 6).stream().map(CommandArgument::name).collect(Collectors.joining("\n")) + "\n+" + (size - 6);
        }
        return component(w, CHILDREN)
                .hoverEvent(HoverEvent.showText(Component.text(args)));
    }

    /**
     * Sets executor of command
     * @param executor command executor
     * @return self
     */
    public @NotNull CommandModule<W> executes(@NotNull CommandListener executor) {
        for (Method method : executor.getClass().getMethods()) {
            if (method.getAnnotation(Command.class) == null) continue;
            arguments.add(new MethodExecutor<>(
                    root,
                    executor,
                    method
            ));
        }
        return this;
    }

    /**
     * Gets max page of help command.
     * @return max page
     */
    private int maxPage() {
        return (int) Math.ceil((double) arguments.size() / 6);
    }

    /**
     * Executes help command
     * @param page target page
     * @param source command source
     */
    private void showHelp(int page, W source) {
        if (source == null) return;
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
        for (CommandArgument<W> args : arguments.subList((page - 1) * 6, Math.min(page * 6, arguments.size()))) {
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

    /**
     * Find a component by its message
     * @param source command source
     * @param message target message
     * @return component
     */
    private @NotNull Component component(W source, @NotNull CommandMessage message) {
        return root.registry.find(source, message);
    }

    @Override
    public @NotNull @Unmodifiable <S> List<LiteralArgumentBuilder<S>> build(@NotNull Function<S, W> mapper) {
        var aliases = aliases();
        var lists = new ArrayList<String>(1 + aliases.length);
        lists.add(name);
        lists.addAll(Arrays.asList(aliases));
        var set = EnumSet.copyOf(Arrays.asList(type));
        return lists.stream().map(s -> {
            var builder = LiteralArgumentBuilder.<S>literal(s)
                    .requires(source -> {
                        W wrapper = mapper.apply(source);
                        if (wrapper == null) return true;
                        if (!predicate.test(wrapper)) return false;
                        if (permission != null && !wrapper.hasPermission(permission)) return false;
                        return set.contains(wrapper.type());
                    });
            builder.then(LiteralArgumentBuilder.<S>literal("help")
                            .requires(source -> {
                                W wrapper = mapper.apply(source);
                                if (wrapper == null) return true;
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
            for (CommandArgument<W> argument : arguments) {
                for (LiteralArgumentBuilder<S> subBuilder : argument.build(mapper)) {
                    builder.then(subBuilder);
                }
            }
            return builder;
        }).toList();
    }
}
