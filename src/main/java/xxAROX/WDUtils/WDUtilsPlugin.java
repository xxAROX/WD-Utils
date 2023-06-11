package xxAROX.WDUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import dev.waterdog.waterdogpe.event.defaults.PlayerDisconnectedEvent;
import dev.waterdog.waterdogpe.event.defaults.PlayerLoginEvent;
import dev.waterdog.waterdogpe.network.PacketDirection;
import dev.waterdog.waterdogpe.network.protocol.ProtocolCodecs;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import dev.waterdog.waterdogpe.plugin.Plugin;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketDefinition;
import org.cloudburstmc.protocol.bedrock.codec.v575.serializer.PlayerAuthInputSerializer_v575;
import org.cloudburstmc.protocol.bedrock.packet.PlaySoundPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;
import org.cloudburstmc.protocol.bedrock.packet.ScriptCustomEventPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateSoftEnumPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import xxAROX.WDForms.WDForms;
import xxAROX.WDUtils.commands.PluginsCommand;
import xxAROX.WDUtils.commands.ReloadCommand;
import xxAROX.WDUtils.lang.LanguageManager;
import xxAROX.WDUtils.managers.PositionManager;

import java.util.ArrayList;
import java.util.List;

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
            BedrockPacketDefinition<PlayerAuthInputPacket> authInputDefinition = bedrockCodec.getPacketDefinition(PlayerAuthInputPacket.class);
            if (authInputDefinition == null) authInputDefinition = new BedrockPacketDefinition<>(0x90, PlayerAuthInputPacket::new, new PlayerAuthInputSerializer_v575());
            builder.registerPacket(PlayerAuthInputPacket::new, authInputDefinition.getSerializer(), authInputDefinition.getId());

            // ScriptCustomEventPacket (if enabled)
            if (getConfig().getBoolean("enable-script-event-actions", false)) {
                BedrockPacketDefinition<ScriptCustomEventPacket> scriptCustomEventDefinition = bedrockCodec.getPacketDefinition(ScriptCustomEventPacket.class);
                builder.registerPacket(ScriptCustomEventPacket::new, scriptCustomEventDefinition.getSerializer(), scriptCustomEventDefinition.getId());
            }
            return builder;
        });
        // TRANSLATE FORMS (when needed)
        WDForms forms = (WDForms) getProxy().getPluginManager().getPluginByName("WDForms");
        if (forms != null) {
            forms.registerTranslator((player, str) -> {
                String formData = str;
                for (LanguageManager language_manager : language_managers) formData = language_manager.translate(player, formData);
                return formData;
            });
        }
        getProxy().getEventManager().subscribe(PlayerDisconnectedEvent.class, playerDisconnectedEvent -> PositionManager.positions.remove(playerDisconnectedEvent.getPlayer().getXuid()));
        getProxy().getEventManager().subscribe(PlayerLoginEvent.class, event -> event.getPlayer().getPluginPacketHandlers().add((bedrockPacket, direction) -> {
            if (direction.equals(PacketDirection.FROM_USER) && bedrockPacket instanceof PlayerAuthInputPacket packet) PositionManager.cache(event.getPlayer(), packet);
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
                        default: {
                            // ignore
                        }
                    }
                }
                return PacketSignal.UNHANDLED;
            }
            return PacketSignal.UNHANDLED;
        }));
        getProxy().getCommandMap().registerCommand(new ReloadCommand());
        getProxy().getCommandMap().registerCommand(new PluginsCommand());
    }

    public static void play_sound(ProxiedPlayer player, String sound_name, float volume, float pitch) {
        if (!PositionManager.positions.containsKey(player.getXuid())) return;
        PlaySoundPacket packet = new PlaySoundPacket();
        packet.setPosition(PositionManager.get(player));
        packet.setSound(sound_name);
        packet.setVolume(volume);
        packet.setPitch(pitch);
        player.sendPacketImmediately(packet);
    }
    public static void play_sound(ProxiedPlayer player, String sound_name, float volume) {
        play_sound(player, sound_name, volume, 1);
    }
    public static void play_sound(ProxiedPlayer player, String sound_name) {
        play_sound(player, sound_name, 1, 1);
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
