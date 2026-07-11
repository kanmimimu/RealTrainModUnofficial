package com.portofino.realtrainmodunofficial.client.screen;

import com.portofino.realtrainmodunofficial.network.SignalControllerPayload;
import jp.masa.signalcontrollermod.SignalType;
import jp.masa.signalcontrollermod.TileEntitySignalController;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * SignalControllerMod (作者: masa300) の GUISignalController 1.21.1 移植。
 * 原作同様: SignalType 切替 / last / repeat / Reduced Speed / above の設定と
 * nextSignal / displayPos 一覧の表示 (追加は Pos Setting Tool で行う)。
 */
public class SignalControllerScreen extends Screen {
    private final TileEntitySignalController controller;
    private SignalType signalType;
    private boolean above;
    private boolean last;
    private boolean repeat;
    private boolean reducedSpeed;

    private Button typeButton;
    private Button lastButton;
    private Button repeatButton;
    private Button reducedButton;
    private Button aboveButton;

    public SignalControllerScreen(TileEntitySignalController controller) {
        super(Component.literal("SignalController"));
        this.controller = controller;
        this.signalType = controller.getSignalType();
        this.above = controller.isAbove();
        this.last = controller.isLast();
        this.repeat = controller.isRepeat();
        this.reducedSpeed = controller.isReducedSpeed();
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        this.typeButton = addRenderableWidget(Button.builder(typeLabel(), b -> {
            SignalType[] values = SignalType.values();
            this.signalType = values[(this.signalType.ordinal() + 1) % values.length];
            b.setMessage(typeLabel());
            send();
        }).bounds(cx - 40, cy - 80, 110, 20).build());

        this.lastButton = addRenderableWidget(Button.builder(toggleLabel("last", last), b -> {
            this.last = !this.last;
            b.setMessage(toggleLabel("last", last));
            send();
        }).bounds(cx - 130, cy - 50, 80, 20).build());

        this.repeatButton = addRenderableWidget(Button.builder(toggleLabel("repeat", repeat), b -> {
            this.repeat = !this.repeat;
            b.setMessage(toggleLabel("repeat", repeat));
            send();
        }).bounds(cx - 45, cy - 50, 80, 20).build());

        this.reducedButton = addRenderableWidget(Button.builder(toggleLabel("Reduced Speed", reducedSpeed), b -> {
            this.reducedSpeed = !this.reducedSpeed;
            b.setMessage(toggleLabel("Reduced Speed", reducedSpeed));
            send();
        }).bounds(cx + 40, cy - 50, 110, 20).build());

        this.aboveButton = addRenderableWidget(Button.builder(toggleLabel("above", above), b -> {
            this.above = !this.above;
            b.setMessage(toggleLabel("above", above));
            send();
        }).bounds(cx - 130, cy - 25, 80, 20).build());

        addRenderableWidget(Button.builder(Component.literal("NextSignal クリア"), b ->
                PacketDistributor.sendToServer(new SignalControllerPayload(
                        controller.getBlockPos(), 1, signalType.toString(), above, last, repeat, reducedSpeed))
        ).bounds(cx - 45, cy - 25, 95, 20).build());

        addRenderableWidget(Button.builder(Component.literal("DisplayPos クリア"), b ->
                PacketDistributor.sendToServer(new SignalControllerPayload(
                        controller.getBlockPos(), 2, signalType.toString(), above, last, repeat, reducedSpeed))
        ).bounds(cx + 55, cy - 25, 95, 20).build());

        addRenderableWidget(Button.builder(Component.literal("閉じる"), b -> onClose())
                .bounds(cx - 40, this.height - 30, 80, 20).build());
    }

    private Component typeLabel() {
        return Component.translatable("SignalControllerMod.gui.signalType." + this.signalType.toString());
    }

    private static Component toggleLabel(String name, boolean value) {
        return Component.literal(name + ": " + (value ? "ON" : "OFF"));
    }

    private void send() {
        PacketDistributor.sendToServer(new SignalControllerPayload(
                controller.getBlockPos(), 0, signalType.toString(), above, last, repeat, reducedSpeed));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int cx = this.width / 2;
        int cy = this.height / 2;
        graphics.drawString(this.font, "SignalController", cx - 130, 20, 0xFFFFFF);
        graphics.drawString(this.font, "SignalType", cx - 130, cy - 74, 0xFFFFFF);
        int y = cy;
        for (int i = 0; i < controller.getNextSignal().size(); i++) {
            BlockPos p = controller.getNextSignal().get(i);
            graphics.drawString(this.font, "nextSignal" + i + ": " + p.getX() + ", " + p.getY() + ", " + p.getZ(),
                    cx - 130, y, 0xFFFFFF);
            y += 12;
        }
        for (int i = 0; i < controller.getDisplayPos().size(); i++) {
            BlockPos p = controller.getDisplayPos().get(i);
            graphics.drawString(this.font, "displayPos" + i + ": " + p.getX() + ", " + p.getY() + ", " + p.getZ(),
                    cx - 130, y, 0xFFFFFF);
            y += 12;
        }
        if (this.above) {
            graphics.drawString(this.font, "above", cx - 130, y, 0xFFFFFF);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
