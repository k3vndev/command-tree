# command-tree

A reusable Bukkit command-tree utility built with Maven.

## Use from a plugin

Extend `CommandNode<YourPlugin>` and provide a constructor receiving the command
sender and your `JavaPlugin` subclass:

```java
public final class RootCommand extends CommandNode<MyPlugin> {
	public RootCommand(CommandSender sender, MyPlugin plugin) {
		super(sender, plugin);
	}

	@Override
	public String getName() {
		return "example";
	}

	@Override
	public boolean execute(List<String> args) {
		return true;
	}
}
```

Register the manager as the command executor and tab completer:

```java
CommandTreeManager<MyPlugin> manager =
		new CommandTreeManager<>(this, RootCommand.class);
getCommand("example").setExecutor(manager);
getCommand("example").setTabCompleter(manager);
```

The Bukkit API is a `provided` dependency because the server supplies it at runtime.

## Build

```text
mvn test
```

## Run

```text
java -cp target/classes com.example.commandtree.Main
```
