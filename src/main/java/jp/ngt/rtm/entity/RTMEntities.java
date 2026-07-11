package jp.ngt.rtm.entity;

import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import jp.ngt.rtm.entity.train.EntityBogie;
import jp.ngt.rtm.entity.train.EntityTrain;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * jp.ngt.rtm エンティティの登録 (Phase 2)。
 * メインクラスから REGISTER.register(modBus) される。
 */
public final class RTMEntities {
    public static final DeferredRegister<EntityType<?>> REGISTER =
            DeferredRegister.create(Registries.ENTITY_TYPE, RealTrainModUnofficial.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTrain>> TRAIN =
            REGISTER.register("rtm_train", () -> EntityType.Builder.<EntityTrain>of(EntityTrain::new, MobCategory.MISC)
                    .sized(EntityTrainBase.TRAIN_WIDTH, EntityTrainBase.TRAIN_HEIGHT)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .build("rtm_train"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityBogie>> BOGIE =
            REGISTER.register("rtm_bogie", () -> EntityType.Builder.<EntityBogie>of(EntityBogie::new, MobCategory.MISC)
                    .sized(EntityTrainBase.TRAIN_WIDTH, EntityTrainBase.TRAIN_HEIGHT)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .build("rtm_bogie"));

    private RTMEntities() {
    }
}
