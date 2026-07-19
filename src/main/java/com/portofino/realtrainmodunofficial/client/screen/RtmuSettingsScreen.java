package com.portofino.realtrainmodunofficial.client.screen;

import com.portofino.realtrainmodunofficial.RtmuSettings;
import com.portofino.realtrainmodunofficial.network.RtmuSettingsPayload;
import com.portofino.realtrainmodunofficial.network.compat.PacketDistributor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * RTMU 設定画面 (ポーズメニューの「RTMU設定」ボタンから開く)。軽量化欄:
 * <ul>
 *   <li>車両描画距離: 0(無制限)/32〜256</li>
 *   <li>静止車両の再計算頻度: 標準/省エネ/積極</li>
 *   <li>遠方車両のライト・方向幕を省略: ON/OFF</li>
 * </ul>
 * 変更は即クライアントへ保存し、サーバーへ同期する。
 */
public class RtmuSettingsScreen extends Screen {

    private final Screen parent;

    public RtmuSettingsScreen(Screen parent) {
        super(Component.literal("RTMU設定"));
        this.parent = parent;
    }

    /** 「軽量化」セクション見出しの y。render() でラベルを描くために保持。 */
    private int perfHeaderY;

    @Override
    protected void init() {
        int cx = this.width / 2;
        int w = 220;
        int y = 36;

        this.perfHeaderY = y;
        y += 14;

        //車両描画距離: 0=無制限、32〜256。遠方車両を丸ごと省略して毎フレームのスクリプト実行を削る。
        addRenderableWidget(new VehicleDistanceSlider(cx - w / 2, y, w, 20));

        y += 24;
        //静止車両の再計算頻度: 標準/省エネ/積極。
        addRenderableWidget(CycleButton.<Integer>builder(RtmuSettingsScreen::throttleLabel)
            .withValues(0, 1, 2)
            .withInitialValue(clampThrottle(RtmuSettings.staticVehicleThrottle))
            .create(cx - w / 2, y, w, 20, Component.literal("静止車両の再計算"),
                (btn, value) -> {
                    RtmuSettings.staticVehicleThrottle = value;
                    RtmuSettings.save();
                    sync();
                }));

        y += 24;
        //遠方車両のライト・方向幕を省略。
        addRenderableWidget(CycleButton.onOffBuilder(RtmuSettings.skipDistantVehicleExtras)
            .create(cx - w / 2, y, w, 20,
                Component.literal("遠方車両のライト/幕を省略"),
                (btn, value) -> {
                    RtmuSettings.skipDistantVehicleExtras = value;
                    RtmuSettings.save();
                    sync();
                }));

        y += 30;
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
            .bounds(cx - 100, y, 200, 20).build());
    }

    private static int clampThrottle(int v) {
        return Math.max(0, Math.min(2, v));
    }

    private static Component throttleLabel(int v) {
        return Component.literal(switch (v) {
            case 1 -> "省エネ";
            case 2 -> "積極";
            default -> "標準";
        });
    }

    /** 車両描画距離スライダー (0=無制限、32〜256、16刻み)。 */
    private static class VehicleDistanceSlider extends net.minecraft.client.gui.components.AbstractSliderButton {
        //0(無制限) を左端、32〜256 を連続に並べる。内部 value 0.0 = 無制限。
        private static final int MIN = 32;
        private static final int MAX = 256;

        VehicleDistanceSlider(int x, int y, int w, int h) {
            super(x, y, w, h, Component.empty(), initialValue());
            updateMessage();
        }

        private static double initialValue() {
            int d = RtmuSettings.clampVehicleRenderDistance(RtmuSettings.vehicleRenderDistance);
            if (d <= 0) {
                return 0.0D;
            }
            //無制限のぶん左端に幅を持たせる (value 0〜0.08 = 無制限帯)。
            return 0.08D + (1.0D - 0.08D) * (d - MIN) / (double) (MAX - MIN);
        }

        private int currentDistance() {
            if (this.value < 0.08D) {
                return 0;  //無制限
            }
            double t = (this.value - 0.08D) / (1.0D - 0.08D);
            int raw = (int) Math.round(MIN + t * (MAX - MIN));
            return Math.max(MIN, Math.min(MAX, (raw / 16) * 16));
        }

        @Override
        protected void updateMessage() {
            int d = currentDistance();
            setMessage(Component.literal("車両描画距離: " + (d <= 0 ? "無制限" : d + "m")));
        }

        @Override
        protected void applyValue() {
            RtmuSettings.vehicleRenderDistance = currentDistance();
            RtmuSettings.save();
        }
    }

    private void sync() {
        if (minecraft != null && minecraft.getConnection() != null) {
            PacketDistributor.sendToServer(new RtmuSettingsPayload(
                RtmuSettings.vehicleRenderDistance, RtmuSettings.staticVehicleThrottle,
                RtmuSettings.skipDistantVehicleExtras));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 0xFFFFFF);
        int cx = this.width / 2;
        int left = cx - 110;
        graphics.drawString(this.font, Component.literal("§l軽量化"), left, this.perfHeaderY, 0xFF60C0FF, false);
        graphics.drawString(this.font,
            Component.literal("§7遠い車両を間引いて FPS を稼ぐ (既定=無制限)"),
            left + 44, this.perfHeaderY, 0xFF808080, false);
    }

    @Override
    public void onClose() {
        //閉じるときにも念のため同期
        sync();
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }
}
