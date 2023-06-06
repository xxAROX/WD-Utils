package xxAROX.WDUtils.managers;

import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import jline.internal.Nullable;
import lombok.*;
import lombok.experimental.Accessors;
import org.cloudburstmc.math.GenericMath;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;

import java.util.HashMap;

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
    public static final class Position extends Vector3f{
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

        @NonNull
        public Vector3f add(float x, float y, float z) {
            return Vector3f.from(this.getX() + x, this.getY() + y, this.getZ() + z);
        }

        @NonNull
        public Vector3f sub(float x, float y, float z) {
            return Vector3f.from(this.getX() - x, this.getY() - y, this.getZ() - z);
        }

        @NonNull
        public Vector3f mul(float x, float y, float z) {
            return Vector3f.from(this.getX() * x, this.getY() * y, this.getZ() * z);
        }

        @NonNull
        public Vector3f div(float x, float y, float z) {
            return Vector3f.from(this.getX() / x, this.getY() / y, this.getZ() / z);
        }

        @NonNull
        public Vector3f project(float x, float y, float z) {
            float lengthSquared = x * x + y * y + z * z;
            if (Math.abs(lengthSquared) < GenericMath.FLT_EPSILON) {
                throw new ArithmeticException("Cannot project onto the zero vector");
            } else {
                float a = this.dot(x, y, z) / lengthSquared;
                return Vector3f.from(a * x, a * y, a * z);
            }
        }

        @NonNull
        public Vector3f cross(float x, float y, float z) {
            return Vector3f.from(this.getY() * z - this.getZ() * y, this.getZ() * x - this.getX() * z, this.getX() * y - this.getY() * x);
        }

        @NonNull
        public Vector3f pow(float power) {
            return Vector3f.from(Math.pow((double)this.x, (double)power), Math.pow((double)this.y, (double)power), Math.pow((double)this.z, (double)power));
        }

        @NonNull
        public Vector3f ceil() {
            return Vector3f.from(Math.ceil((double)this.getX()), Math.ceil((double)this.getY()), Math.ceil((double)this.getZ()));
        }

        @NonNull
        public Vector3f floor() {
            return Vector3f.from((float)GenericMath.floor(this.getX()), (float)GenericMath.floor(this.getY()), (float)GenericMath.floor(this.getZ()));
        }

        @NonNull
        public Vector3f round() {
            return Vector3f.from((float)Math.round(this.getX()), (float)Math.round(this.getY()), (float)Math.round(this.getZ()));
        }

        @NonNull
        public Vector3f abs() {
            return Vector3f.from(Math.abs(this.getX()), Math.abs(this.getY()), Math.abs(this.getZ()));
        }

        @NonNull
        public Vector3f negate() {
            return Vector3f.from(-this.getX(), -this.getY(), -this.getZ());
        }

        @NonNull
        public Vector3f min(float x, float y, float z) {
            return Vector3f.from(Math.min(this.getX(), x), Math.min(this.getY(), y), Math.min(this.getZ(), z));
        }

        @NonNull
        public Vector3f max(float x, float y, float z) {
            return Vector3f.from(Math.max(this.getX(), x), Math.max(this.getY(), y), Math.max(this.getZ(), z));
        }

        @NonNull
        public Vector3f up(float v) {
            return Vector3f.from(this.getX(), this.getY() + v, this.getZ());
        }

        @NonNull
        public Vector3f down(float v) {
            return Vector3f.from(this.getX(), this.getY() - v, this.getZ());
        }

        @NonNull
        public Vector3f north(float v) {
            return Vector3f.from(this.getX(), this.getY(), this.getZ() - v);
        }

        @NonNull
        public Vector3f south(float v) {
            return Vector3f.from(this.getX(), this.getY(), this.getZ() + v);
        }

        @NonNull
        public Vector3f east(float v) {
            return Vector3f.from(this.getX() + v, this.getY(), this.getZ());
        }

        @NonNull
        public Vector3f west(float v) {
            return Vector3f.from(this.getX() - v, this.getY(), this.getZ());
        }

        @NonNull
        public Vector3f normalize() {
            float length = this.length();
            if (Math.abs(length) < GenericMath.FLT_EPSILON) {
                throw new ArithmeticException("Cannot normalize the zero vector");
            } else {
                return Vector3f.from(this.getX() / length, this.getY() / length, this.getZ() / length);
            }
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            else if (!(o instanceof Vector3f vector3)) return false;
            else {
                if (Float.compare(vector3.getX(), this.x) != 0) return false;
                else if (Float.compare(vector3.getY(), this.y) != 0) return false;
                else return Float.compare(vector3.getZ(), this.z) == 0;
            }
        }
    }
}
