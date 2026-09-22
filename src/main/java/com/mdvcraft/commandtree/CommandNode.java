package com.mdvcraft.commandtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

public abstract class CommandNode<P extends JavaPlugin> {
  protected final CommandSender sender;
  protected final P plugin;

  protected CommandNode(CommandSender sender, P plugin) {
    this.plugin = plugin;
    this.sender = sender;
  }

  public abstract String getName();

  public List<String> getAliases() {
    return List.of();
  }

  public List<CommandNode<P>> getSubCommands() {
    return List.of();
  }

  public List<String> getOptions() {
    return List.of();
  }

  protected boolean executeSubcommands(List<String> args) {
    for (CommandNode<P> command : getSubCommands()) {
      if (command.matches(args)) {
        args.remove(0);
        command.execute(args);
        return true;
      }
    }
    return false;
  }

  public boolean matches(List<String> args) {
    if (args.isEmpty()) {
      return false;
    }

    String first = args.get(0);
    if (first.equalsIgnoreCase(getName())) {
      return true;
    }

    for (String alias : getAliases()) {
      if (first.equalsIgnoreCase(alias)) {
        return true;
      }
    }

    return false;
  }

  public abstract boolean execute(List<String> args);

  public List<String> getTabCompletions(List<String> args) {
    if (args == null || args.isEmpty() || !hasPermission()) {
      return getCompletions("");
    }

    if (args.size() > 1) {
      for (CommandNode<P> subCommand : getSubCommands()) {
        if (subCommand.matches(args)) {
          return subCommand.getTabCompletions(args.subList(1, args.size()));
        }
      }
      return List.of();
    }

    return getCompletions(args.get(0));
  }

  private List<String> getCompletions(String partial) {
    List<String> completions = new ArrayList<>();
    for (CommandNode<P> subCommand : getSubCommands()) {
      completions.add(subCommand.getName());
      completions.addAll(subCommand.getAliases());
    }
    completions.addAll(getOptions());

    List<String> matches = new ArrayList<>();
    StringUtil.copyPartialMatches(partial, completions, matches);
    Collections.sort(matches);
    return matches;
  }

  protected boolean hasPermission() {
    return sender.hasPermission(getName() + ".admin");
  }

  protected void throwMissingArgumentsError() {
    sendError("Missing arguments for command: " + getName());
    printValidArgumentsList();
  }

  protected void throwInvalidArgumentError() {
    sendError("Invalid argument for command: " + getName());
    printValidArgumentsList();
  }

  protected void throwExtraArgumentsError() {
    sendError("Extra arguments provided for command: " + getName());
    printValidArgumentsList();
  }

  protected void sendError(String message) {
    sender.sendMessage("§c" + message);
  }

  protected void printValidArgumentsList() {
    List<CommandNode<P>> subCommands = getSubCommands();
    List<String> options = getOptions();
    if (subCommands.isEmpty() && options.isEmpty()) {
      sendError("This command has no valid arguments.");
      return;
    }

    StringBuilder message = new StringBuilder("Valid arguments for ")
        .append(getName()).append(": ");
    if (!subCommands.isEmpty()) {
      List<String> subCommandNames = new ArrayList<>();
      for (CommandNode<P> subCommand : subCommands) {
        subCommandNames.add(subCommand.getName());
      }
      message.append("[").append(String.join(" | ", subCommandNames)).append("]");
    }
    if (!options.isEmpty()) {
      if (!subCommands.isEmpty()) {
        message.append(" or ");
      }
      message.append("[").append(String.join(" | ", options)).append("]");
    }
    sendError(message.toString());
  }
}