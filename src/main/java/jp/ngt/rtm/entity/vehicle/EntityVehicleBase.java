package jp.ngt.rtm.entity.vehicle;

import jp.ngt.rtm.modelpack.cfg.TrainConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * 本家 jp.ngt.rtm.entity.vehicle.EntityVehicleBase の最小移植 (Phase 2)。
 * onUpdate 骨格: onVehicleUpdate → (server) updateMovement → applyPhysicalEffect。
 * モデル/リソース状態/GUI 系は段階移植 (TODO Phase 4)。
 *
 * @param <T> 本家は VehicleBaseConfig; 当面 TrainConfig のみ。
 */
public abstract class EntityVehicleBase<T extends TrainConfig> extends Entity {
    public static final int MAX_SEAT_ROTATION = 45;
    public static final int MAX_DOOR_MOVE = 60;
    public static final int MAX_PANTOGRAPH_MOVE = 40;
    public static final float TO_ANGULAR_VELOCITY = (float) (360.0D / Math.PI);

    public float rotationRoll;
    public float prevRotationRoll;

    //クライアントアニメ (本家 updateAnimation が更新、スクリプトが参照)
    public int seatRotation;
    public int doorMoveL;
    public int doorMoveR;
    public int pantograph_F;
    public int pantograph_B;
    public float wheelRotationR;
    public float wheelRotationL;

    //パックスクリプト互換 (1.7.10 SRG 名を直接参照するスクリプトのため tick 毎に更新)
    public Level field_70170_p;
    public int field_70173_aa;
    public float field_70177_z;
    public float field_70125_A;
    public Entity field_70153_n;

    private final jp.ngt.rtm.modelpack.state.ResourceState resourceState =
            new jp.ngt.rtm.modelpack.state.ResourceState(this::getResourceName);

    public float prevRotationYawVehicle;
    public float prevRotationPitchVehicle;

    //本家 setPositionAndRotation2/updatePosAndRotationClient のクライアント補間
    protected int vehiclePosRotationInc;
    protected double vehicleX, vehicleY, vehicleZ;
    protected float vehicleYaw, vehiclePitch, vehicleRoll;

    public EntityVehicleBase(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /**
     * 本家 getModelSet().getConfig() 相当。暫定: サブクラスが供給。
     */
    public abstract T getConfig();

    @Override
    public void tick() {
        this.prevRotationRoll = this.rotationRoll;
        this.prevRotationYawVehicle = this.getYRot();
        this.prevRotationPitchVehicle = this.getXRot();

        this.baseTick();

        if (this.level().isClientSide) {
            this.updatePosAndRotationClient();
            this.updateAnimation();
        }

        this.onVehicleUpdate();

        if (!this.level().isClientSide) {
            this.updateMovement();
        }

        this.applyPhysicalEffect();

        //パックスクリプト互換 SRG フィールドの更新
        this.field_70170_p = this.level();
        this.field_70173_aa = this.tickCount;
        this.field_70177_z = this.getYRot();
        this.field_70125_A = this.getXRot();
        this.field_70153_n = this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
    }

    /**
     * 本家 updateAnimation (Client): 車輪回転。ドア/パンタ/座席はサブクラス。
     */
    protected void updateAnimation() {
        float speed = this.getVehicleSpeed();
        float f0 = speed * TO_ANGULAR_VELOCITY * this.getConfig().wheelRotationSpeed * this.getMoveDir();
        this.wheelRotationR = (this.wheelRotationR + f0) % 360.0F;
        this.wheelRotationL = (this.wheelRotationL + f0) % 360.0F;
    }

    protected float getMoveDir() {
        return 1.0F;
    }

    /**
     * 本家 getSpeed 相当 (アニメ用)。
     */
    public float getVehicleSpeed() {
        return 0.0F;
    }

    public jp.ngt.rtm.modelpack.state.ResourceState getResourceState() {
        return this.resourceState;
    }

    /**
     * ResourceState.getResourceName 用 (モデル名)。
     */
    protected String getResourceName() {
        return "";
    }

    //---- パックスクリプト互換 (1.7.10/1.12 SRG メソッド) ----

    /**
     * getBrightnessForRender (packed lightmap; 1.21 と同レイアウト sky<<20|block<<4)
     */
    public int func_70070_b() {
        BlockPos pos = BlockPos.containing(this.getX(), this.getY() + 0.5D, this.getZ());
        return net.minecraft.client.renderer.LevelRenderer.getLightColor(this.level(), pos);
    }

    public int func_70070_b(float partialTick) {
        return this.func_70070_b();
    }

    /**
     * getEntityId
     */
    public int func_145782_y() {
        return this.getId();
    }

    /**
     * isBeingRidden
     */
    public boolean func_184207_aI() {
        return !this.getPassengers().isEmpty();
    }

    /**
     * 本家 EntityVehicleBase.setPositionAndRotation2 相当 (エンティティトラッカーからの位置同期)。
     */
    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps) {
        this.vehiclePosRotationInc = steps;
        this.vehicleX = x;
        this.vehicleY = y;
        this.vehicleZ = z;
        this.vehicleYaw = yaw;
        this.vehiclePitch = pitch;
    }

    /**
     * 本家 updatePosAndRotationClient の忠実移植 (クライアント補間)。
     */
    protected void updatePosAndRotationClient() {
        if (this.vehiclePosRotationInc > 0) {
            float d0 = 1.0F / (float) this.vehiclePosRotationInc;
            double x = this.getX() + (this.vehicleX - this.getX()) * d0;
            double y = this.getY() + (this.vehicleY - this.getY()) * d0;
            double z = this.getZ() + (this.vehicleZ - this.getZ()) * d0;
            float yaw = this.getYRot() + Mth.wrapDegrees(this.vehicleYaw - this.getYRot()) * d0;
            float pitch = this.getXRot() + (this.vehiclePitch - this.getXRot()) * d0;
            this.rotationRoll = this.rotationRoll + (this.vehicleRoll - this.rotationRoll) * d0;
            --this.vehiclePosRotationInc;
            this.setPos(x, y, z);
            this.setRot(yaw, pitch);
        }
    }

    protected void onVehicleUpdate() {
    }

    protected void updateMovement() {
    }

    protected void applyPhysicalEffect() {
    }

    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch) {
        this.setPos(x, y, z);
        this.setRotationWrapped(yaw, pitch);
    }

    protected void setRotationWrapped(float yaw, float pitch) {
        this.setYRot(Mth.wrapDegrees(yaw));
        this.setXRot(Mth.wrapDegrees(pitch));
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }
}
