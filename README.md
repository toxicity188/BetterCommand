# BetterCommand
A translatable command API that implements brigadier command node.  
The main propose of this project is **support cross-platform like Bukkit, Velocity and Fabric**.

## Example source (Bukkit)
You can see this in CommandTest.java.
```java
//Create command
var command = library.<CommandSourceStack, BetterCommandSource>module("mycommand", p -> {
    var get = p.getBukkitSender();
    if (get instanceof ConsoleCommandSender) return new CommandConsole();
    else if (get instanceof Player player) return new CommandPlayer(player);
    else throw new RuntimeException("Invalid sender.");
}).aliases(new String[] {"my"})
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
        me.audience().sendMessage(Component.text("Go!"));
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
}));
```
## Build
Required JDK 17.  
./gradlew build