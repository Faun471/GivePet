package me.faun.givepet.commands;

public record Command(String name, String[] alias, String description, String permission, String usage) {
}
