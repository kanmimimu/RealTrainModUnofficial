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

    /**
     * 転換クロスシートの回転量 (-{@link #MAX_SEAT_ROTATION} 〜 {@link #MAX_SEAT_ROTATION})。
     * {@code updateAnimation} が進行方向へ毎 tick 1 ずつ動かすので、転換は滑らかに進む。
     * <p>
     * ★ このフィールドと同名の getter ({@code getSeatRotation()}) を<b>絶対に足さないこと</b>。
     * <p>
     * パックの描画スクリプトは {@code entity.seatRotation / 45} でこの値を読む
     * ({@code RTMCore.VERSION} が "1.7.10" を含むため、どのパックも legacy 経路に入る)。
     * ところが Nashorn (Dynalink の BeansLinker) はプロパティ解決で<b>フィールドより getter を
     * 優先する</b>ため、{@code getSeatRotation()} を定義すると {@code entity.seatRotation} が
     * フィールドではなく getter を返すようになる。戻り値の尺度が変われば、スクリプト側の
     * {@code / 45} が二重に効いて座席が動かなくなる (実際に小田急 30000 形で発生した)。
     */
    public int seatRotation;
    public int doorMoveL;
    public int doorMoveR;
    public int pantograph_F;
    public int pantograph_B;
    public float wheelRotationR;
    public float wheelRotationL;

    //パックスクリプト互換 (1.7.10 SRG 名を直接参照するスクリプトのため tick 毎に更新)
    //field_70170_p は World の SRG メソッド (func_72929_e 等) を委譲する WorldCompat
    public jp.ngt.mccompat.WorldCompat field_70170_p;
    public int field_70173_aa;
    public float field_70177_z;
    public float field_70125_A;
    public Entity field_70153_n;

    private final jp.ngt.rtm.modelpack.state.ResourceState resourceState =
            new jp.ngt.rtm.modelpack.state.ResourceState(this::getResourceName);

    //本家 vehicleFloors (slotPos 座席)
    protected final java.util.List<jp.ngt.rtm.entity.train.parts.EntityFloor> vehicleFloors = new java.util.ArrayList<>();
    protected boolean floorLoaded;

    public float prevRotationYawVehicle;
    public float prevRotationPitchVehicle;

    //本家 setPositionAndRotation2/updatePosAndRotationClient のクライアント補間
    protected int vehiclePosRotationInc;
    protected double vehicleX, vehicleY, vehicleZ;
    protected float vehicleYaw, vehiclePitch, vehicleRoll;
    private boolean clientRotInit;

    public EntityVehicleBase(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        //スクリプトは初回 tick 前 (スポーン直後の描画) にも参照する
        this.field_70170_p = new jp.ngt.mccompat.WorldCompat(level);
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
            if (!this.floorLoaded) {
                this.setupFloors();
            }
            this.updateMovement();
        }

        this.applyPhysicalEffect();

        //視点追従 (本家 KaizPatchX EntityTrainBase.updateRiderPosition)
        this.rotateRiders();

        //パックスクリプト互換 SRG フィールドの更新
        if (this.field_70170_p == null || this.field_70170_p.level != this.level()) {
            this.field_70170_p = new jp.ngt.mccompat.WorldCompat(this.level());
        }
        this.field_70173_aa = this.tickCount;
        this.field_70177_z = this.getYRot();
        this.field_70125_A = this.getXRot();
        this.field_70153_n = this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
    }

    /**
     * 車体が向きを変えたぶんだけ、乗客の視点も一緒に回す (カーブでの視点追従)。
     * <p>
     * 本家 KaizPatchX {@code EntityTrainBase.updateRiderPosition}:
     * <pre>
     *   //運転手のYaw調整, PlayerのYawは他のEntityとは逆向き
     *   riddenByEntity.rotationYaw   -= wrapAngleTo180(rotationYaw   - prevRotationYaw);
     *   riddenByEntity.rotationPitch -= wrapAngleTo180(rotationPitch - prevRotationPitch);
     * </pre>
     * <p>
     * 符号が引き算なのは、<b>車体の yaw (RTM 系: 90° = +X) と Minecraft のプレイヤー yaw
     * (90° = −X) で X の符号が逆</b>だから。車体が RTM 系で +θ 回れば、同じ向きを向くために
     * プレイヤーの yaw は −θ 動かす必要がある。
     * <p>
     * クライアントだけで行う。プレイヤーの視点はクライアントが持ち主で、毎tickサーバーへ
     * 送られるため、サーバー側で回しても上書きされて意味がない (他プレイヤーの視点は
     * そのプレイヤー自身のクライアントが回す)。
     */
    private void rotateRiders() {
        if (!this.level().isClientSide || this.getPassengers().isEmpty()) {
            return;
        }
        float dYaw = net.minecraft.util.Mth.wrapDegrees(this.getYRot() - this.prevRotationYawVehicle);
        float dPitch = net.minecraft.util.Mth.wrapDegrees(this.getXRot() - this.prevRotationPitchVehicle);
        if (dYaw == 0.0F && dPitch == 0.0F) {
            return;
        }
        for (Entity rider : this.getPassengers()) {
            //yRotO / xRotO も一緒に動かす。動かさないと補間が 1 tick ぶん引っ張られて
            //カーブのたびに視点がガクつく。
            rider.setYRot(rider.getYRot() - dYaw);
            rider.yRotO -= dYaw;
            rider.setYHeadRot(rider.getYHeadRot() - dYaw);
            rider.setXRot(rider.getXRot() - dPitch);
            rider.xRotO -= dPitch;
            if (rider instanceof net.minecraft.world.entity.LivingEntity living) {
                living.yBodyRot -= dYaw;
                living.yBodyRotO -= dYaw;
            }
        }
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
     * 本家 setupFloors (Server Only): config の slotPos ごとに EntityFloor をスポーン。
     */
    protected void setupFloors() {
        this.vehicleFloors.stream().filter(java.util.Objects::nonNull).forEach(Entity::discard);
        this.vehicleFloors.clear();

        this.floorLoaded = true;
        float[][] slots = this.getConfig().getSlotPos();
        if (slots == null) {
            return;
        }
        for (float[] fa : slots) {
            if (fa == null || fa.length < 3) {
                continue;
            }
            byte type = fa.length >= 4 ? (byte) fa[3] : (byte) 2;
            jp.ngt.rtm.entity.train.parts.EntityFloor floor = new jp.ngt.rtm.entity.train.parts.EntityFloor(
                    jp.ngt.rtm.entity.RTMEntities.FLOOR.get(), this.level(), this,
                    new float[]{fa[0], fa[1], fa[2]}, type);
            if (this.level().addFreshEntity(floor)) {
                this.vehicleFloors.add(floor);
            } else {
                this.floorLoaded = false;//1つでもスポーン失敗したら、やり直し
                break;
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (!this.level().isClientSide && reason.shouldDestroy()) {
            this.vehicleFloors.stream().filter(java.util.Objects::nonNull).forEach(Entity::discard);
            this.vehicleFloors.clear();
        }
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
     * 回転はバニラパケットだとバイト量子化 (約1.4°刻み) で段階的になるため受け取らず、
     * float 同期 (EntityTrainBase の DATA_YAW/DATA_PITCH → vehicleYaw/vehiclePitch) を使う。
     */
    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps) {
        this.vehiclePosRotationInc = steps;
        this.vehicleX = x;
        this.vehicleY = y;
        this.vehicleZ = z;
        if (!this.clientRotInit) {
            //スポーン直後の初期姿勢のみパケット値を採用 (以降は float 同期)
            this.vehicleYaw = yaw;
            this.vehiclePitch = pitch;
        }
    }

    /**
     * 本家 updatePosAndRotationClient の忠実移植 (クライアント補間)。
     * 回転: float 同期値 (DATA_YAW/PITCH/ROLL) を毎 tick そのまま採用し、
     * フレーム間は yRotO との描画補間で滑らかにする (バニラと同じ方式)。
     * 漸近補間 (t=1/3) は回転がワンテンポ遅れて見えるため廃止。
     */
    protected void updatePosAndRotationClient() {
        if (!this.clientRotInit) {
            this.clientRotInit = true;
            this.setRot(this.vehicleYaw, this.vehiclePitch);
            this.yRotO = this.vehicleYaw;
            this.xRotO = this.vehiclePitch;
            this.rotationRoll = this.vehicleRoll;
            this.prevRotationRoll = this.vehicleRoll;
        }
        if (this.vehiclePosRotationInc > 0) {
            float d0 = 1.0F / (float) this.vehiclePosRotationInc;
            double x = this.getX() + (this.vehicleX - this.getX()) * d0;
            double y = this.getY() + (this.vehicleY - this.getY()) * d0;
            double z = this.getZ() + (this.vehicleZ - this.getZ()) * d0;
            --this.vehiclePosRotationInc;
            this.setPos(x, y, z);
        }
        //ラップ跨ぎ (179→-179 等) でフレーム補間が逆回りしないよう、現在値の近傍へ展開
        float yaw = this.getYRot() + Mth.wrapDegrees(this.vehicleYaw - this.getYRot());
        this.setRot(yaw, this.vehiclePitch);
        this.rotationRoll = this.vehicleRoll;
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
