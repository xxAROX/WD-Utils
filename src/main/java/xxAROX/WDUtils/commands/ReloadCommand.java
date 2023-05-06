package xxAROX.WDUtils.commands;

import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.command.Command;
import dev.waterdog.waterdogpe.command.CommandSender;
import dev.waterdog.waterdogpe.command.CommandSettings;
import dev.waterdog.waterdogpe.command.ConsoleCommandSender;
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
        String reloading = "Reloading plugins..";
        String reloaded = "Reloaded!";

        if (!(commandSender instanceof ConsoleCommandSender)) commandSender.getProxy().getConsoleSender().sendMessage(reloading);
        commandSender.sendMessage(reloading);
        ProxyServer.getInstance().getPluginManager().disableAllPlugins();
        ProxyServer.getInstance().getPluginManager().enableAllPlugins();
        if (!(commandSender instanceof ConsoleCommandSender)) commandSender.getProxy().getConsoleSender().sendMessage(reloaded);
        commandSender.sendMessage(reloaded);
        return true;
    }
}
