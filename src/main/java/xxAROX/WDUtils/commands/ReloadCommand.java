package xxAROX.WDUtils.commands;

import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.command.Command;
import dev.waterdog.waterdogpe.command.CommandSender;
import dev.waterdog.waterdogpe.command.CommandSettings;
import xxAROX.WDUtils.util.Permissions;

public class ReloadCommand extends Command {
    public ReloadCommand() {
        super(
                "wdreload",
                CommandSettings.builder()
                        .setDescription("Reload plugins")
                        .setAliases("wdrl")
                        .setPermission(Permissions.reload)
                        .build()
        );
    }
    @Override
    public boolean onExecute(CommandSender commandSender, String s, String[] strings) {
        commandSender.sendMessage("Reloading plugins..");
        commandSender.sendMessage("§7§oNOTE: please dont use this in production-mode, because this command is only for development!");
        ProxyServer.getInstance().getPluginManager().disableAllPlugins();
        ProxyServer.getInstance().getPluginManager().enableAllPlugins();
        commandSender.sendMessage("Reloaded!");
        return true;
    }
}
