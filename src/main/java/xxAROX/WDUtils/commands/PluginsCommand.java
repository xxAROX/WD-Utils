package xxAROX.WDUtils.commands;

import dev.waterdog.waterdogpe.command.Command;
import dev.waterdog.waterdogpe.command.CommandSender;
import dev.waterdog.waterdogpe.command.CommandSettings;
import dev.waterdog.waterdogpe.plugin.Plugin;
import xxAROX.WDUtils.util.Permissions;

public class PluginsCommand extends Command {
    public PluginsCommand() {
        super(
                "wdplugins",
                CommandSettings.builder()
                        .setDescription("Show plugins")
                        .setAliases("wdpl")
                        .setPermission(Permissions.plugins)
                        .build()
        );
    }
    @Override
    public boolean onExecute(CommandSender commandSender, String aliasUsed, String[] args) {
        StringBuilder list = new StringBuilder();
        var plugins = commandSender.getProxy().getPluginManager().getPlugins();
        for (Plugin plugin : plugins) {
            if (list.length() > 0) list.append("§f, ");
            list.append(plugin.isEnabled() ? "§a" : "§c").append(plugin.getDescription().getName()).append(" ").append(plugin.getDescription().getVersion());
        }
        commandSender.sendMessage("Plugins (" + String.valueOf(plugins.size()) + "): " + list);
        return true;
    }
}
