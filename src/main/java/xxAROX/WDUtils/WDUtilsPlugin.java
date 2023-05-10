package xxAROX.WDUtils;

import dev.waterdog.waterdogpe.event.defaults.PlayerLoginEvent;
import dev.waterdog.waterdogpe.network.PacketDirection;
import dev.waterdog.waterdogpe.network.protocol.ProtocolCodecs;
import dev.waterdog.waterdogpe.plugin.Plugin;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketDefinition;
import org.cloudburstmc.protocol.bedrock.packet.ScriptCustomEventPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateSoftEnumPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import xxAROX.WDUtils.commands.PluginsCommand;
import xxAROX.WDUtils.commands.ReloadCommand;
import xxAROX.WDUtils.lang.LanguageManager;

public class WDUtilsPlugin extends Plugin {
    public static final String IDENTIFIER = "waterdogpe:";

    @Override
    public void onStartup() {
        saveResource("config.yml");
    }

    @Override
    public void onEnable() {
        getProxy().getCommandMap().registerCommand(new ReloadCommand());
        getProxy().getCommandMap().registerCommand(new PluginsCommand());
        ProtocolCodecs.addUpdater((builder, bedrockCodec) -> {
            BedrockPacketDefinition<UpdateSoftEnumPacket> updateSoftEnumPacketBedrockPacketDefinition = bedrockCodec.getPacketDefinition(UpdateSoftEnumPacket.class);
            builder.registerPacket(UpdateSoftEnumPacket::new, updateSoftEnumPacketBedrockPacketDefinition.getSerializer(), updateSoftEnumPacketBedrockPacketDefinition.getId());

            if (getConfig().getBoolean("enable-script-event-actions", false)) {
                BedrockPacketDefinition<ScriptCustomEventPacket> scriptCustomEventPacketBedrockPacketDefinition = bedrockCodec.getPacketDefinition(ScriptCustomEventPacket.class);
                builder.registerPacket(ScriptCustomEventPacket::new, scriptCustomEventPacketBedrockPacketDefinition.getSerializer(), scriptCustomEventPacketBedrockPacketDefinition.getId());
            }
            return builder;
        });
        if (getConfig().getBoolean("enable-script-event-actions", false)) {
            getProxy().getEventManager().subscribe(PlayerLoginEvent.class, event -> event.getPlayer().getPluginPacketHandlers().add((bedrockPacket, direction) -> {
                if (direction.equals(PacketDirection.FROM_SERVER) && bedrockPacket instanceof ScriptCustomEventPacket packet) {
                    switch (packet.getEventName().toLowerCase().replace(IDENTIFIER, "")) {
                        case "dispatch_command": {
                            getProxy().dispatchCommand(event.getPlayer(), packet.getData());
                            return PacketSignal.HANDLED;
                        }
                    }
                }
                return PacketSignal.UNHANDLED;
            }));
        }
    }
}
