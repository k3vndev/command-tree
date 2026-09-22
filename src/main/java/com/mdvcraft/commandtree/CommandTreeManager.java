package com.mdvcraft.commandtree;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public final class CommandTreeManager<P extends JavaPlugin> implements TabExecutor {
  private final P plugin;
  private final Class<? extends CommandNode<P>> rootCommandClass;

  public CommandTreeManager(P plugin, Class<? extends CommandNode<P>> rootCommandClass) {
    this.plugin = plugin;
    this.rootCommandClass = rootCommandClass;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    CommandNode<P> rootCommand = createRootCommand(sender);
    List<String> argsList = new ArrayList<>(List.of(args));
    argsList.add(0, label);
    if (rootCommand.matches(argsList)) {
      argsList.remove(0);
      rootCommand.execute(argsList);
    }
    return true;
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender,
      Command command,
      String alias,
      String[] args) {
    CommandNode<P> rootCommand = createRootCommand(sender);
    List<String> commandArgs = new ArrayList<>(List.of(args));
    commandArgs.add(0, alias);
    if (!rootCommand.matches(commandArgs)) {
      return Collections.emptyList();
    }
    return rootCommand.getTabCompletions(new ArrayList<>(List.of(args)));
  }

  private CommandNode<P> createRootCommand(CommandSender sender) {
    try {
      Constructor<? extends CommandNode<P>> constructor = rootCommandClass
          .getDeclaredConstructor(CommandSender.class, JavaPlugin.class);
      constructor.setAccessible(true);
      return constructor.newInstance(sender, plugin);
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException(
          "The root command must declare a constructor(CommandSender, plugin)", exception);
    }
  }
}