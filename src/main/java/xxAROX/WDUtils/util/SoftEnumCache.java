package xxAROX.WDUtils.util;

import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import jline.internal.Nullable;
import lombok.Getter;
import org.cloudburstmc.protocol.bedrock.data.command.CommandEnumData;
import org.cloudburstmc.protocol.bedrock.data.command.SoftEnumUpdateType;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateSoftEnumPacket;

import java.util.HashMap;

public final class SoftEnumCache {
    @Getter
    private final static HashMap<String, CommandEnumData> enums = new HashMap<>();

    @Nullable
    public static CommandEnumData get(String name) {
        return enums.getOrDefault(name.toLowerCase(), null);
    }

    public static void add(CommandEnumData enumData) {
        enums.put(enumData.getName().toLowerCase(), enumData);
        broadcastSoftEnum(enumData, SoftEnumUpdateType.ADD);
    }

    public static void update(CommandEnumData enumData) {
        if (enums.containsKey(enumData.getName().toLowerCase())) {
            enums.put(enumData.getName().toLowerCase(), enumData);
            broadcastSoftEnum(enumData, SoftEnumUpdateType.REPLACE);
        } else add(enumData);
    }

    public static void remove(CommandEnumData enumData) {
        remove(enumData.getName());
    }

    public static void remove(String name) {
        if (enums.containsKey(name.toLowerCase())) {
            enums.remove(name.toLowerCase());
            broadcastSoftEnum(enums.get(name), SoftEnumUpdateType.REMOVE);
        }
    }


    public static void broadcastSoftEnum(CommandEnumData enumData, SoftEnumUpdateType type) {
        UpdateSoftEnumPacket packet = new UpdateSoftEnumPacket();
        packet.setSoftEnum(enumData);
        packet.setType(type);
        broadcastPacket(packet);
    }

    private static <T extends BedrockPacket> void broadcastPacket(T packet) {
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayerManager().getPlayers().values())
            player.sendPacket(packet);
    }
}
