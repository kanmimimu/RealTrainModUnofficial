package com.portofino.realtrainmodunofficial;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.portofino.realtrainmodunofficial.block.LargeRailCoreBlock;
import com.portofino.realtrainmodunofficial.block.RailCollisionBlock;
import com.portofino.realtrainmodunofficial.entity.TrainBogieEntity;
import com.portofino.realtrainmodunofficial.entity.TrainEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = RealTrainModUnofficial.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class TrainCommands {
    private TrainCommands() {
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            Commands.literal("del")
                .then(Commands.literal("train")
                    .executes(context -> executeDeleteTrain(context.getSource()))
                )
        );

        dispatcher.register(
            Commands.literal("rtm")
                .then(Commands.literal("delAlltrain")
                    .requires(source -> source.hasPermission(2))
                    .executes(context -> executeDeleteTrain(context.getSource()))
                )
                //小文字表記でも効くように (コマンドリテラルは大文字小文字を区別する)
                .then(Commands.literal("delalltrain")
                    .requires(source -> source.hasPermission(2))
                    .executes(context -> executeDeleteTrain(context.getSource()))
                )
                .then(Commands.literal("flyspeed")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("speed", IntegerArgumentType.integer(1, 10))
                        .executes(context -> executeSetFlySpeed(
                            context.getSource(),
                            IntegerArgumentType.getInteger(context, "speed")
                        ))
                    )
                )
                .then(Commands.literal("nashorntest")
                    .requires(source -> source.hasPermission(2))
                    .executes(context -> executeNashornTest(context.getSource()))
                )
                //本家 MacroRecorder: 運転操作 (ノッチ/ドア/警笛) をマクロとして録画する。
                //start → 列車を運転 → stop で config/realtrainmodunofficial/macro/日時.txt へ保存。
                //保存したマクロは運転士 (素手右クリック) が再生できる。
                .then(Commands.literal("macro")
                    .then(Commands.literal("start")
                        .executes(context -> {
                            var player = context.getSource().getPlayerOrException();
                            if (!jp.ngt.rtm.entity.npc.macro.MacroRecorder.start(player)) {
                                context.getSource().sendFailure(
                                    net.minecraft.network.chat.Component.literal("すでに録画中です"));
                            }
                            return 1;
                        })
                    )
                    .then(Commands.literal("stop")
                        .executes(context -> {
                            var player = context.getSource().getPlayerOrException();
                            if (!jp.ngt.rtm.entity.npc.macro.MacroRecorder.stop(player)) {
                                context.getSource().sendFailure(
                                    net.minecraft.network.chat.Component.literal("録画していません (/rtm macro start)"));
                            }
                            return 1;
                        })
                    )
                )
        );
    }

    private static int executeDeleteTrain(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        int removedCount = 0;
        TrainEntity.clearCouplingModes();

        for (ServerLevel level : server.getAllLevels()) {
            removedCount += removeTrainEntities(level);
            removeBogieEntities(level);
            removedCount += removeRtmTrainEntities(level);
            removeRailCollisionBlocks(level);
        }

        int finalRemovedCount = removedCount;
        source.sendSuccess(() -> Component.literal("電車を " + finalRemovedCount + " 両削除しました。残って見える場合はワールドを開き直してください。"), true);
        return removedCount;
    }

    /**
     * Phase 0 スモークテスト: スタンドアロン Nashorn が本家 RTM と同じフラグ
     * ("-doe" "--language=es6" + mozilla_compat.js) で NeoForge/Java21 上で動き、
     * MOD クラス (Packages.jp.ngt / Java.type) を解決できるかを検証する。
     */
    private static int executeNashornTest(CommandSourceStack source) {
        try {
            String script =
                // mozilla_compat: importPackage / importClass が生えていること
                "importPackage(java.util);\n" +
                "var list = new ArrayList(); list.add('a'); list.add('b');\n" +
                // ES6 構文が有効であること (--language=es6)
                "let square = (x) => x * x;\n" +
                "const msg = `es6:${square(4)}`;\n" +
                // MOD クラスローダ経由で jp.ngt.* が見えること
                "var su = Java.type('jp.ngt.ngtlib.io.ScriptUtil');\n" +
                "function result() { return msg + ' list:' + list.size() + ' cls:' + su.class.getSimpleName(); }\n";
            javax.script.ScriptEngine se = jp.ngt.ngtlib.io.ScriptUtil.doScript(script);
            Object result = jp.ngt.ngtlib.io.ScriptUtil.doScriptFunction(se, "result");
            String engineName = se.getFactory().getEngineName() + " " + se.getFactory().getEngineVersion();
            source.sendSuccess(() -> Component.literal(
                "Nashorn OK: engine=[" + engineName + "] result=[" + result + "]"), false);
            return 1;
        } catch (Throwable t) {
            String detail = t.getClass().getSimpleName() + ": " + t.getMessage();
            source.sendFailure(Component.literal("Nashorn NG: " + detail));
            RealTrainModUnofficial.LOGGER.error("Nashorn smoke test failed", t);
            return 0;
        }
    }

    private static int executeSetFlySpeed(CommandSourceStack source, int speed) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        float normalizedSpeed = 0.05F * speed;
        player.getAbilities().setFlyingSpeed(normalizedSpeed);
        player.onUpdateAbilities();
        source.sendSuccess(() -> Component.literal("飛行速度を " + speed + " に設定しました。"), false);
        return speed;
    }

    private static int removeTrainEntities(ServerLevel level) {
        AABB worldAABB = new AABB(-3.0E7D, -2048.0D, -3.0E7D, 3.0E7D, 4096.0D, 3.0E7D);
        List<TrainEntity> trains = new ArrayList<>(level.getEntitiesOfClass(TrainEntity.class, worldAABB, entity -> true));
        try {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof TrainEntity train && !trains.contains(train)) {
                    trains.add(train);
                }
            }
        } catch (Exception ignored) {
        }
        for (TrainEntity train : trains) {
            train.forceDiscardTrain();
        }
        return trains.size();
    }

    private static void removeBogieEntities(ServerLevel level) {
        AABB worldAABB = new AABB(-3.0E7D, -2048.0D, -3.0E7D, 3.0E7D, 4096.0D, 3.0E7D);
        List<TrainBogieEntity> bogies = new ArrayList<>(level.getEntitiesOfClass(TrainBogieEntity.class, worldAABB, entity -> true));
        try {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof TrainBogieEntity bogie && !bogies.contains(bogie)) {
                    bogies.add(bogie);
                }
            }
        } catch (Exception ignored) {
        }
        for (TrainBogieEntity bogie : bogies) {
            bogie.discard();
        }
    }

    /**
     * 本家系の列車 (jp.ngt.rtm.entity.train.EntityTrainBase — 設置される列車はこちら) と
     * その台車・車両パーツを全て削除する。旧 TrainEntity の削除だけでは実車が残る。
     */
    private static int removeRtmTrainEntities(ServerLevel level) {
        List<Entity> targets = new ArrayList<>();
        int trainCount = 0;
        try {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof jp.ngt.rtm.entity.train.EntityTrainBase) {
                    targets.add(entity);
                    trainCount++;
                } else if (entity instanceof jp.ngt.rtm.entity.train.EntityBogie
                        || entity instanceof jp.ngt.rtm.entity.train.parts.EntityVehiclePart) {
                    targets.add(entity);
                }
            }
        } catch (Exception ignored) {
        }
        for (Entity entity : targets) {
            entity.discard();
        }
        return trainCount;
    }

    private static void removeRailCollisionBlocks(ServerLevel level) {
        if (!(level.getChunkSource() instanceof ServerChunkCache cache)) {
            return;
        }

        try {
            java.lang.reflect.Field field = ServerChunkCache.class.getDeclaredField("chunkMap");
            field.setAccessible(true);
            Object chunkMap = field.get(cache);
            java.lang.reflect.Method method = chunkMap.getClass().getMethod("getChunks");
            Iterable<?> chunks = (Iterable<?>) method.invoke(chunkMap);

            for (Object holderObject : chunks) {
                if (!(holderObject instanceof ChunkHolder holder)) {
                    continue;
                }
                Optional<ChunkAccess> optional = Optional.ofNullable(holder.getLatestChunk());
                if (optional.isEmpty() || !(optional.get() instanceof LevelChunk chunk)) {
                    continue;
                }

                List<BlockPos> blockPositions = new ArrayList<>(chunk.getBlockEntities().keySet());
                for (BlockPos pos : blockPositions) {
                    BlockState blockState = chunk.getBlockState(pos);
                    if (blockState.getBlock() instanceof RailCollisionBlock) {
                        level.removeBlock(pos, false);
                    } else if (blockState.getBlock() instanceof LargeRailCoreBlock) {
                        level.removeBlock(pos, false);
                    }
                }
            }
        } catch (ReflectiveOperationException e) {
            // If reflection fails, skip removing block entities rather than crashing.
        }
    }
}
