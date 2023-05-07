package xxAROX.WDUtils;

import dev.waterdog.waterdogpe.network.protocol.ProtocolCodecs;
import dev.waterdog.waterdogpe.plugin.Plugin;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketDefinition;
import org.cloudburstmc.protocol.bedrock.packet.UpdateSoftEnumPacket;
import xxAROX.WDUtils.commands.PluginsCommand;
import xxAROX.WDUtils.commands.ReloadCommand;

public class WDUtilsPlugin extends Plugin {

    @Override
    public void onStartup() {
        getDataFolder().delete();
    }

    @Override
    public void onEnable() {
        getProxy().getCommandMap().registerCommand(new ReloadCommand());
        getProxy().getCommandMap().registerCommand(new PluginsCommand());
        ProtocolCodecs.addUpdater((builder, bedrockCodec) -> {
            BedrockPacketDefinition<UpdateSoftEnumPacket> updateSoftEnumPacketBedrockPacketDefinition = bedrockCodec.getPacketDefinition(UpdateSoftEnumPacket.class);
            builder.registerPacket(UpdateSoftEnumPacket::new, updateSoftEnumPacketBedrockPacketDefinition.getSerializer(), updateSoftEnumPacketBedrockPacketDefinition.getId());
            return builder;
        });
    }
}
