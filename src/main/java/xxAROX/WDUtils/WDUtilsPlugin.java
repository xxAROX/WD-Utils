package xxAROX.WDUtils;

import dev.waterdog.waterdogpe.event.defaults.PlayerLoginEvent;
import dev.waterdog.waterdogpe.network.PacketDirection;
import dev.waterdog.waterdogpe.network.protocol.ProtocolCodecs;
import dev.waterdog.waterdogpe.plugin.Plugin;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketDefinition;
import org.cloudburstmc.protocol.bedrock.packet.*;
import org.cloudburstmc.protocol.common.PacketSignal;
import xxAROX.WDForms.event.FormSendEvent;
import xxAROX.WDUtils.commands.PluginsCommand;
import xxAROX.WDUtils.commands.ReloadCommand;
import xxAROX.WDUtils.lang.LanguageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class WDUtilsPlugin extends Plugin {
    public static final String IDENTIFIER = "waterdogpe:";
    public static final List<LanguageManager> language_managers = new ArrayList<>();

    @Override
    public void onStartup() {
        saveResource("config.yml");
    }

    @Override
    public void onEnable() {
        ProtocolCodecs.addUpdater((builder, bedrockCodec) -> {
            BedrockPacketDefinition<UpdateSoftEnumPacket> updateSoftEnumDefinition = bedrockCodec.getPacketDefinition(UpdateSoftEnumPacket.class);
            builder.registerPacket(UpdateSoftEnumPacket::new, updateSoftEnumDefinition.getSerializer(), updateSoftEnumDefinition.getId());

            BedrockPacketDefinition<TextPacket> textPacketBedrockPacketDefinition = bedrockCodec.getPacketDefinition(TextPacket.class);
            builder.registerPacket(TextPacket::new, textPacketBedrockPacketDefinition.getSerializer(), textPacketBedrockPacketDefinition.getId());
            BedrockPacketDefinition<SetTitlePacket> setTitlePacketBedrockPacketDefinition = bedrockCodec.getPacketDefinition(SetTitlePacket.class);
            builder.registerPacket(SetTitlePacket::new, setTitlePacketBedrockPacketDefinition.getSerializer(), setTitlePacketBedrockPacketDefinition.getId());
            BedrockPacketDefinition<ModalFormRequestPacket> modalFormRequestPacketBedrockPacketDefinition = bedrockCodec.getPacketDefinition(ModalFormRequestPacket.class);
            builder.registerPacket(ModalFormRequestPacket::new, modalFormRequestPacketBedrockPacketDefinition.getSerializer(), modalFormRequestPacketBedrockPacketDefinition.getId());

            if (getConfig().getBoolean("enable-script-event-actions", false)) {
                BedrockPacketDefinition<ScriptCustomEventPacket> scriptCustomEventPacketBedrockPacketDefinition = bedrockCodec.getPacketDefinition(ScriptCustomEventPacket.class);
                builder.registerPacket(ScriptCustomEventPacket::new, scriptCustomEventPacketBedrockPacketDefinition.getSerializer(), scriptCustomEventPacketBedrockPacketDefinition.getId());
            }
            return builder;
        });
        getProxy().getEventManager().subscribe(FormSendEvent.class, event -> {
            AtomicReference<String> formData = new AtomicReference<>(event.getFormRequestPacket().getFormData());
            language_managers.forEach(lm -> {
                if (lm.getOptions().isTranslate_forms()) formData.set(lm.translate(event.getPlayer(), formData.get()));
            });
            event.getFormRequestPacket().setFormData(formData.get());
        });
        getProxy().getEventManager().subscribe(PlayerLoginEvent.class, event -> {
            event.getPlayer().getPluginPacketHandlers().add((bedrockPacket, direction) -> {
                if (getConfig().getBoolean("enable-script-event-actions", false)) {
                    if (direction.equals(PacketDirection.FROM_SERVER) && bedrockPacket instanceof ScriptCustomEventPacket packet) {
                        switch (packet.getEventName().toLowerCase().replace(IDENTIFIER, "")) {
                            case "dispatch_command": {
                                getProxy().dispatchCommand(event.getPlayer(), packet.getData());
                                return PacketSignal.HANDLED;
                            }
                        }
                    }
                    return PacketSignal.UNHANDLED;
                }
                return PacketSignal.UNHANDLED;
            });
        });
        getProxy().getCommandMap().registerCommand(new ReloadCommand());
        getProxy().getCommandMap().registerCommand(new PluginsCommand());
    }
}
