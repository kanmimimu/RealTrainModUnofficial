package com.portofino.realtrainmodunofficial.client;

import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import com.portofino.realtrainmodunofficial.RealTrainModUnofficialItems;
import com.portofino.realtrainmodunofficial.client.sound.LegacyScriptSoundManager;
import com.portofino.realtrainmodunofficial.client.screen.TrainControlScreen;
import com.portofino.realtrainmodunofficial.entity.TrainEntity;
import com.portofino.realtrainmodunofficial.entity.TrainSeatEntity;
import com.portofino.realtrainmodunofficial.network.MountTrainPayload;
import com.portofino.realtrainmodunofficial.network.TrainControlPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = RealTrainModUnofficial.MODID, value = Dist.CLIENT)
public final class TrainControlKeyHandler {
    private static final int HOLD_REPEAT_INITIAL_DELAY_TICKS = 7;
    private static final int HOLD_REPEAT_INTERVAL_TICKS = 2;
    private static boolean doorLeftChordDown;
    private static boolean doorRightChordDown;
    private static boolean shiftWasDown;
    private static int powerHoldTicks = -1;
    private static int brakeHoldTicks = -1;

    private TrainControlKeyHandler() {
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }
        // Phase 2: 本家忠実列車 (jp.ngt) — マスコン/降車/ドアのみ先行対応
        if (mc.player.getVehicle() instanceof jp.ngt.rtm.entity.train.EntityTrainBase rtmTrain) {
            handleRtmTrainKeys(mc, rtmTrain, event);
            return;
        }
        // slotPos 座席 (EntityFloor): スニークで降車
        if (mc.player.getVehicle() instanceof jp.ngt.rtm.entity.train.parts.EntityFloor floor) {
            if (TrainControlKeyMappings.matchesSneak(event.getKey(), event.getScanCode())) {
                PacketDistributor.sendToServer(new TrainControlPayload(floor.getId(), "dismount", 0));
            }
            return;
        }
        TrainEntity train = getControlledTrain(mc);
        if (train == null) {
            return;
        }
        if (TrainControlKeyMappings.matchesSneak(event.getKey(), event.getScanCode())) {
            PacketDistributor.sendToServer(new TrainControlPayload(train.getId(), "dismount", 0));
            return;
        }
        if (!train.isLikelyDriverPassenger(mc.player)) {
            return;
        }
        if (TrainControlKeyMappings.OPEN_CONTROL.matches(event.getKey(), event.getScanCode())) {
            mc.setScreen(new TrainControlScreen(train));
            return;
        }
        if (TrainControlKeyMappings.TOGGLE_CAB.matches(event.getKey(), event.getScanCode())) {
            TrainHudOverlay.toggleCabHidden();
            return;
        }
        if (TrainControlKeyMappings.POWER_OFF.matches(event.getKey(), event.getScanCode())) {
            PacketDistributor.sendToServer(new TrainControlPayload(train.getId(), "mascon_power", 0), new CustomPacketPayload[0]);
            powerHoldTicks = 0;
            brakeHoldTicks = -1;
            return;
        }
        if (TrainControlKeyMappings.BRAKE_OFF.matches(event.getKey(), event.getScanCode())) {
            PacketDistributor.sendToServer(new TrainControlPayload(train.getId(), "mascon_brake", 0), new CustomPacketPayload[0]);
            brakeHoldTicks = 0;
            powerHoldTicks = -1;
            return;
        }
        if (TrainControlKeyMappings.NEUTRAL.matches(event.getKey(), event.getScanCode())) {
            PacketDistributor.sendToServer(new TrainControlPayload(train.getId(), "mascon_neutral", 0), new CustomPacketPayload[0]);
        }

        boolean jumpDown = mc.options.keyJump.isDown();
        if (jumpDown && event.getKey() == GLFW.GLFW_KEY_LEFT) {
            PacketDistributor.sendToServer(new TrainControlPayload(train.getId(), "toggle_door_left", 0));
            doorLeftChordDown = true;
            return;
        }
        if (jumpDown && event.getKey() == GLFW.GLFW_KEY_RIGHT) {
            PacketDistributor.sendToServer(new TrainControlPayload(train.getId(), "toggle_door_right", 0));
            doorRightChordDown = true;
        }
    }

    /**
     * 本家 1122: マーカーのアンカー線を右クリックで掴む/確定 (レール形状編集)。
     * 掴み/確定時はブロック・アイテムの右クリックを消費する。
     */
    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT || event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }
        if (com.portofino.realtrainmodunofficial.client.renderer.MarkerBlockEntityRenderer.onRightClick()) {
            event.setCanceled(true);
        }
    }

    /**
     * 本家: 運転席乗車中にインベントリキー → 運転台 GUI (通常インベントリを差し替え)。
     */
    @SubscribeEvent
    public static void onScreenOpening(net.neoforged.neoforge.client.event.ScreenEvent.Opening event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (!(event.getNewScreen() instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen)) {
            return;
        }
        if (mc.player.getVehicle() instanceof jp.ngt.rtm.entity.train.EntityTrainBase rtmTrain) {
            event.setNewScreen(new com.portofino.realtrainmodunofficial.client.screen.RtmTrainControlScreen(rtmTrain));
        }
    }

    /**
     * jp.ngt.rtm.entity.train.EntityTrainBase 用のキー処理 (Phase 2 先行版)。
     */
    private static void handleRtmTrainKeys(Minecraft mc, jp.ngt.rtm.entity.train.EntityTrainBase train,
                                           InputEvent.Key event) {
        int id = train.getId();
        if (TrainControlKeyMappings.matchesSneak(event.getKey(), event.getScanCode())) {
            PacketDistributor.sendToServer(new TrainControlPayload(id, "dismount", 0));
            return;
        }
        if (TrainControlKeyMappings.TOGGLE_CAB.matches(event.getKey(), event.getScanCode())) {
            TrainHudOverlay.toggleCabHidden();
            return;
        }
        if (TrainControlKeyMappings.POWER_OFF.matches(event.getKey(), event.getScanCode())) {
            PacketDistributor.sendToServer(new TrainControlPayload(id, "mascon_power", 0));
            return;
        }
        if (TrainControlKeyMappings.BRAKE_OFF.matches(event.getKey(), event.getScanCode())) {
            PacketDistributor.sendToServer(new TrainControlPayload(id, "mascon_brake", 0));
            return;
        }
        if (TrainControlKeyMappings.NEUTRAL.matches(event.getKey(), event.getScanCode())) {
            PacketDistributor.sendToServer(new TrainControlPayload(id, "mascon_neutral", 0));
            return;
        }
        boolean jumpDown = mc.options.keyJump.isDown();
        if (jumpDown && event.getKey() == GLFW.GLFW_KEY_LEFT) {
            PacketDistributor.sendToServer(new TrainControlPayload(id, "toggle_door_left", 0));
            return;
        }
        if (jumpDown && event.getKey() == GLFW.GLFW_KEY_RIGHT) {
            PacketDistributor.sendToServer(new TrainControlPayload(id, "toggle_door_right", 0));
        }
    }

    /**
     * 座席追従用: 前 tick の車両ヨー (NaN = 未着席)
     */
    private static float lastSeatVehicleYaw = Float.NaN;

    /**
     * 本家: 座席 (客席/運転席) に座っている間、車両のヨー変化を乗員の視点にも適用する。
     * これが無いと列車がカーブしても体がワールド方向に固定されたままになり、
     * 気づくと横や後ろを向いている状態になる。マウスでの視点操作は自由なまま。
     */
    private static void followSeatRotation(Minecraft mc) {
        net.minecraft.world.entity.Entity vehicle = mc.player.getVehicle();
        jp.ngt.rtm.entity.vehicle.EntityVehicleBase<?> base = null;
        if (vehicle instanceof jp.ngt.rtm.entity.train.parts.EntityVehiclePart part) {
            base = part.getVehicle();
        } else if (vehicle instanceof jp.ngt.rtm.entity.vehicle.EntityVehicleBase<?> v) {
            base = v;
        }
        if (base == null) {
            lastSeatVehicleYaw = Float.NaN;
            return;
        }
        float yaw = base.getYRot();
        if (!Float.isNaN(lastSeatVehicleYaw)) {
            float delta = net.minecraft.util.Mth.wrapDegrees(yaw - lastSeatVehicleYaw);
            if (delta != 0.0F) {
                //yRotO は触らない — フレーム補間で滑らかに回るように
                mc.player.setYRot(mc.player.getYRot() + delta);
                mc.player.setYBodyRot(mc.player.yBodyRot + delta);
                mc.player.setYHeadRot(mc.player.getYHeadRot() + delta);
            }
        }
        lastSeatVehicleYaw = yaw;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            shiftWasDown = false;
            lastSeatVehicleYaw = Float.NaN;
            resetHoldState();
            return;
        }

        followSeatRotation(mc);

        if (TrainControlKeyMappings.TOGGLE_RENDER_PROFILER.consumeClick()) {
            ClientRenderProfiler.toggleOverlay();
        }

        if (mc.screen != null) {
            doorLeftChordDown = false;
            doorRightChordDown = false;
            resetHoldState();
            return;
        }

        TrainEntity train = getControlledTrain(mc);
        if (train == null) {
            shiftWasDown = false;
            doorLeftChordDown = false;
            doorRightChordDown = false;
            resetHoldState();
            return;
        }
        boolean shiftDown = mc.options.keyShift.isDown();
        if (shiftDown && !shiftWasDown) {
            PacketDistributor.sendToServer(new TrainControlPayload(train.getId(), "dismount", 0));
            shiftWasDown = true;
            return;
        }
        shiftWasDown = shiftDown;
        if (!train.isLikelyDriverPassenger(mc.player)) {
            doorLeftChordDown = false;
            doorRightChordDown = false;
            resetHoldState();
            return;
        }
        if (TrainControlKeyMappings.OPEN_CONTROL.consumeClick()) {
            mc.setScreen(new TrainControlScreen(train));
        }
        if (TrainControlKeyMappings.TOGGLE_CAB.consumeClick()) {
            TrainHudOverlay.toggleCabHidden();
        }
        if (TrainControlKeyMappings.PLAY_ANNOUNCEMENT.consumeClick()) {
            PacketDistributor.sendToServer(new TrainControlPayload(train.getId(), "play_selected_announcement", 0));
        }
        if (TrainControlKeyMappings.PLAY_HORN.consumeClick()) {
            PacketDistributor.sendToServer(new TrainControlPayload(train.getId(), "play_horn", 0));
        }
        if (TrainControlKeyMappings.NEUTRAL.consumeClick()) {
            PacketDistributor.sendToServer(new TrainControlPayload(train.getId(), "mascon_neutral", 0), new CustomPacketPayload[0]);
        }

        boolean jumpDown = mc.options.keyJump.isDown();
        boolean leftArrowDown = GLFW.glfwGetKey(mc.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS;
        boolean rightArrowDown = GLFW.glfwGetKey(mc.getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS;
        if (jumpDown && leftArrowDown) {
            if (!doorLeftChordDown) {
                PacketDistributor.sendToServer(new TrainControlPayload(train.getId(), "toggle_door_left", 0));
                doorLeftChordDown = true;
            }
        } else {
            doorLeftChordDown = false;
        }
        if (jumpDown && rightArrowDown) {
            if (!doorRightChordDown) {
                PacketDistributor.sendToServer(new TrainControlPayload(train.getId(), "toggle_door_right", 0));
                doorRightChordDown = true;
            }
        } else {
            doorRightChordDown = false;
        }

        boolean powerHeld = TrainControlKeyMappings.POWER_OFF.isDown();
        boolean brakeHeld = TrainControlKeyMappings.BRAKE_OFF.isDown();
        if (powerHeld && !brakeHeld) {
            powerHoldTicks = Math.max(0, powerHoldTicks + 1);
            brakeHoldTicks = -1;
            if (shouldSendRepeat(powerHoldTicks)) {
                PacketDistributor.sendToServer(new TrainControlPayload(train.getId(), "mascon_power", 0), new CustomPacketPayload[0]);
            }
        } else if (brakeHeld && !powerHeld) {
            brakeHoldTicks = Math.max(0, brakeHoldTicks + 1);
            powerHoldTicks = -1;
            if (shouldSendRepeat(brakeHoldTicks)) {
                PacketDistributor.sendToServer(new TrainControlPayload(train.getId(), "mascon_brake", 0), new CustomPacketPayload[0]);
            }
        } else {
            resetHoldState();
        }
    }

    @SubscribeEvent
    public static void onUseKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || mc.player.getVehicle() != null) {
            return;
        }
        if (mc.hitResult instanceof EntityHitResult) {
            return;
        }
        ItemStack mainHand = mc.player.getMainHandItem();
        ItemStack offHand = mc.player.getOffhandItem();
        boolean holdingCrowbar = mainHand.is(RealTrainModUnofficialItems.CROWBAR_ITEM.get())
            || offHand.is(RealTrainModUnofficialItems.CROWBAR_ITEM.get());
        if (!holdingCrowbar) {
            return;
        }
        PacketDistributor.sendToServer(MountTrainPayload.INSTANCE);
    }

    private static boolean shouldSendRepeat(int heldTicks) {
        if (heldTicks < HOLD_REPEAT_INITIAL_DELAY_TICKS) {
            return false;
        }
        return (heldTicks - HOLD_REPEAT_INITIAL_DELAY_TICKS) % HOLD_REPEAT_INTERVAL_TICKS == 0;
    }

    private static TrainEntity getControlledTrain(Minecraft mc) {
        if (mc.player == null) {
            return null;
        }
        if (mc.player.getVehicle() instanceof TrainEntity train) {
            return train;
        }
        if (mc.player.getVehicle() instanceof TrainSeatEntity seat) {
            return seat.getTrain();
        }
        return null;
    }

    private static void resetHoldState() {
        powerHoldTicks = -1;
        brakeHoldTicks = -1;
    }
}
