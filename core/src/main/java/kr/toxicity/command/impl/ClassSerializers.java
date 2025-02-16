package kr.toxicity.command.impl;

import java.util.List;
import java.util.Optional;

/**
 * Builtin class serializers
 */
final class ClassSerializers {
    private ClassSerializers() {
        throw new RuntimeException();
    }

    static final ClassSerializer<String> STRING = ClassSerializer.builder((source, raw) -> raw.equals("null") ? null : raw)
            .name("string")
            .suggests(source -> List.of("string"))
            .build();

    static final ClassSerializer<Integer> INTEGER = ClassSerializer.builder((source, raw) ->
                    raw.equals("null") ? null : Optional.of(raw).map(r -> { try { return Integer.parseInt(r); } catch(NumberFormatException e) { return null; } }).orElse(null))
            .name("integer")
            .suggests(source -> List.of("0", "1", "2"))
            .build();
    static final ClassSerializer<Double> DOUBLE = ClassSerializer.builder((source, raw) ->
                    raw.equals("null") ? null : Optional.of(raw).map(r -> { try { return Double.parseDouble(r); } catch(NumberFormatException e) { return null; } }).orElse(null))
            .name("double")
            .suggests(source -> List.of("0.0", "1.0", "2.0"))
            .build();
    static final ClassSerializer<Float> FLOAT = ClassSerializer.builder((source, raw) ->
                    raw.equals("null") ? null : Optional.of(raw).map(r -> { try { return Float.parseFloat(r); } catch(NumberFormatException e) { return null; } }).orElse(null))
            .name("float")
            .suggests(source -> List.of("0.0", "1.0", "2.0"))
            .build();
    static final ClassSerializer<Long> LONG = ClassSerializer.builder((source, raw) ->
                    raw.equals("null") ? null : Optional.of(raw).map(r -> { try { return Long.parseLong(r); } catch(NumberFormatException e) { return null; } }).orElse(null))
            .name("long")
            .suggests(source -> List.of("0", "1", "2"))
            .build();
    static final ClassSerializer<Short> SHORT = ClassSerializer.builder((source, raw) ->
                    raw.equals("null") ? null : Optional.of(raw).map(r -> { try { return Short.parseShort(r); } catch(NumberFormatException e) { return null; } }).orElse(null))
            .name("short")
            .suggests(source -> List.of("0", "1", "2"))
            .build();
    static final ClassSerializer<Byte> BYTE = ClassSerializer.builder((source, raw) ->
                    raw.equals("null") ? null : Optional.of(raw).map(r -> { try { return Byte.parseByte(r); } catch(NumberFormatException e) { return null; } }).orElse(null))
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
