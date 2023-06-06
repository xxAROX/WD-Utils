package xxAROX.WDUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import dev.waterdog.waterdogpe.event.defaults.PlayerLoginEvent;
import dev.waterdog.waterdogpe.network.PacketDirection;
import dev.waterdog.waterdogpe.network.protocol.ProtocolCodecs;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import dev.waterdog.waterdogpe.plugin.Plugin;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketDefinition;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;
import org.cloudburstmc.protocol.bedrock.packet.ScriptCustomEventPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateSoftEnumPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import xxAROX.WDForms.event.FormSendEvent;
import xxAROX.WDUtils.commands.PluginsCommand;
import xxAROX.WDUtils.commands.ReloadCommand;
import xxAROX.WDUtils.lang.LanguageManager;
import xxAROX.WDUtils.managers.PositionManager;

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
            // UpdateSoftEnumPacket
            BedrockPacketDefinition<UpdateSoftEnumPacket> updateSoftEnumDefinition = bedrockCodec.getPacketDefinition(UpdateSoftEnumPacket.class);
            builder.registerPacket(UpdateSoftEnumPacket::new, updateSoftEnumDefinition.getSerializer(), updateSoftEnumDefinition.getId());

            // PlayerAuthInputPacket
            BedrockPacketDefinition<PlayerAuthInputPacket> playerAuthInputPacketBedrockPacketDefinition = bedrockCodec.getPacketDefinition(PlayerAuthInputPacket.class);
            builder.registerPacket(PlayerAuthInputPacket::new, playerAuthInputPacketBedrockPacketDefinition.getSerializer(), playerAuthInputPacketBedrockPacketDefinition.getId());

            // ScriptCustomEventPacket (if enabled)
            if (getConfig().getBoolean("enable-script-event-actions", false)) {
                BedrockPacketDefinition<ScriptCustomEventPacket> scriptCustomEventPacketBedrockPacketDefinition = bedrockCodec.getPacketDefinition(ScriptCustomEventPacket.class);
                builder.registerPacket(ScriptCustomEventPacket::new, scriptCustomEventPacketBedrockPacketDefinition.getSerializer(), scriptCustomEventPacketBedrockPacketDefinition.getId());
            }
            return builder;
        });
        // TRANSLATE FORMS (when needed)
        try {
            Class.forName(String.valueOf(FormSendEvent.class));
            getProxy().getEventManager().subscribe(FormSendEvent.class, event -> {
                AtomicReference<String> formData = new AtomicReference<>(event.getFormRequestPacket().getFormData());
                language_managers.forEach(lm -> {
                    if (lm.getOptions().isTranslate_forms()) formData.set(lm.translate(event.getPlayer(), formData.get()));
                });
                event.getFormRequestPacket().setFormData(formData.get());
            });
        } catch (ClassNotFoundException e) {
            // ignore
        }
        getProxy().getEventManager().subscribe(PlayerLoginEvent.class, event -> {
            event.getPlayer().getPluginPacketHandlers().add((bedrockPacket, direction) -> {
                if (bedrockPacket instanceof PlayerAuthInputPacket packet) PositionManager.cache(event.getPlayer(), packet);

                if (getConfig().getBoolean("enable-script-event-actions", false)) {
                    if (direction.equals(PacketDirection.FROM_SERVER) && bedrockPacket instanceof ScriptCustomEventPacket packet) {
                        if (!packet.getEventName().toLowerCase().startsWith(IDENTIFIER)) return PacketSignal.HANDLED; // dylan thinks this is a serverbound packet

                        switch (packet.getEventName().toLowerCase().replace(IDENTIFIER, "")) {
                            case "dispatch_command": {
                                getProxy().dispatchCommand(event.getPlayer(), packet.getData());
                                return PacketSignal.HANDLED;
                            }
                            case "set_permissions": {
                                try {
                                    JsonArray perms = new Gson().fromJson(packet.getData(), JsonArray.class);
                                    for (String perm : perms.asList().stream().map(JsonElement::getAsString).toList()) {
                                        if (!event.getPlayer().hasPermission(perm)) event.getPlayer().addPermission(perm);
                                    }

                                } catch (JsonSyntaxException e) {
                                    // ignore
                                }
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

    public static void dispatch_command(ProxiedPlayer player, String command_line){
        if (player.getDownstreamConnection() != null) {
            if (!command_line.startsWith("/")) command_line = "/" + command_line;
            ScriptCustomEventPacket packet = new ScriptCustomEventPacket();
            packet.setEventName(IDENTIFIER + "dispatch_command");
            packet.setData(command_line);
            player.getDownstreamConnection().sendPacket(packet);
        }
    }

    public static void sync_permissions(ProxiedPlayer player){
        if (player.getDownstreamConnection() != null) {
            ScriptCustomEventPacket packet = new ScriptCustomEventPacket();
            packet.setEventName(IDENTIFIER + "sync_permissions");
            packet.setData("NO EVENT DATA NEEDED, AND FUN-FACT THIS DOESN'T TAKE ANY TRAFFIC :O");
            player.getDownstreamConnection().sendPacket(packet);
        }
    }
}
