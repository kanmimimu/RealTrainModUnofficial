package com.portofino.realtrainmodunofficial.client;

import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.List;

@EventBusSubscriber(modid = RealTrainModUnofficial.MODID, value = Dist.CLIENT)
public final class TitleScreenWarningOverlay {
    private TitleScreenWarningOverlay() {
    }

    //同意画面をこのセッションで一度開いたか (タイトルへ戻る度に開き直さない)。
    private static boolean consentOpened;

    /**
     * タイトル画面が開いたら、README 未同意のパックがあれば同意画面を出す。
     * (パック読み込みはタイトル画面より前に済んでいるので、この時点で未決一覧が揃っている)
     */
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof TitleScreen)) {
            return;
        }
        if (consentOpened || !com.portofino.realtrainmodunofficial.pack.PackConsent.hasPending()) {
            return;
        }
        consentOpened = true;
        Minecraft mc = Minecraft.getInstance();
        //init 中の setScreen 再入を避けて次tickで開く。
        mc.execute(() -> {
            if (mc.screen instanceof TitleScreen) {
                var screen = com.portofino.realtrainmodunofficial.client.screen.PackConsentScreen
                        .createIfPending(mc.screen);
                if (screen != null) {
                    mc.setScreen(screen);
                }
            }
        });
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof TitleScreen)) {
            return;
        }
        List<String> warnings = PackRequirementWarnings.getWarnings();
        if (warnings.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        GuiGraphics graphics = event.getGuiGraphics();
        int x = 8;
        int y = 8;
        int maxWidth = 0;
        for (String warning : warnings) {
            maxWidth = Math.max(maxWidth, minecraft.font.width(warning));
        }
        int height = warnings.size() * (minecraft.font.lineHeight + 2) + 6;
        graphics.fill(x - 4, y - 4, x + maxWidth + 6, y + height, 0xB0200000);
        int lineY = y;
        for (String warning : warnings) {
            graphics.drawString(minecraft.font, warning, x, lineY, 0xFFFF66, false);
            lineY += minecraft.font.lineHeight + 2;
        }
    }
}
