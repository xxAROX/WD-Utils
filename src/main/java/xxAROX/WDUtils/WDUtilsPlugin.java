package xxAROX.WDUtils;

import dev.waterdog.waterdogpe.plugin.Plugin;
import xxAROX.WDUtils.commands.ReloadCommand;

public class WDUtilsPlugin extends Plugin {
    @Override
    public void onEnable() {
        getProxy().getCommandMap().registerCommand(new ReloadCommand());
    }
}
