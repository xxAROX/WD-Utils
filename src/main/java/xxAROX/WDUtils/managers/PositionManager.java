package xxAROX.WDUtils.managers;

import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import jline.internal.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;

import java.util.HashMap;
import java.util.Objects;

public final class PositionManager {
    public static final HashMap<String, Position> positions = new HashMap<>();

    public static void cache(ProxiedPlayer player, PlayerAuthInputPacket packet) {
        int yaw = Math.floorMod((int) packet.getRotation().getX(), 360);
        int pitch = Math.floorMod((int) packet.getRotation().getY(), 360);
        if (!positions.containsKey(player.getXuid())) positions.put(player.getXuid(), new Position(
                packet.getPosition().getX(),
                packet.getPosition().getY(),
                packet.getPosition().getZ(),
                yaw,
                pitch
        ));
    }

    @Nullable
    public static Position get(ProxiedPlayer player) {
        return positions.getOrDefault(player.getXuid(), null);
    }

    @AllArgsConstructor @ToString
    @Getter @Setter @Accessors(chain = true)
    public static final class Position {
        private float x;
        private float y;
        private float z;
        private int yaw;
        private int pitch;
        public static Position fromVector3(Vector3f vector3){
            return fromVector3(vector3, false);
        }
        public static Position fromVector3(Vector3f vector3, boolean floor){
            return new Position(
                    floor ? vector3.getFloorX() : vector3.getX(),
                    floor ? vector3.getFloorY() : vector3.getY(),
                    floor ? vector3.getFloorZ() : vector3.getZ(),
                    0,
                    0
            );
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Position position = (Position) o;
            return Float.compare(position.x, x) == 0
                    && Float.compare(position.y, y) == 0
                    && Float.compare(position.z, z) == 0
                    && yaw == position.yaw
                    && pitch == position.pitch
            ;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z, yaw, pitch);
        }
    }
}
