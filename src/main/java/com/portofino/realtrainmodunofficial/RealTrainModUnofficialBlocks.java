package com.portofino.realtrainmodunofficial;

import com.portofino.realtrainmodunofficial.block.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class RealTrainModUnofficialBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, RealTrainModUnofficial.MODID);

    public static final RegistryObject<CrossingGateBlock> CROSSING_GATE
        = BLOCKS.register("crossing_gate", () -> new CrossingGateBlock());
    public static final RegistryObject<MarkerBlock> MARKER
        = BLOCKS.register("legacy_marker", () -> new MarkerBlock(false));
    public static final RegistryObject<MarkerBlock> MARKER_SWITCH
        = BLOCKS.register("legacy_marker_switch", () -> new MarkerBlock(true));

    /** 道床ブロック（レールと独立した物理ブロック） */
    public static final RegistryObject<BallastBlock> BALLAST
        = BLOCKS.register("legacy_ballast", BallastBlock::new);

    /** レールコアブロック（起点1個のみ、MQOモデル描画を担当） */
    public static final RegistryObject<LargeRailCoreBlock> LARGE_RAIL_CORE
        = BLOCKS.register("legacy_large_rail_core", () -> new LargeRailCoreBlock());

    /** レール当たり判定ブロック（非表示・薄い） */
    public static final RegistryObject<RailCollisionBlock> RAIL_COLLISION
        = BLOCKS.register("legacy_rail_collision", () -> new RailCollisionBlock());

    public static final RegistryObject<InstalledObjectBlock> INSTALLED_OBJECT
        = BLOCKS.register("installed_object", () -> new InstalledObjectBlock());

    /** 本家 electric: 信号変換器 (RSIn/RSOut/Increment/Decrement) */
    public static final RegistryObject<jp.ngt.rtm.electric.BlockSignalConverter> SIGNAL_CONVERTER
        = BLOCKS.register("signal_converter", () -> new jp.ngt.rtm.electric.BlockSignalConverter(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                .strength(1.5F).sound(net.minecraft.world.level.block.SoundType.STONE)));

    /** SignalControllerMod (masa300) 移植: 信号制御器 (閉塞信号の自動制御) */
    public static final RegistryObject<jp.masa.signalcontrollermod.SignalController> SIGNAL_CONTROLLER
        = BLOCKS.register("signal_controller", () -> new jp.masa.signalcontrollermod.SignalController(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()));

    public static final RegistryObject<SignalRemoteBlock> SIGNAL_RECEIVER
        = BLOCKS.register("signal_receiver", () -> new SignalRemoteBlock(SignalRemoteBlock.Mode.RECEIVER));

    public static final RegistryObject<SignalRemoteBlock> SIGNAL_CHANGER
        = BLOCKS.register("signal_changer", () -> new SignalRemoteBlock(SignalRemoteBlock.Mode.CHANGER));

    public static final RegistryObject<SignalRemoteBlock> SIGNAL_VALUE_RECEIVER
        = BLOCKS.register("signal_value_receiver", () -> new SignalRemoteBlock(SignalRemoteBlock.Mode.VALUE_INPUT));

    public static final RegistryObject<TrainDetectorBlock> TRAIN_DETECTOR
        = BLOCKS.register("train_detector", () -> new TrainDetectorBlock());

    public static final RegistryObject<SignalStateBlock> SIGNAL_STATE
        = BLOCKS.register("signal_state", () -> new SignalStateBlock());

    public static final RegistryObject<ScriptBlock> SCRIPT_BLOCK
        = BLOCKS.register("script_block", () -> new ScriptBlock());
}
