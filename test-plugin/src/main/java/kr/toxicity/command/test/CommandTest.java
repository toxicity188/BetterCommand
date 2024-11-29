package kr.toxicity.command.test;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import kr.toxicity.command.*;
import kr.toxicity.command.impl.BetterCommand;
import kr.toxicity.command.impl.ClassSerializer;
import kr.toxicity.command.impl.CommandMessage;
import kr.toxicity.command.impl.annotation.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class CommandTest extends JavaPlugin {
    @Override
    public void onEnable() {
        var logger = getLogger();
        //Setup library
        var library = new BetterCommand(new File(getDataFolder(), "lang"), MiniMessage.miniMessage(), new CommandLogger() {
            @Override
            public void info(@NotNull String... messages) {
                synchronized (logger) {
                    for (@NotNull String message : messages) {
                        logger.info(message);
                    }
                }
            }

            @Override
            public void warn(@NotNull String... messages) {
                synchronized (logger) {
                    for (@NotNull String message : messages) {
                        logger.warning(message);
                    }
                }
            }
        }).addSerializer(Location.class, ClassSerializer.builder((source, raw) -> {
            var split = raw.split("_");
            if (split.length == 4) {
                var world = Bukkit.getWorld(split[0]);
                if (world == null) return null;
                try {
                    return new Location(
                            world,
                            Double.parseDouble(split[1]),
                            Double.parseDouble(split[2]),
                            Double.parseDouble(split[3])
                    );
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        })
                        .name("location")
                        .suggests(source -> List.of("world_x_y_z"))
                        .nullMessage(new CommandMessage("test.null.location", Component.text("Location not found in [value]!")))
                        .build()
        );
        var teleportMessage = library.registerKey(new CommandMessage("test.teleport.message", Component.text("Go!")));
        //Create command
        var command = library.module("mycommand")
                .aliases(new String[] {"my"})
                .executes(new CommandListener() {
                    @Command
                    @Description(key = "test.print", defaultValue = "Shows 'hello world!'")
                    @Permission("test.print")
                    public void print(@Source BetterCommandSource me) {
                        me.audience().sendMessage(Component.text("Hello world!"));
                    }

                    @Command
                    @Description(key = "test.teleport", defaultValue = "Teleports to some location.")
                    @Permission("test.teleport")
                    @Aliases(aliases = "tp")
                    @Sender(type = SenderType.PLAYER)
                    public void teleport(@Source BetterCommandSource me, Location location) {
                        ((Player) me.audience()).teleport(location);
                        teleportMessage.send(me);
                    }

                    @Command
                    @Description(key = "test.generate", defaultValue = "Generates default lang file.")
                    @Permission("test.generated")
                    public void generate(@Source BetterCommandSource me) {
                        Bukkit.getScheduler().runTaskAsynchronously(CommandTest.this, () -> {
                            if (library.generateDefaultLang(me.locale())) me.audience().sendMessage(Component.text("Successfully generated."));
                            else me.audience().sendMessage(Component.text("Generation failed."));
                        });
                    }

                    @Command
                    @Description(key = "test.reload", defaultValue = "Reloads command.")
                    @Permission("test.reload")
                    public void reload(@Source BetterCommandSource me) {
                        Bukkit.getScheduler().runTaskAsynchronously(CommandTest.this, () -> {
                            var state = library.reload();
                            if (state instanceof ReloadState.Success success) {
                                me.audience().sendMessage(Component.text("Reload completes: " + success.time() + " ms"));
                            } else if (state instanceof ReloadState.Failure failure) {
                                me.audience().sendMessage(Component.text("Reload failures. Reason: " + failure.exception().getClass().getSimpleName()));
                            } else if (state instanceof ReloadState.OnReload) {
                                me.audience().sendMessage(Component.text("Still on reload!"));
                            }
                        });
                    }

                    @Command
                    @Description(key = "test.test", defaultValue = "Test command.")
                    @Permission("test.test")
                    public void test(@Source BetterCommandSource me, String sender, @Vararg @Option String argus) {
                        me.audience().sendMessage(Component.text(sender + ": " + argus));
                    }
                }).children("child", children -> children.permission("test.child").executes(new CommandListener() {
                    @Command
                    @Description(key = "test.child.die", defaultValue = "Die.")
                    @Permission("test.child.die")
                    @Sender(type = SenderType.PLAYER)
                    public void die(@Source BetterCommandSource me) {
                        ((Player) me.audience()).damage(9999);
                        me.audience().sendMessage(Component.text("Good bye!"));
                    }
                    @Command
                    @Description(key = "test.child.test", defaultValue = "Test command.")
                    @Permission("test.child.test")
                    public void test(@Source BetterCommandSource me, String sender, @Vararg @Option String argus) {
                        me.audience().sendMessage(Component.text(sender + ": " + argus));
                    }
                }
        ));
        for (LiteralArgumentBuilder<CommandSourceStack> builder : command.<CommandSourceStack>build(p -> {
            var get = p.getBukkitSender();
            if (get instanceof ConsoleCommandSender) return new CommandConsole();
            else if (get instanceof Player player) return new CommandPlayer(player);
            else throw new RuntimeException("Invalid sender.");
        })) {
            ((CraftServer) Bukkit.getServer()).getServer().getCommands().getDispatcher().register(builder);
        }
    }
}
