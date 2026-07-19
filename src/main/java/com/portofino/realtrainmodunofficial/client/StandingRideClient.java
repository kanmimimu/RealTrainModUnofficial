package com.portofino.realtrainmodunofficial.client;

import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.modelpack.cfg.TrainConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

public final class StandingRideClient {

    private static final double SURFACE_MARGIN = 0.3D;
    private static final double RIDE_MARGIN = 1.0D;
    private static final double BOARDING_MARGIN = 1.3D;
    private static final double SIDE_MARGIN = 0.15D;
    private static final double Y_CORRECTION_FACTOR = 0.35D;
    private static final double MAX_Y_CORRECTION = 0.6D;
    private static final double BOARDING_Y_CORRECTION = 1.5D;
    private static final float STOP_SPEED_THRESHOLD = 0.01F;
    private static final double STOP_MOVE_THRESHOLD = 0.05D;

    private static final Map<EntityTrainBase, double[]> PREV = new WeakHashMap<>();
    private static long lastCarriedGameTime = -1000L;
    private static long lastRideGameTime = -1000L;

    private StandingRideClient() {
    }

    public static void tick(EntityTrainBase train) {
        double curX = train.getX();
        double curY = train.getY();
        double curZ = train.getZ();
        float curYaw = train.getYRot();

        double[] prev = PREV.get(train);
        if (prev == null) {
            PREV.put(train, new double[]{curX, curY, curZ, curYaw});
            return;
        }
        double dx = curX - prev[0];
        double dy = curY - prev[1];
        double dz = curZ - prev[2];
        float dYaw = Mth.wrapDegrees(curYaw - (float) prev[3]);
        double prevX = prev[0];
        double prevZ = prev[2];
        prev[0] = curX;
        prev[1] = curY;
        prev[2] = curZ;
        prev[3] = curYaw;

        Player player = Minecraft.getInstance().player;
        if (player == null || !player.isAlive() || player.isPassenger()
                || player.isSpectator() || player.getAbilities().flying
                || player.level() != train.level()) {
            return;
        }

        long now = train.level().getGameTime();
        boolean rideHold = (now - lastRideGameTime) <= 2L;
        double yMargin = rideHold ? RIDE_MARGIN : SURFACE_MARGIN;
        boolean isTrainStopped = isTrainStopped(train, dx, dy, dz);

        if (!isStandingOn(train, player, rideHold, yMargin, isTrainStopped)) {
            return;
        }
        if (lastCarriedGameTime == now) {
            return;
        }
        lastCarriedGameTime = now;
        lastRideGameTime = now;

        carry(player, train, prevX, prevZ, dx, dy, dz, dYaw, isTrainStopped);
    }

    private static boolean isStandingOn(EntityTrainBase train, Player player, boolean rideHold, double yMargin, boolean isTrainStopped) {
        double relX = player.getX() - train.getX();
        double relZ = player.getZ() - train.getZ();

        double rad = Math.toRadians(train.getYRot());
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);
        double localLength = relX * sin + relZ * cos;
        double localWidth = relX * cos - relZ * sin;

        double halfWidth = EntityTrainBase.TRAIN_WIDTH / 2.0D + SIDE_MARGIN;
        double halfLength = getHalfLength(train);
        double widthMargin = rideHold ? halfWidth + 0.5D : halfWidth;
        double lengthMargin = rideHold ? halfLength + 0.3D : halfLength;

        if (Math.abs(localWidth) > widthMargin || Math.abs(localLength) > lengthMargin) {
            return false;
        }

        double feetY = player.getBoundingBox().minY;
        double floorY = train.getInteriorFloorY();

        // 乗り込みモード: 停止中で、車両側面にいて、車両方向に移動中
        boolean isBoarding = isTrainStopped && isMovingTowardsTrain(train, player, localWidth, localLength, halfWidth, halfLength);
        double effectiveMargin = isBoarding ? BOARDING_MARGIN : yMargin;

        return feetY >= floorY - effectiveMargin && feetY <= floorY + 0.5D;
    }

    private static boolean isTrainStopped(EntityTrainBase train, double dx, double dy, double dz) {
        float speed = train.getSpeed();
        if (Math.abs(speed) < STOP_SPEED_THRESHOLD) {
            return true;
        }
        double moveDistance = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
        return moveDistance < STOP_MOVE_THRESHOLD;
    }

    private static boolean isMovingTowardsTrain(EntityTrainBase train, Player player, double localWidth, double localLength, double halfWidth, double halfLength) {
        // プレイヤーが車両の外側にいて、内側に向かっているかチェック
        double playerSpeedX = player.getDeltaMovement().x;
        double playerSpeedZ = player.getDeltaMovement().z;

        double rad = Math.toRadians(train.getYRot());
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);

        // プレイヤーの速度を車両ローカル座標に変換
        double localSpeedWidth = playerSpeedX * cos - playerSpeedZ * sin;
        double localSpeedLength = playerSpeedX * sin + playerSpeedZ * cos;

        // 幅方向: 外側から内側へ移動中（絶対値が減少）
        boolean movingInWidth = (localWidth > 0 && localSpeedWidth < 0) || (localWidth < 0 && localSpeedWidth > 0);
        // 長さ方向: 車両の範囲内にいる
        boolean inLengthRange = Math.abs(localLength) <= halfLength;

        return movingInWidth && inLengthRange;
    }

    private static double getHalfLength(EntityTrainBase train) {
        TrainConfig cfg = train.getConfig();
        if (cfg != null && cfg.trainDistance > 0.0F) {
            return cfg.trainDistance;
        }
        return EntityTrainBase.TRAIN_WIDTH / 2.0D;
    }

    private static void carry(Player player, EntityTrainBase train, double prevX, double prevZ,
                             double dx, double dy, double dz, float dYaw, boolean isTrainStopped) {
        double addX;
        double addZ;
        if (dYaw != 0.0F) {
            double relX = player.getX() - prevX;
            double relZ = player.getZ() - prevZ;
            double rad = Math.toRadians(dYaw);
            double cos = Math.cos(rad);
            double sin = Math.sin(rad);
            double rotX = relX * cos + relZ * sin;
            double rotZ = relZ * cos - relX * sin;
            addX = (train.getX() + rotX) - player.getX();
            addZ = (train.getZ() + rotZ) - player.getZ();
        } else {
            addX = dx;
            addZ = dz;
        }

        double floorY = train.getInteriorFloorY();
        double yError = floorY - player.getY();
        double maxYCorrection = isTrainStopped ? BOARDING_Y_CORRECTION : MAX_Y_CORRECTION;
        double yCorrection = Mth.clamp(yError * Y_CORRECTION_FACTOR, -maxYCorrection, maxYCorrection);

        player.move(MoverType.SELF, new Vec3(addX, dy + yCorrection, addZ));

        Vec3 dm = player.getDeltaMovement();
        player.setDeltaMovement(dm.x, 0.0D, dm.z);
        player.fallDistance = 0.0F;
        player.setOnGround(true);
    }
}
