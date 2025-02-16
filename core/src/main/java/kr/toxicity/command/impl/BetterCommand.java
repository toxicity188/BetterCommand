package kr.toxicity.command.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import kr.toxicity.command.BetterCommandSource;
import kr.toxicity.command.CommandLogger;
import kr.toxicity.command.MessageSender;
import kr.toxicity.command.ReloadState;
import kr.toxicity.command.impl.exception.NotDirectoryException;
import kr.toxicity.command.impl.exception.NotJsonObjectException;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * BetterCommand!
 */
@SuppressWarnings("unused")
public final class BetterCommand {

    private static final Pattern VALUE_PATTERN = Pattern.compile("\\[(?<value>[a-zA-Z]+)]");

    private final File dataFolder;
    private final Map<Class<?>, ClassSerializer<?>> serializerMap = new LinkedHashMap<>();
    private final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    private Consumer<Throwable> exceptionHandler = e -> {};

    private final CommandLogger logger;
    private CommandPrefix prefix = CommandPrefix.DEFAULT;
    final ComponentSerializer<Component, Component, String> serializer;
    final MessageRegistry registry;

    @Getter
    private Supplier<Boolean> silentLog = () -> false;
    @Getter
    private volatile boolean onReload;


    /**
     * BetterCommand initializer
     * @param dataFolder data folder
     * @param serializer serializer (typically this is a MiniMessage)
     */
    public BetterCommand(@NotNull File dataFolder, @NotNull ComponentSerializer<Component, Component, String> serializer) {
        this(dataFolder, serializer, CommandLogger.DEFAULT);
    }

    /**
     * BetterCommand initializer
     * @param dataFolder data folder
     * @param serializer serializer (typically this is a MiniMessage)
     * @param logger logger
     */
    public BetterCommand(@NotNull File dataFolder, @NotNull ComponentSerializer<Component, Component, String> serializer, @NotNull CommandLogger logger) {
        Objects.requireNonNull(dataFolder, "dataFolder");
        Objects.requireNonNull(serializer, "serializer");
        Objects.requireNonNull(logger, "logger");

        if (dataFolder.isFile()) throw new NotDirectoryException(dataFolder.getPath() + " is not a directory.");

        this.dataFolder = dataFolder;
        this.serializer = serializer;
        this.logger = new CommandLogger() {
            @Override
            public void info(@NotNull String... messages) {
                if (!silentLog.get()) logger.info(messages);
            }

            @Override
            public void warn(@NotNull String... messages) {
                if (!silentLog.get()) logger.warn(messages);
            }
        };
        this.registry = new MessageRegistry(serializer);

        addSerializer(String.class, ClassSerializers.STRING);
        addSerializer(Integer.class, ClassSerializers.INTEGER);
        addSerializer(Double.class, ClassSerializers.DOUBLE);
        addSerializer(Float.class, ClassSerializers.FLOAT);
        addSerializer(Long.class, ClassSerializers.LONG);
        addSerializer(Short.class, ClassSerializers.SHORT);
        addSerializer(Byte.class, ClassSerializers.BYTE);
        addSerializer(Character.class, ClassSerializers.CHARACTER);
        addSerializer(Boolean.class, ClassSerializers.BOOLEAN);
    }

    /**
     * Adds class serializer
     * @param clazz target class
     * @param serializer serializer of class
     * @return self
     * @param <T> type of target class
     */
    public <T> @NotNull BetterCommand addSerializer(@NotNull Class<T> clazz, ClassSerializer<? extends T> serializer) {
        Objects.requireNonNull(clazz, "clazz");
        Objects.requireNonNull(serializer, "serializer");
        serializerMap.put(clazz, serializer);
        return this;
    }


    /**
     * Sets whether to disable log
     * @param silentLog checker
     * @return self
     */
    public @NotNull BetterCommand silentLog(Supplier<Boolean> silentLog) {
        this.silentLog = silentLog;
        return this;
    }

    /**
     * Sets command prefix
     * @param prefix prefix
     * @return self
     */
    public @NotNull BetterCommand prefix(@NotNull CommandPrefix prefix) {
        Objects.requireNonNull(prefix, "prefix");
        this.prefix = prefix;
        return this;
    }

    /**
     * Sets exception handler
     * @param handler handler
     * @return self
     */
    public @NotNull BetterCommand exceptionHandler(@NotNull Consumer<Throwable> handler) {
        Objects.requireNonNull(handler, "handler");
        var old = exceptionHandler;
        exceptionHandler = old.andThen(handler);
        return this;
    }

    /**
     * Handle exception
     * @param throwable exception or error
     */
    void handleException(@NotNull Throwable throwable) {
        exceptionHandler.accept(throwable);
    }

    /**
     * Gets command prefix
     * @return prefix
     */
    public @NotNull CommandPrefix prefix() {
        return prefix;
    }

    /**
     * Gets command logger
     * @return logger
     */
    public @NotNull CommandLogger logger() {
        return logger;
    }

    /**
     * Reload lang file
     * @see ReloadState
     * @return state
     */
    public synchronized @NotNull ReloadState reload() {
        if (onReload) return ReloadState.ON_RELOAD;
        onReload = true;
        try {
            var time = System.currentTimeMillis();
            if (!dataFolder.exists() && dataFolder.mkdirs()) {
                logger.warn("Unable to make a directory in " + dataFolder.getPath());
            }
            var listFiles = dataFolder.listFiles();
            registry.clear();
            if (listFiles != null) {
                for (File file : listFiles) {
                    addLang(file);
                }
            }
            synchronized (this) {
                onReload = false;
            }
            return new ReloadState.Success(System.currentTimeMillis() - time);
        } catch (Exception any) {
            synchronized (this) {
                onReload = false;
            }
            return new ReloadState.Failure(any);
        }
    }

    /**
     * Generates lang file fit locale
     * @param locale target locale
     * @return whether to success
     */
    public boolean generateDefaultLang(@NotNull Locale locale) {
        if (registry.contains(locale)) return false;
        var file = new File(dataFolder, locale.getLanguage().toLowerCase() + "_" + locale.getCountry().toUpperCase() + ".json");
        var object = file.exists() ? parseFile(file) : new JsonObject();
        for (CommandMessage allMessage : CommandMessage.allMessages()) {
            object.addProperty(allMessage.key(), serializer.serialize(allMessage.defaultMessage()));
        }
        try (
                var stream = new FileWriter(file);
                var buffer = new BufferedWriter(stream);
                var json = new JsonWriter(buffer)
        ) {
            json.setIndent(" ");
            gson.toJson(object, json);
            registry.register(
                    locale,
                    object
            );
        } catch (IOException e) {
            handleException(e, "Unable to create lang file to " + file.getPath());
        }
        return true;
    }

    /**
     * Adds lang file
     * @param file target file
     */
    private void addLang(@NotNull File file) {
        var fileName = FileName.parse(file);
        if (fileName.extension == null || !fileName.extension.equals("json")) {
            logger.warn("This file is not a json. skipped: " + file.getPath());
            return;
        }
        try {
            var localeSplit = fileName.name.split("_");
            var locale = localeSplit.length == 1 ? new Locale(localeSplit[0]) : new Locale(localeSplit[0].toLowerCase(), localeSplit[1].toUpperCase());
            registry.register(
                    locale,
                    parseFile(file)
            );
            logger.info("New lang file loaded: " + locale);
        } catch (Exception e) {
            handleException(e, "Unable to read this file: " + file.getPath());
        }
    }

    /**
     * Print exception to logger
     * @param exception target exception
     * @param message additional message
     */
    private void handleException(@NotNull Exception exception, @NotNull String message) {
        try (var stringWriter = new StringWriter(); var printWriter = new PrintWriter(stringWriter)) {
            exception.printStackTrace(printWriter);
            logger.warn(
                    message,
                    "Reason: " + exception.getClass().getSimpleName(),
                    "Stack trace: " + stringWriter
            );
        } catch (IOException ignored) {
            //StringWriter has no effect.
        }
    }

    /**
     * Find component in registry
     * @param source command source
     * @param message message
     * @return component
     */
    private @NotNull Component component(@NotNull BetterCommandSource source, @NotNull CommandMessage message) {
        return registry.find(source, message);
    }

    /**
     * Creates message sender depend on message
     * @param message target message
     * @return message sender
     */
    public @NotNull MessageSender registerKey(@NotNull CommandMessage message) {
        return (l, s, m) -> {
            var msg = component(s, message);
            if (serializer.serialize(msg).isEmpty()) return;
            s.audience().sendMessage(Component.text()
                    .append(component(s, switch (l) {
                        case INFO -> prefix.info();
                        case WARN -> prefix.warn();
                        case ERROR -> prefix.error();
                    }))
                    .append(msg.replaceText(TextReplacementConfig.builder()
                            .match(VALUE_PATTERN)
                            .replacement((r, b) -> {
                                var group = r.group(1);
                                var get = m.get(group);
                                return get != null ? get : Component.text(group);
                            })
                            .build())));
        };
    }

    /**
     * Parse file to json object
     * @param file target file
     * @return json object
     * @throws NotJsonObjectException if it is not a json object.
     */
    private JsonObject parseFile(@NotNull File file) {
        try (var reader = new FileReader(file); var buffered = new BufferedReader(reader); var json = new JsonReader(buffered)) {
            var result = JsonParser.parseReader(json);
            if (result.isJsonObject()) return result.getAsJsonObject();
            else throw new NotJsonObjectException("This file is not a json object: " + file.getPath());
        } catch (IOException exception) {
            handleException(exception);
            throw new RuntimeException(exception);
        }
    }

    /**
     * File name
     * @param name name without extension
     * @param extension extension
     */
    private record FileName(@NotNull String name, @Nullable String extension) {
        private static @NotNull FileName parse(@NotNull File file) {
            var name = file.getName().split("\\.");
            return new FileName(
                    name[0],
                    name.length > 1 ? name[1] : null
            );
        }
    }

    /**
     * Find serializer by its class
     * @param clazz target class
     * @return serializer or null
     */
    @Nullable ClassSerializer<?> find(@NotNull Class<?> clazz) {
        return serializerMap.get(clazz);
    }

    /**
     * Creates a command module
     * @param name command name
     * @return command module
     * @param <W> wrapper class of platform-side command source (normally it is a wrapper class of CommandSourceStack)
     */
    public @NotNull <W extends BetterCommandSource> CommandModule<W> module(@NotNull String name) {
        return new CommandModule<>(this, name);
    }

    /**
     * Gets prefix component.
     * @param type prefix type
     * @param source component source
     * @return prefix component
     */
    public @NotNull Component prefix(@NotNull BetterCommandSource source, @NotNull PrefixType type) {
        var get = switch (type) {
            case INFO -> prefix.info();
            case WARN -> prefix.warn();
            case ERROR -> prefix.error();
        };
        return registry.find(source, get);
    }

    /**
     * Prefix type.
     */
    public enum PrefixType {
        /**
         * Info
         */
        INFO,
        /**
         * Warn
         */
        WARN,
        /**
         * Error
         */
        ERROR
    }
}