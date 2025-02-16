package kr.toxicity.command.impl;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * Builtin class serializers
 */
final class ClassSerializers {
    private ClassSerializers() {
        throw new RuntimeException();
    }

    private static <T> T getExceptional(@NotNull Supplier<T> getter) {
        try {
            return getter.get();
        } catch (Exception e) {
            return null;
        }
    }

    static final ClassSerializer<String> STRING = ClassSerializer.builder((source, raw) -> raw.equals("null") ? null : raw)
            .name("string")
            .suggests(source -> List.of("string"))
            .build();

    static final ClassSerializer<Integer> INTEGER = ClassSerializer.builder((source, raw) -> raw.equals("null") ? null : getExceptional(() -> Integer.parseInt(raw)))
            .name("integer")
            .suggests(source -> List.of("0", "1", "2"))
            .build();
    static final ClassSerializer<Double> DOUBLE = ClassSerializer.builder((source, raw) -> raw.equals("null") ? null : getExceptional(() -> Double.parseDouble(raw)))
            .name("double")
            .suggests(source -> List.of("0.0", "1.0", "2.0"))
            .build();
    static final ClassSerializer<Float> FLOAT = ClassSerializer.builder((source, raw) -> raw.equals("null") ? null : getExceptional(() -> Float.parseFloat(raw)))
            .name("float")
            .suggests(source -> List.of("0.0", "1.0", "2.0"))
            .build();
    static final ClassSerializer<Long> LONG = ClassSerializer.builder((source, raw) -> raw.equals("null") ? null : getExceptional(() -> Long.parseLong(raw)))
            .name("long")
            .suggests(source -> List.of("0", "1", "2"))
            .build();
    static final ClassSerializer<Short> SHORT = ClassSerializer.builder((source, raw) -> raw.equals("null") ? null : getExceptional(() -> Short.parseShort(raw)))
            .name("short")
            .suggests(source -> List.of("0", "1", "2"))
            .build();
    static final ClassSerializer<Byte> BYTE = ClassSerializer.builder((source, raw) -> raw.equals("null") ? null : getExceptional(() -> Byte.parseByte(raw)))
            .name("byte")
            .suggests(source -> List.of("0", "1", "2"))
            .build();
    static final ClassSerializer<Character> CHARACTER = ClassSerializer.builder((source, raw) -> raw.length() != 1 ? null : raw.toCharArray()[0])
            .name("character")
            .suggests(source -> List.of("A", "B", "C"))
            .build();
    static final ClassSerializer<Boolean> BOOLEAN = ClassSerializer.builder((source, raw) -> switch (raw.toLowerCase()) {
                case "true" -> true;
                case "false" -> false;
                default -> null;
            })
            .name("boolean")
            .suggests(source -> List.of("true", "false"))
            .build();
}
