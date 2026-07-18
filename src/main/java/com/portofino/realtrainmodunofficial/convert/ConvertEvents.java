package com.portofino.realtrainmodunofficial.convert;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.nio.file.Path;
import java.util.List;

/**
 * 旧ワールド変換の入り口。
 *
 * <ul>
 *   <li>起動時: {@code .minecraft/rtmu_convert/} に置かれた旧ワールドを {@code saves/} へ変換する</li>
 *   <li>ワールドを開いたとき: 変換済みワールドなら RTM オブジェクトを置き直す</li>
 * </ul>
 */
public final class ConvertEvents {

    private ConvertEvents() {
    }

    /** 起動時の自動変換 (クライアント)。 */
    @EventBusSubscriber(modid = RealTrainModUnofficial.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Setup {
        private Setup() {
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                Path gameDir = net.minecraft.client.Minecraft.getInstance().gameDirectory.toPath();
                List<WorldConverter.Report> reports = WorldConverter.convertAll(gameDir, gameDir.resolve("saves"));
                for (WorldConverter.Report report : reports) {
                    RealTrainModUnofficial.LOGGER.info(
                            "[convert] 「{}」を変換しました → {} (RTM オブジェクト {} 個 / 列車 {} 両)",
                            report.name(), report.output().getFileName(), report.objects(), report.entities());
                }
            });
        }
    }

    /** ワールドを開いたときの復元 (サーバー側)。 */
    @EventBusSubscriber(modid = RealTrainModUnofficial.MODID)
    public static final class Game {
        private Game() {
        }

        @SubscribeEvent
        public static void onServerStarted(ServerStartedEvent event) {
            LegacyRestorer.onServerStarted(net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer());
        }

        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            LegacyRestorer.tick(net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer());
        }

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("rtmu")
                    .then(Commands.literal("convert")
                            .then(Commands.literal("status").executes(ctx -> {
                                String msg = LegacyRestorer.isRunning()
                                        ? "旧ワールドの復元中: " + LegacyRestorer.progress()
                                        : "復元中のものはありません。";
                                ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
                                return 1;
                            })));
            event.getDispatcher().register(root);
        }
    }
}
