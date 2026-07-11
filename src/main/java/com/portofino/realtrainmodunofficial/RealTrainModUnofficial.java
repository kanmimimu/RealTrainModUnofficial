package com.portofino.realtrainmodunofficial;

import net.neoforged.api.distmarker.Dist;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(RealTrainModUnofficial.MODID)
public class RealTrainModUnofficial {
    public static final String MODID = "realtrainmodunofficial";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB =
        CREATIVE_MODE_TABS.register("main_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.realtrainmodunofficial"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> RealTrainModUnofficialItems.RAIL_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(RealTrainModUnofficialItems.TRAIN_ITEM.get());
                output.accept(RealTrainModUnofficialItems.CAR_ITEM.get());
                output.accept(RealTrainModUnofficialItems.IC_CARD_ITEM.get());
                output.accept(RealTrainModUnofficialItems.RAIL_ITEM.get());
                output.accept(RealTrainModUnofficialItems.WIRE_ITEM.get());
                output.accept(RealTrainModUnofficialItems.CROWBAR_ITEM.get());
                output.accept(RealTrainModUnofficialItems.WRENCH_ITEM.get());
                output.accept(RealTrainModUnofficialItems.CROSSING_GATE_ITEM.get());
                output.accept(RealTrainModUnofficialItems.MARKER_ITEM.get());
                //マーカー(斜め)/分岐マーカー(斜め)/スピーカー はユーザー要望で削除
                //(本家は通常マーカーが8方位対応のため斜めバリアント不要)
                output.accept(RealTrainModUnofficialItems.MARKER_SWITCH_ITEM.get());
                output.accept(RealTrainModUnofficialItems.LIGHT_ITEM.get());
                output.accept(RealTrainModUnofficialItems.INSULATOR_ITEM.get());
                output.accept(RealTrainModUnofficialItems.SIGNAL_ITEM.get());
                //架線柱 はユーザー要望で削除 (NGTO Builder 等で代替。登録は既存ワールド互換のため残置)
                output.accept(RealTrainModUnofficialItems.TICKET_GATE_ITEM.get());
                //入力コネクタ/出力コネクタ/信号変換器 はユーザー要望で削除
                //(信号機とワイヤーのみ残す。登録は既存ワールド互換のため残置)
                //MCTE 互換ミニチュア (NGTO Builder 用)
                output.accept(RealTrainModUnofficialItems.MINIATURE_ITEM.get());
            }).build());

    public RealTrainModUnofficial(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        // 軽量化: 既定のログレベルは INFO に固定する(描画には無関係)。
        // 以前はバグ追跡のため DEBUG を強制していたが、毎tick/毎フレームの DEBUG ログが
        // 文字列整形・I/O コストになり負荷源になるため INFO に下げる(調査時は手動で DEBUG に上げる)。
        try {
            org.apache.logging.log4j.core.config.Configurator.setLevel(
                "com.portofino.realtrainmodunofficial",
                org.apache.logging.log4j.Level.INFO
            );
        } catch (Throwable t) {
            LOGGER.warn("Failed to set log level for rtmu: {}", t.toString());
        }

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerNetwork);
        modEventBus.addListener(this::buildCreativeTabContents);

        RealTrainModUnofficialBlocks.BLOCKS.register(modEventBus);
        // jp.ngt.rtm.rail: 本家忠実移植のレール/マーカー (Phase 1)
        jp.ngt.rtm.rail.RTMRailBlocks.REGISTER.register(modEventBus);
        jp.ngt.rtm.rail.RTMRailBlockEntities.REGISTER.register(modEventBus);
        jp.ngt.rtm.item.RTMItems.REGISTER.register(modEventBus);
        // jp.ngt.rtm.entity: 本家忠実移植の列車/台車 (Phase 2)
        jp.ngt.rtm.entity.RTMEntities.REGISTER.register(modEventBus);
        RealTrainModUnofficialItems.ITEMS.register(modEventBus);
        RealTrainModUnofficialEntities.ENTITIES.register(modEventBus);
        com.portofino.realtrainmodunofficial.registry.RealTrainModUnofficialEntities.ENTITY_TYPES.register(modEventBus);
        RealTrainModUnofficialBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        RealTrainModUnofficialComponents.REGISTRAR.register(modEventBus);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            com.portofino.realtrainmodunofficial.compat.webctc.WebCtcCompat::onServerStarted);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            com.portofino.realtrainmodunofficial.compat.webctc.WebCtcCompat::onServerStopping);
        // スピーカー音源マッピングをサーバー起動時にロードし、プレイヤー接続時に同期する。
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.event.server.ServerStartingEvent e) ->
                com.portofino.realtrainmodunofficial.installedobject.SpeakerSoundConfig.load());
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e) -> {
                if (e.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp,
                        new com.portofino.realtrainmodunofficial.network.SyncSpeakerSoundsPayload(
                            java.util.Arrays.asList(
                                com.portofino.realtrainmodunofficial.installedobject.SpeakerSoundConfig.snapshot())));
                }
            });

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BundledPackInstaller.installDefaultPacks();
            com.portofino.realtrainmodunofficial.rail.RailPackLoader.load();
            com.portofino.realtrainmodunofficial.vehicle.VehiclePackLoader.load();
            com.portofino.realtrainmodunofficial.installedobject.InstalledObjectPackLoader.load();
            com.portofino.realtrainmodunofficial.script.TrainScriptSystem.getInstance().initialize();
        });
    }

    private void registerNetwork(RegisterPayloadHandlersEvent event) {
        com.portofino.realtrainmodunofficial.network.RealTrainModUnofficialNetwork.registerPayloadHandlers(event);
    }

    private void buildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (CreativeModeTabs.REDSTONE_BLOCKS.equals(event.getTabKey())) {
            event.accept(RealTrainModUnofficialItems.CROSSING_GATE_ITEM.get());
            event.accept(RealTrainModUnofficialItems.SIGNAL_ITEM.get());
            event.accept(RealTrainModUnofficialItems.SPEAKER_ITEM.get());
        }
    }
}
