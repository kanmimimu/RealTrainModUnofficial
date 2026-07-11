package jp.ngt.rtm.entity.train;

import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.math.Vec3;
import jp.ngt.rtm.entity.train.util.BogieController;
import jp.ngt.rtm.entity.train.util.Formation;
import jp.ngt.rtm.entity.train.util.FormationEntry;
import jp.ngt.rtm.entity.train.util.FormationManager;
import jp.ngt.rtm.entity.train.util.TrainSpeedManager;
import jp.ngt.rtm.entity.train.util.TrainState;
import jp.ngt.rtm.entity.train.util.TrainState.TrainStateType;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import jp.ngt.rtm.modelpack.cfg.TrainConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Base64;

/**
 * 本家 jp.ngt.rtm.entity.train.EntityTrainBase (KaizPatchX) の忠実移植 (物理コア)。
 * updateSpeed / updateRoll / applyPhysicalEffect / TrainState 16byte 同期 / Formation 連携は本家のまま。
 * TODO(段階移植): ChunkLoader, ATS, 煙, パンタ/ドアのクライアントアニメ, PermissionManager, 実 ModelSet (Phase 4)。
 */
public abstract class EntityTrainBase extends EntityVehicleBase<TrainConfig> {
    private static final EntityDataAccessor<Integer> DATA_BOGIE0 =
            SynchedEntityData.defineId(EntityTrainBase.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BOGIE1 =
            SynchedEntityData.defineId(EntityTrainBase.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_BYTE_ARRAY =
            SynchedEntityData.defineId(EntityTrainBase.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> DATA_SPEED =
            SynchedEntityData.defineId(EntityTrainBase.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Byte> DATA_CAB_DIR =
            SynchedEntityData.defineId(EntityTrainBase.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<String> DATA_MODEL_NAME =
            SynchedEntityData.defineId(EntityTrainBase.class, EntityDataSerializers.STRING);

    public static final short MAX_AIR_COUNT = 2880;
    public static final short MIN_AIR_COUNT = 2480;
    public static final float TRAIN_WIDTH = 2.75F;
    public static final float TRAIN_HEIGHT = 1.25F - 0.0625F;//レールに合わせ高さ修正

    public BogieController bogieController = new BogieController();
    private Formation formation;

    private float trainSpeed;
    /**
     * notch x -18
     */
    public int brakeCount = 72;
    public int brakeAirCount = MAX_AIR_COUNT;
    public boolean complessorActive;

    private float wave;

    private TrainConfig configCache;

    public EntityTrainBase(EntityType<?> type, Level level) {
        super(type, level);
    }

    /**
     * TrainConfig の供給。暫定: VehicleDefinition アダプタ (Phase 4 で ModelSet 直結)。
     */
    @Override
    public TrainConfig getConfig() {
        if (this.configCache == null) {
            this.configCache = jp.ngt.rtm.modelpack.cfg.TrainConfigAdapter.get(this.getModelName());
        }
        return this.configCache;
    }

    public String getModelName() {
        return this.entityData.get(DATA_MODEL_NAME);
    }

    public void setModelName(String name) {
        this.entityData.set(DATA_MODEL_NAME, name == null ? "" : name);
        this.configCache = null;
    }

    public void spawnTrain(Level world) {
        //spawnの順番は「先に台車」
        this.bogieController.createBogie(world, this);
        this.bogieController.setupBogiePos(this);
        this.bogieController.spawnBogies(world, this);
        world.addFreshEntity(this);
        this.formation = FormationManager.getInstance(world.isClientSide).createNewFormation(this);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_BOGIE0, 0);
        builder.define(DATA_BOGIE1, 0);
        builder.define(DATA_BYTE_ARRAY, Base64.getEncoder().encodeToString(new byte[16]));
        builder.define(DATA_SPEED, 0.0F);
        builder.define(DATA_CAB_DIR, (byte) 0);
        builder.define(DATA_MODEL_NAME, "");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        CompoundTag entryData = new CompoundTag();
        this.writeFormationData(entryData);
        nbt.put("FormationEntry", entryData);

        nbt.putInt("trainDir", this.getTrainDirection());
        nbt.putString("byteArray", this.entityData.get(DATA_BYTE_ARRAY));
        nbt.putByte("cabDir", this.entityData.get(DATA_CAB_DIR));
        nbt.putString("modelName", this.getModelName());
    }

    private void writeFormationData(CompoundTag nbt) {
        if (this.formation == null) {
            return;
        }
        FormationEntry entry = this.formation.getEntry(this);
        if (entry != null) {
            nbt.putLong("FormationId", this.formation.id);
            nbt.putByte("EntryPos", entry.entryId);
            nbt.putByte("EntryDir", entry.dir);
            nbt.putInt("FormationSize", this.formation.size());
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.readFormationData(nbt.getCompound("FormationEntry"));
        this.setTrainDirection(nbt.getInt("trainDir"));
        this.entityData.set(DATA_BYTE_ARRAY, nbt.getString("byteArray"));
        this.entityData.set(DATA_CAB_DIR, nbt.getByte("cabDir"));
        this.setModelName(nbt.getString("modelName"));
    }

    private void readFormationData(CompoundTag nbt) {
        long id = nbt.getLong("FormationId");
        byte pos = nbt.getByte("EntryPos");
        byte dir = nbt.getByte("EntryDir");
        int size = nbt.getInt("FormationSize");
        FormationManager fm = FormationManager.getInstance(this.level().isClientSide);
        Formation f0 = fm.getFormation(id);
        if (f0 == null) {
            if (nbt.contains("FormationId")) {
                this.formation = fm.createNewFormation(this, id, pos, dir, size);
            } else {
                this.formation = fm.createNewFormation(this);
            }
        } else {
            this.formation = f0;
            f0.setTrain(this, pos, dir);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (!this.level().isClientSide && reason.shouldDestroy()) {
            this.bogieController.setDead();
            if (this.formation != null) {
                try {
                    this.formation.onRemovedTrain(this);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onVehicleUpdate() {
        this.updateSpeed();

        if (this.existBogies()) {
            this.bogieController.updateBogies();
        }

        //ブレーキ空気圧 (本家はクライアント側アニメ)
        if (this.level().isClientSide) {
            if (this.complessorActive) {
                ++this.brakeAirCount;
                if (this.brakeAirCount >= MAX_AIR_COUNT) {
                    this.complessorActive = false;
                }
            } else if (this.brakeAirCount < MIN_AIR_COUNT) {
                this.complessorActive = true;
            }
        }
    }

    @Override
    protected void updateMovement() {
        if (this.formation != null && this.formation.isFrontCar(this)) {
            this.formation.updateTrainMovement();
        }
    }

    @Override
    protected void applyPhysicalEffect() {
        this.setDeltaMovement(this.getDeltaMovement().scale(0.99D));

        float f0 = 0.125F;
        float pitch = this.getXRot();
        if (pitch > 0.0F) {
            pitch -= f0;
        } else if (pitch < 0.0F) {
            pitch += f0;
        }
        if (Math.abs(pitch) < f0) {
            pitch = 0.0F;
        }
        this.setXRot(pitch);

        if (this.existBogies()) {
            this.getBogie(0).setXRot(pitch);
            this.getBogie(1).setXRot(pitch * -1.0F);
        }
    }

    //BCから呼び出し
    public void updateRoll(float par1) {
        TrainConfig cfg = this.getConfig();
        float f0 = -cfg.rolling;
        float pendulum = Mth.wrapDegrees((this.getYRot() - this.prevRotationYawVehicle) * f0);
        if (this.getTrainDirection() == 1) {
            pendulum *= -1.0F;
        }
        float roll = par1 + pendulum;
        this.wave = (float) ((this.wave + this.trainSpeed * cfg.rollSpeedCoefficient) % (2.0D * Math.PI));
        float sw = (NGTMath.getSin(this.wave) + NGTMath.getSin(this.wave * cfg.rollVariationCoefficient)) * 0.5F;
        this.rotationRoll = roll + sw * cfg.rollWidthCoefficient;
    }

    protected void updateSpeed() {
        int notch = this.getNotch();

        boolean isBrakeDisabled = true;
        float speed = this.trainSpeed;

        //ブレーキ処理, 全ての車両で
        if (notch < 0) {
            int max = notch * -18;
            if (this.brakeCount < max) {
                ++this.brakeCount;
                if (this.level().isClientSide) {
                    --this.brakeAirCount;
                }
            } else if (this.brakeCount > max) {
                this.brakeCount -= (this.brakeCount - max > 1) ? 2 : 1;
            }
        } else {
            if (this.brakeCount > 0) {
                if (speed <= 0.0F) {
                    isBrakeDisabled = false;
                }
                this.brakeCount -= 2;
            } else if (this.brakeCount < 0) {
                this.brakeCount = 0;
            }
        }

        //速度処理, 先頭車のみ
        if (this.isControlCar()) {
            if (isBrakeDisabled && !this.level().isClientSide) {
                TrainConfig cfg = this.getConfig();
                float acceleration = TrainSpeedManager.getAcceleration(this, notch, Math.abs(speed), cfg);
                TrainState dir = this.getTrainState(10);
                if ((dir == TrainState.Direction_Back && speed > 0) || (dir == TrainState.Direction_Front && speed < 0)) {
                    acceleration = Math.abs(acceleration);
                }
                if (dir == TrainState.Direction_Back) {
                    acceleration *= -1;
                }

                speed += acceleration;

                if (notch >= 0)//ブレーキ解
                {
                    float deceleration;
                    if (this.getXRot() == 0.0F) {
                        float f1 = -cfg.deccelerations[0];
                        deceleration = speed > 0.0F ? f1 : (speed < 0.0F ? -f1 : 0.0F);
                    } else//坂
                    {
                        float dec = 0.0125F;
                        float f2 = (this.getTrainDirection() == 0) ? dec : -dec;
                        deceleration = NGTMath.sin(this.getXRot()) * f2;
                    }
                    speed -= deceleration;
                }

                this.setSpeed(speed);
            }
        }
    }

    /**
     * 連結時の車両同士の距離
     */
    public double getDefaultDistanceToConnectedTrain(EntityTrainBase par1) {
        double d0 = this.getConfig().trainDistance;
        double d1 = par1.getConfig().trainDistance;
        return d0 + d1;
    }

    /**
     * 車両同士が連結可能な距離内にあるか
     */
    public boolean inConnectableRange(EntityTrainBase par1) {
        double d0 = this.getDefaultDistanceToConnectedTrain(par1);
        return this.distanceToSqr(par1) <= d0 * d0;
    }

    @Override
    protected void positionRider(Entity rider, Entity.MoveFunction move) {
        if (this.hasPassenger(rider)) {
            float[][] pos = this.getConfig().getPlayerPos();
            int dir = this.entityData.get(DATA_CAB_DIR);
            Vec3 v31 = new Vec3(pos[dir][0], pos[dir][1], pos[dir][2]);
            v31 = v31.rotateAroundZ(-this.rotationRoll);
            v31 = v31.rotateAroundX(this.getXRot());
            v31 = v31.rotateAroundY(this.getYRot());
            move.accept(rider,
                    this.getX() + v31.getX(),
                    this.getY() + v31.getY() + (this.getBbHeight() - 0.93F),
                    this.getZ() + v31.getZ());
        }
    }

    /**
     * 本家 attackEntityFrom 相当: バール/クリエイティブで撤去
     */
    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (this.level().isClientSide || this.isRemoved()) {
            return false;
        }
        if (source.getEntity() instanceof Player player) {
            if (player.getAbilities().instabuild
                    || player.getMainHandItem().getItem() instanceof com.portofino.realtrainmodunofficial.item.CrowbarItem) {
                this.kill();
                return true;
            }
        }
        return false;
    }

    /**
     * EntityBogieで呼ばれる (乗車・連結棒)
     */
    protected InteractionResult interactTrain(EntityBogie bogie, Player player) {
        if (!this.level().isClientSide) {
            int id1 = bogie.getBogieId();
            net.minecraft.world.item.ItemStack itemstack = player.getMainHandItem();
            if (!itemstack.isEmpty() && itemstack.getItem() instanceof com.portofino.realtrainmodunofficial.item.CrowbarItem) {
                if (id1 >= 0) {
                    if (this.getConnectedTrain(id1) == null) {
                        if (bogie.isActivated) {
                            if (this.getNotch() == 0) {
                                this.setTrainDirection(id1);
                                this.setNotch(1);
                                jp.ngt.ngtlib.io.NGTLog.sendChatMessage(player, "message.train.start_auto_concatenation");
                            } else {
                                jp.ngt.ngtlib.io.NGTLog.sendChatMessage(player, "message.train.already_concatenation_mode");
                            }
                        } else {
                            bogie.isActivated = true;
                            jp.ngt.ngtlib.io.NGTLog.sendChatMessage(player, "message.train.concatenation_mode");
                        }
                    } else {
                        this.formation.onDisconnectedTrain(this, id1);
                        jp.ngt.ngtlib.io.NGTLog.sendChatMessage(player, "message.train.deconcatenation");
                    }
                }
            } else if (id1 >= 0) {
                this.mountEntityToTrain(player, id1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void mountEntityToTrain(Entity entity, int direction) {
        if (this.isControlCar()) {
            this.setTrainDirection(direction);
            if (this.formation != null && this.formation.size() > 1) {
                byte data = this.getTrainStateData(TrainStateType.State_Direction.id);
                byte newData = this.getCabDirection() == this.getTrainDirection() ? data : (byte) (data ^ 2);
                this.setTrainStateData_NoSync(TrainStateType.State_Direction.id, newData);
            }
        }
        this.entityData.set(DATA_CAB_DIR, (byte) direction);
        entity.startRiding(this);
    }

    /**
     * @param par1 連結される台車
     * @param par2 連結対象の台車
     */
    public void connectTrain(EntityBogie par1, EntityBogie par2) {
        if (!this.level().isClientSide && par2.getTrain() != null) {
            int id1 = par1.getBogieId();
            int id2 = par2.getBogieId();
            if (id1 >= 0 && id2 >= 0 && this.getConnectedTrain(id1) == null && par2.getTrain().getFormation() != null) {
                this.formation.connectTrain(this, par2.getTrain(), id1, id2, par2.getTrain().getFormation());
                this.level().levelEvent(1022, this.blockPosition(), 0);
            }
        }
    }

    public float getSpeed() {
        return this.entityData.get(DATA_SPEED);
    }

    public void setSpeed(float par1) {
        if (this.level().isClientSide) {
            this.trainSpeed = par1;
        } else if (this.isControlCar() && this.formation != null) {
            this.formation.setSpeed(par1);
        }
    }

    public void setSpeed_NoSync(float par1) {
        if (this.trainSpeed != par1) {
            this.trainSpeed = par1;
            this.entityData.set(DATA_SPEED, par1);
        }
    }

    public void stopTrain(boolean changeSpeed) {
        if (this.formation != null) {
            this.setEBNotch();
            if (changeSpeed) {
                this.setSpeed(0.0F);
            }
        }
    }

    public boolean isControlCar() {
        int data = this.getTrainStateData(TrainStateType.State_Direction.id);
        return data == TrainState.Direction_Front.data || data == TrainState.Direction_Back.data;
    }

    public boolean existBogies() {
        return this.getBogie(0) != null && this.getBogie(1) != null;
    }

    public EntityBogie getBogie(int bogieId) {
        if (this.bogieController.getBogie(bogieId) == null) {
            EntityDataAccessor<Integer> id = bogieId == 0 ? DATA_BOGIE0 : DATA_BOGIE1;
            Entity entity = this.level().getEntity(this.entityData.get(id));
            if (entity instanceof EntityBogie bogie) {
                this.bogieController.setBogie(bogieId, bogie);
            }
        }
        return this.bogieController.getBogie(bogieId);
    }

    /**
     * EntityBogieから呼び出し
     */
    public void setBogie(int id, EntityBogie bogie) {
        this.bogieController.setBogie(id, bogie);
        this.entityData.set(id == 0 ? DATA_BOGIE0 : DATA_BOGIE1, bogie.getId());
    }

    /**
     * @param par1 0 or 1
     */
    public EntityTrainBase getConnectedTrain(int par1) {
        if (this.formation != null) {
            FormationEntry entry = this.formation.getEntry(this);
            if (entry == null) {
                return null;
            }
            int pos = entry.entryId;
            int dif = (par1 == 0) ? -1 : 1;
            if (entry.dir == 1) {
                dif *= -1;
            }
            pos += dif;
            if (pos < 0 || pos >= this.formation.size()) {
                return null;
            }
            FormationEntry connected = this.formation.get(pos);
            if (connected != null) {
                return connected.train;
            }
        }
        return null;
    }

    public Formation getFormation() {
        return this.formation;
    }

    public void setFormation(Formation par1) {
        this.formation = par1;
    }

    private byte[] getByteArray() {
        byte[] ba = Base64.getDecoder().decode(this.entityData.get(DATA_BYTE_ARRAY));
        return ba.length < 16 ? new byte[16] : ba;
    }

    private byte clampTrainStateData(int id, byte data) {
        TrainStateType trainStateType = TrainState.getStateType(id);
        if (trainStateType == TrainStateType.State_Notch) {
            TrainConfig cfg = this.getConfig();
            return NGTMath.clamp(data, (byte) -(cfg.deccelerations.length - 1), (byte) cfg.maxSpeed.length);
        } else {
            return NGTMath.clamp(data, trainStateType.min, trainStateType.max);
        }
    }

    private byte getByteFromDataWatcher(int par1) {
        byte[] ba = this.getByteArray();
        return this.clampTrainStateData(par1, ba[par1]);
    }

    private byte setByteToDataWatcher(int par1, byte par2) {
        byte data = this.clampTrainStateData(par1, par2);
        byte[] ba = this.getByteArray();
        ba[par1] = data;
        this.entityData.set(DATA_BYTE_ARRAY, Base64.getEncoder().encodeToString(ba));
        return data;
    }

    public byte getCabDirection() {
        return this.entityData.get(DATA_CAB_DIR);
    }

    /**
     * 0:前, 1:後
     */
    public int getTrainDirection() {
        return this.getByteFromDataWatcher(TrainStateType.State_TrainDir.id);
    }

    public void setTrainDirection(int par1) {
        if (this.formation == null) {
            this.setTrainDirection_NoSync((byte) par1);
        } else {
            this.formation.setTrainDirection((byte) par1, this);
        }
    }

    public void setTrainDirection_NoSync(byte par1) {
        byte data = this.setByteToDataWatcher(TrainStateType.State_TrainDir.id, par1);
        int id2 = 1 - data;
        if (id2 < 2 && this.existBogies()) {
            this.getBogie(data).setFront(true);
            this.getBogie(id2).setFront(false);
        }
    }

    public int getNotch() {
        return this.getByteFromDataWatcher(TrainStateType.State_Notch.id);
    }

    public boolean addNotch(Entity driver, int par2) {
        if (par2 != 0) {
            int i = this.getNotch();
            return this.setNotch(i + par2);
        }
        return false;
    }

    public boolean setNotch(int par1) {
        if (this.isControlCar()) {
            byte notch = this.clampTrainStateData(TrainStateType.State_Notch.id, (byte) par1);
            int prevNotch = this.getNotch();
            if (prevNotch != notch) {
                this.setByteToDataWatcher(TrainStateType.State_Notch.id, notch);
                return true;
            }
        }
        return false;
    }

    public void setEBNotch() {
        int prevNotch = this.getNotch();
        int ebNotch = -(this.getConfig().deccelerations.length - 1);
        if (prevNotch != ebNotch) {
            this.setByteToDataWatcher(TrainStateType.State_Notch.id, (byte) ebNotch);
        }
    }

    public int getSignal() {
        return this.getByteFromDataWatcher(TrainStateType.State_Signal.id);
    }

    public void setSignal(int par1) {
        int signal = this.getSignal();
        if (par1 > 0 && signal != -1) {
            this.setByteToDataWatcher(TrainStateType.State_Signal.id, (byte) par1);
        }
    }

    /**
     * 0:direction 1:notch 2:signal 4:door 5:light 6:pantograph 7:chunk_loader
     * 8:destination 9:announcement 10:direction 11:interior_light
     */
    public byte getTrainStateData(int id) {
        return this.getByteFromDataWatcher(id);
    }

    public TrainState getTrainState(int id) {
        return TrainState.getState(id, this.getTrainStateData(id));
    }

    public void setTrainStateData(int id, byte data) {
        if (this.formation != null) {
            this.formation.setTrainStateData(id, data, this);
        }
    }

    public void setTrainStateData_NoSync(int id, byte data) {
        this.setByteToDataWatcher(id, data);
    }

    /**
     * サーバー同期後にクライアントで speed を反映
     */
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_SPEED.equals(key) && this.level().isClientSide) {
            this.trainSpeed = this.entityData.get(DATA_SPEED);
        }
        if (DATA_MODEL_NAME.equals(key)) {
            this.configCache = null;
        }
    }
}
