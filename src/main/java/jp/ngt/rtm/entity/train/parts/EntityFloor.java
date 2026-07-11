package jp.ngt.rtm.entity.train.parts;

import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 本家 jp.ngt.rtm.entity.train.parts.EntityFloor (KaizPatchX) の忠実移植。
 * config の slotPos ごとにスポーンし、右クリックで着席する。
 */
public class EntityFloor extends EntityVehiclePart {
    private static final EntityDataAccessor<Byte> DATA_SEAT_TYPE =
            SynchedEntityData.defineId(EntityFloor.class, EntityDataSerializers.BYTE);

    public EntityFloor(EntityType<?> type, Level level) {
        super(type, level);
    }

    public EntityFloor(EntityType<?> type, Level level, EntityVehicleBase<?> vehicle, float[] pos, byte seatType) {
        this(type, level);
        this.setVehicle(vehicle);
        this.setPartPos(pos[0], pos[1], pos[2]);
        this.updatePartPos(vehicle);
        this.setSeatType(seatType);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SEAT_TYPE, (byte) 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.setSeatType(nbt.getByte("seatType"));
        super.readAdditionalSaveData(nbt);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putByte("seatType", this.getSeatType());
        super.addAdditionalSaveData(nbt);
    }

    @Override
    public void onLoadVehicle() {
        //setupFloors が新しいフロアを常にスポーンし直すため、旧セーブのフロアは破棄する
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.getVehicle() == null || this.getVehicle().isRemoved()) {
            if (!this.level().isClientSide) {
                this.discard();
            }
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            //親車両が消えたら追従して消える
            if (this.tickCount > 100 && this.getVehicle() == null) {
                this.discard();
            }
            //スニークで降車 (バニラ経路が効かない場合の保険)
            for (Entity rider : new java.util.ArrayList<>(this.getPassengers())) {
                if (rider instanceof Player player && player.isShiftKeyDown()) {
                    player.stopRiding();
                }
            }
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!this.getPassengers().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (this.getSeatType() != 0 && !player.isShiftKeyDown()) {
            player.startRiding(this);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void positionRider(Entity rider, Entity.MoveFunction move) {
        if (this.hasPassenger(rider)) {
            //本家: posY + height + 0.25 (1.21 は着座姿勢分を引く)
            move.accept(rider, this.getX(), this.getY() + this.getBbHeight() + 0.25D - 0.45D, this.getZ());
        }
    }

    @Override
    public net.minecraft.world.phys.Vec3 getDismountLocationForPassenger(net.minecraft.world.entity.LivingEntity rider) {
        //車体の外 (左右) に降ろす
        EntityVehicleBase<?> vehicle = this.getVehicle();
        float yaw = vehicle != null ? vehicle.getYRot() : this.getYRot();
        double side = 2.0D;
        double rad = Math.toRadians(yaw);
        double ox = Math.cos(rad) * side;
        double oz = -Math.sin(rad) * side;
        return new net.minecraft.world.phys.Vec3(this.getX() + ox, this.getY() + 1.0D, this.getZ() + oz);
    }

    public void setSeatType(byte par1) {
        this.entityData.set(DATA_SEAT_TYPE, par1);
    }

    public byte getSeatType() {
        return this.entityData.get(DATA_SEAT_TYPE);
    }
}
