package me.general_viski.links;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Links extends JavaPlugin {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private FileConfiguration config;
    private final String PERMISSION_USE = "linksplugin.use";
    private final String PERMISSION_RELOAD = "linksplugin.reload";

    @Override
    public void onEnable() {
        loadConfig();
        getLogger().info("LinksPlugin включен!");
    }

    private void loadConfig() {
        saveDefaultConfig();
        reloadConfig();
        config = getConfig();

        config.addDefault("messages.no_permission", "<red>У вас нет прав!");
        config.addDefault("messages.no_links", "<yellow>Нет ссылок в конфиге");
        config.addDefault("messages.header", "<gradient:green:blue><bold>Ссылки сервера:");
        config.addDefault("messages.link_hover", "<gray>Кликните чтобы открыть");
        config.addDefault("messages.reload_success", "<green>Конфиг перезагружен!");
        config.addDefault("links", List.of(
                "<gradient:green:blue>https://example.com</gradient>",
                "<gradient:red:blue>https://example.org</gradient>",
                "<gradient:gold:yellow>https://example.net</gradient>"
        ));
        config.options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("links")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                return handleReload(sender);
            }
            return handleLinksCommand(sender);
        }
        return false;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission(PERMISSION_RELOAD)) {
            sendMessage(sender, config.getString("messages.no_permission"));
            return true;
        }

        try {
            loadConfig();
            sendMessage(sender, config.getString("messages.reload_success"));
        } catch (Exception e) {
            sendMessage(sender, "<red>Ошибка: " + e.getMessage());
        }
        return true;
    }

    private boolean handleLinksCommand(CommandSender sender) {
        if (!sender.hasPermission(PERMISSION_USE)) {
            sendMessage(sender, config.getString("messages.no_permission"));
            return true;
        }

        List<String> links = config.getStringList("links");

        if (links.isEmpty()) {
            sendMessage(sender, config.getString("messages.no_links"));
        } else {
            sendMessage(sender, "");
            sendMessage(sender, config.getString("messages.header"));

            Component hoverText = miniMessage.deserialize(config.getString("messages.link_hover"));

            links.forEach(formattedLink -> {
                // Извлекаем чистый URL из форматированной строки
                String plainUrl = formattedLink.replaceAll("<[^>]+>", "");

                // Создаем компонент с сохранением форматирования
                Component linkComponent = miniMessage.deserialize(formattedLink)
                        .clickEvent(ClickEvent.openUrl(plainUrl))
                        .hoverEvent(HoverEvent.showText(hoverText));

                sender.sendMessage(linkComponent);
            });
        }
        return true;
    }

    private void sendMessage(CommandSender sender, String message) {
        if (message != null) {
            sender.sendMessage(miniMessage.deserialize(message));
        }
    }
}