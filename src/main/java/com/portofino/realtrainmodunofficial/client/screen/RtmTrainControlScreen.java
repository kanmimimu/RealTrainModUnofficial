package com.portofino.realtrainmodunofficial.client.screen;

import com.portofino.realtrainmodunofficial.network.TrainControlPayload;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.train.util.TrainState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * jp.ngt 列車の運転台 GUI (本家 GuiTrainCtrl 相当の簡易版)。
 * 運転席乗車中にインベントリキーで開く。既存の TrainControlPayload 操作を使用。
 */
public class RtmTrainControlScreen extends Screen {
    private final EntityTrainBase train;

    public RtmTrainControlScreen(EntityTrainBase train) {
        super(Component.literal("運転台"));
        this.train = train;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int bw = 90;
        int bh = 20;
        int gap = 4;

        //左列: マスコン
        this.addRenderableWidget(Button.builder(Component.literal("ノッチ +"), b -> this.send("mascon_power"))
                .bounds(cx - bw - gap - bw / 2, cy - 60, bw, bh).build());
        this.addRenderableWidget(Button.builder(Component.literal("ノッチ −(ブレーキ)"), b -> this.send("mascon_brake"))
                .bounds(cx - bw - gap - bw / 2, cy - 60 + (bh + gap), bw, bh).build());
        this.addRenderableWidget(Button.builder(Component.literal("ニュートラル"), b -> this.send("mascon_neutral"))
                .bounds(cx - bw - gap - bw / 2, cy - 60 + (bh + gap) * 2, bw, bh).build());
        this.addRenderableWidget(Button.builder(Component.literal("前後切替"), b -> this.send("toggle_reverse"))
                .bounds(cx - bw - gap - bw / 2, cy - 60 + (bh + gap) * 3, bw, bh).build());

        //中列: ドア/灯
        this.addRenderableWidget(Button.builder(Component.literal("ドア 左"), b -> this.send("toggle_door_left"))
                .bounds(cx - bw / 2, cy - 60, bw, bh).build());
        this.addRenderableWidget(Button.builder(Component.literal("ドア 右"), b -> this.send("toggle_door_right"))
                .bounds(cx - bw / 2, cy - 60 + (bh + gap), bw, bh).build());
        this.addRenderableWidget(Button.builder(Component.literal("前照灯/尾灯"), b -> this.send("toggle_headlight"))
                .bounds(cx - bw / 2, cy - 60 + (bh + gap) * 2, bw, bh).build());
        this.addRenderableWidget(Button.builder(Component.literal("室内灯"), b -> this.send("toggle_interior_light"))
                .bounds(cx - bw / 2, cy - 60 + (bh + gap) * 3, bw, bh).build());

        //右列: その他
        this.addRenderableWidget(Button.builder(Component.literal("パンタグラフ"), b -> this.send("toggle_pantograph"))
                .bounds(cx + bw / 2 + gap, cy - 60, bw, bh).build());
        this.addRenderableWidget(Button.builder(Component.literal("降りる"), b -> {
            this.send("dismount");
            this.onClose();
        }).bounds(cx + bw / 2 + gap, cy - 60 + (bh + gap), bw, bh).build());
        this.addRenderableWidget(Button.builder(Component.literal("閉じる"), b -> this.onClose())
                .bounds(cx + bw / 2 + gap, cy - 60 + (bh + gap) * 3, bw, bh).build());
    }

    private void send(String action) {
        PacketDistributor.sendToServer(new TrainControlPayload(this.train.getId(), action, 0));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int cx = this.width / 2;
        int cy = this.height / 2;
        int notch = this.train.getNotch();
        String notchText = notch > 0 ? "P" + notch : (notch < 0 ? "B" + (-notch) : "N");
        int speedKmh = Math.round(Math.abs(this.train.getSpeed()) * 72.0F);
        TrainState dir = this.train.getTrainState(10);
        String dirText = dir == TrainState.Direction_Back ? "後" : "前";
        graphics.drawCenteredString(this.font,
                "ノッチ: " + notchText + "   速度: " + speedKmh + " km/h   進行: " + dirText,
                cx, cy - 85, 0xFFFFFF);
    }

    /**
     * 開いたままでも運転キー (W/S 等) が効くように game を止めない
     */
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * インベントリキーでも閉じられるように
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
