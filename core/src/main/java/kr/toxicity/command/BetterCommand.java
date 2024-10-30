package kr.toxicity.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import kr.toxicity.command.exception.NotDirectoryException;
import kr.toxicity.command.exception.NotJsonObjectException;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.function.Function;

@SuppressWarnings("unused")
public final class BetterCommand {
    private final File dataFolder;
    private final Map<Class<?>, ClassSerializer<?>> serializerMap = new LinkedHashMap<>();
    private final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    private final CommandLogger logger;
    private CommandPrefix prefix = CommandPrefix.DEFAULT;
    final ComponentSerializer<Component, Component, String> serializer;
    final MessageRegistry registry;

    @Getter
    private volatile boolean onReload;

    public BetterCommand(@NotNull File dataFolder, @NotNull ComponentSerializer<Component, Component, String> serializer) {
        this(dataFolder, serializer, CommandLogger.DEFAULT);
    }
    public BetterCommand(@NotNull File dataFolder, @NotNull ComponentSerializer<Component, Component, String> serializer, @NotNull CommandLogger logger) {
        Objects.requireNonNull(dataFolder, "dataFolder");
        Objects.requireNonNull(serializer, "serializer");
        Objects.requireNonNull(logger, "logger");

        if (dataFolder.isFile()) throw new NotDirectoryException(dataFolder.getPath() + " is not a directory.");

        this.dataFolder = dataFolder;
        this.serializer = serializer;
        this.logger = logger;
        this.registry = new MessageRegistry(serializer);

        reload();

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

    public <T> @NotNull BetterCommand addSerializer(@NotNull Class<T> clazz, ClassSerializer<? extends T> serializer) {
        Objects.requireNonNull(clazz, "clazz");
        Objects.requireNonNull(serializer, "serializer");
        serializerMap.put(clazz, serializer);
        return this;
    }

    public @NotNull BetterCommand prefix(@NotNull CommandPrefix prefix) {
        Objects.requireNonNull(prefix, "prefix");
        this.prefix = prefix;
        return this;
    }

    public @NotNull CommandPrefix prefix() {
        return prefix;
    }
    public @NotNull CommandLogger logger() {
        return logger;
    }

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

    private static JsonObject parseFile(@NotNull File file) {
        try (var reader = new FileReader(file); var buffered = new BufferedReader(reader); var json = new JsonReader(buffered)) {
            var result = JsonParser.parseReader(json);
            if (result.isJsonObject()) return result.getAsJsonObject();
            else throw new NotJsonObjectException("This file is not a json object: " + file.getPath());
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private record FileName(@NotNull String name, @Nullable String extension) {
        private static @NotNull FileName parse(@NotNull File file) {
            var name = file.getName().split("\\.");
            return new FileName(
                    name[0],
                    name.length > 1 ? name[1] : null
            );
        }
    }

    @Nullable ClassSerializer<?> find(@NotNull Class<?> clazz) {
        return serializerMap.get(clazz);
    }

    public @NotNull <S, W extends BetterCommandSource> CommandModule<S, W> module(@NotNull String name, @NotNull Function<S, W> mapper) {
        return new CommandModule<>(this, name, mapper);
    }
}