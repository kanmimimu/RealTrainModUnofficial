package com.portofino.realtrainmodunofficial.client.screen;

import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import com.portofino.realtrainmodunofficial.RealTrainModUnofficialItems;
import com.portofino.realtrainmodunofficial.network.TrainControlPayload;
import com.portofino.realtrainmodunofficial.vehicle.VehicleDefinition;
import com.portofino.realtrainmodunofficial.vehicle.VehicleRegistry;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.train.util.TrainState.TrainStateType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * jp.ngt 列車の運転台メニュー — 本家 GuiTrainCtrl 相当 (RTM 風テクスチャ付き)。
 * 旧 TrainControlScreen のレイアウト/テクスチャを EntityTrainBase (TrainState) 向けに移植。
 * 運転席乗車中にインベントリキーで開く。
 */
public class RtmTrainControlScreen extends Screen {
    private static final ResourceLocation TAB_INVENTORY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RealTrainModUnofficial.MODID, "textures/gui/tab_inventory.png");
    private static final ResourceLocation TAB_SETTING_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RealTrainModUnofficial.MODID, "textures/gui/tab_setting.png");
    private static final ResourceLocation TAB_FORMATION_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RealTrainModUnofficial.MODID, "textures/gui/tab_formation.png");

    private static final int PANEL_W = 176;
    private static final int PANEL_H = 166;
    private static final int TAB_W = 28;
    private static final int TAB_H = 32;

    private final EntityTrainBase train;
    private ControlTab selectedTab = ControlTab.SETTING;
    private int refreshTimer;

    public RtmTrainControlScreen(EntityTrainBase train) {
        super(Component.literal("Train Control Panel"));
        this.train = train;
    }

    @Override
    protected void init() {
        rebuildTabWidgets();
    }

    private void rebuildTabWidgets() {
        clearWidgets();
        int left = leftPos();
        int top = topPos();
        if (selectedTab == ControlTab.SETTING) {
            addButton(left + 4, top + 4, 82, interiorLightLabel(), "toggle_interior_light", 0);
            addButton(left + 90, top + 4, 82, lightLabel(), "set_light_mode", nextLightMode());
            addButton(left + 4, top + 28, 82, pantographLabel(), "toggle_pantograph", 0);
            int dir = train.getTrainStateData(TrainStateType.State_Direction.id);
            addButton(left + 90, top + 28, 27, "前", "set_direction", 0).active = dir != 0;
            addButton(left + 117, top + 28, 28, "中", "set_direction", 1).active = dir != 1;
            addButton(left + 145, top + 28, 27, "後", "set_direction", 2).active = dir != 2;
            addArrowButton(left + 4, top + 52, "<", "noop", 0);
            addButton(left + 28, top + 52, 120, "チャンクロード", "noop", 0);
            addArrowButton(left + 152, top + 52, ">", "noop", 0);
            int dest = train.getTrainStateData(TrainStateType.State_Destination.id);
            int destCount = Math.max(1, rollsignNames().length);
            addArrowButton(left + 4, top + 76, "<", "set_destination", Math.floorMod(dest - 1, destCount));
            addButton(left + 28, top + 76, 120, destinationLabel(), "set_destination", (dest + 1) % destCount);
            addArrowButton(left + 152, top + 76, ">", "set_destination", (dest + 1) % destCount);
            int announce = train.getTrainStateData(TrainStateType.State_Announcement.id);
            addArrowButton(left + 4, top + 100, "<", "set_announcement", Math.max(0, announce - 1));
            addButton(left + 28, top + 100, 120, "アナウンス " + (announce + 1), "set_announcement", announce + 1);
            addArrowButton(left + 152, top + 100, ">", "set_announcement", announce + 1);
        } else if (selectedTab == ControlTab.FUNCTION) {
            VehicleDefinition definition = VehicleRegistry.getById(train.getModelName());
            List<List<String>> options = definition != null ? definition.getCustomButtonOptions() : List.of();
            for (int i = 0; i < Math.min(18, options.size()); i++) {
                int x = left + 4 + (i % 3) * 56;
                int y = top + 4 + (i / 3) * 24;
                int index = i;
                int value = train.getResourceState().getDataMap().getInt("Button" + i);
                List<String> optionList = options.get(i);
                String text = optionList.isEmpty() ? ("カスタム" + (i + 1))
                        : optionList.get(Math.floorMod(value, optionList.size()));
                int packed = (index << 8) | (value & 0xFF);
                addRenderableWidget(Button.builder(Component.literal(text), b -> send("cycle_custom_button", packed))
                        .bounds(x, y, 52, 22).build());
            }
        }
        addRenderableWidget(new DoorButton(left + PANEL_W + 20, top + 20, false));
        addRenderableWidget(new DoorButton(left - 84, top + 20, true));
    }

    private String[] rollsignNames() {
        return train.getConfig().rollsignNames;
    }

    private Button addButton(int x, int y, int w, String label, String action, int value) {
        Button button = Button.builder(Component.literal(label), b -> send(action, value)).bounds(x, y, w, 20).build();
        if ("noop".equals(action)) {
            button.active = false;
        }
        return addRenderableWidget(button);
    }

    private void addArrowButton(int x, int y, String label, String action, int value) {
        Button button = Button.builder(Component.literal(label), b -> send(action, value)).bounds(x, y, 20, 20).build();
        if ("noop".equals(action)) {
            button.active = false;
        }
        addRenderableWidget(button);
    }

    private String lightLabel() {
        return switch (train.getTrainStateData(TrainStateType.State_Light.id)) {
            case 1 -> "前照灯";
            case 2 -> "前照灯・尾灯";
            default -> "消灯";
        };
    }

    private int nextLightMode() {
        return switch (train.getTrainStateData(TrainStateType.State_Light.id)) {
            case 0 -> 1;
            case 1 -> 2;
            default -> 0;
        };
    }

    private String interiorLightLabel() {
        return train.getTrainStateData(TrainStateType.State_InteriorLight.id) != 0 ? "室内灯 ON" : "室内灯 OFF";
    }

    private String pantographLabel() {
        return train.getTrainStateData(TrainStateType.State_Pantograph.id) != 0 ? "パンタ 上" : "パンタ 下";
    }

    private String destinationLabel() {
        String[] names = rollsignNames();
        int count = Math.max(1, names.length);
        int dest = Math.floorMod(train.getTrainStateData(TrainStateType.State_Destination.id), count);
        String name = names.length == 0 ? "なし" : names[dest];
        return "方向幕 " + name;
    }

    private void send(String action, int value) {
        if ("noop".equals(action)) {
            return;
        }
        //カスタムボタンは描画がクライアント DataMap を読むためローカルにも反映
        if ("cycle_custom_button".equals(action)) {
            int index = (value >>> 8) & 0xFF;
            int current = value & 0xFF;
            VehicleDefinition definition = VehicleRegistry.getById(train.getModelName());
            List<List<String>> options = definition != null ? definition.getCustomButtonOptions() : List.of();
            int next = (index < options.size() && !options.get(index).isEmpty())
                    ? (current + 1) % options.get(index).size()
                    : (current == 0 ? 1 : 0);
            train.getResourceState().getDataMap().setInt("Button" + index, next, 0);
        }
        PacketDistributor.sendToServer(new TrainControlPayload(train.getId(), action, value));
        rebuildTabWidgets();
    }

    /**
     * TrainState はサーバー→クライアント同期のため、少し遅れてラベルへ反映する
     */
    @Override
    public void tick() {
        super.tick();
        if (++refreshTimer >= 5) {
            refreshTimer = 0;
            rebuildTabWidgets();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            ControlTab tab = tabAt(mouseX, mouseY);
            if (tab != null) {
                selectedTab = tab;
                rebuildTabWidgets();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private ControlTab tabAt(double mouseX, double mouseY) {
        int left = leftPos();
        int top = topPos();
        for (ControlTab tab : ControlTab.values()) {
            int x = tabX(left, tab);
            int y = top - 28;
            if (mouseX >= x && mouseX < x + TAB_W && mouseY >= y && mouseY < y + TAB_H) {
                return tab;
            }
        }
        return null;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int left = leftPos();
        int top = topPos();
        renderTabs(graphics, left, top, false);
        graphics.blit(selectedTab.background, left - 1, top - 1, 0, 0, PANEL_W, PANEL_H, 256, 256);
        renderTabs(graphics, left, top, true);
        for (Renderable renderable : renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }
        renderTabContents(graphics, left, top);
        renderPlayerInventory(graphics, left, top);
    }

    private void renderTabContents(GuiGraphics graphics, int left, int top) {
        if (selectedTab != ControlTab.FORMATION) {
            return;
        }
        //TODO PacketFormation 移植後に編成全体を表示
        VehicleDefinition def = VehicleRegistry.getById(train.getModelName());
        String name = def == null ? train.getModelName() : def.getDisplayName();
        graphics.drawString(font, Component.literal("編成"), left + 8, top + 10, 0x404040, false);
        graphics.renderFakeItem(new ItemStack(RealTrainModUnofficialItems.TRAIN_ITEM.get()), left + 10, top + 26);
        graphics.drawString(font, Component.literal("1  " + name), left + 30, top + 30, 0x404040, false);
    }

    private void renderPlayerInventory(GuiGraphics graphics, int left, int top) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        List<ItemStack> items = minecraft.player.getInventory().items;
        if (selectedTab == ControlTab.INVENTORY) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) {
                    int index = 9 + row * 9 + col;
                    renderInventoryItem(graphics, items, index, left + 8 + col * 18, top + 84 + row * 18);
                }
            }
        }
        for (int col = 0; col < 9; col++) {
            renderInventoryItem(graphics, items, col, left + 8 + col * 18, top + 142);
        }
    }

    private void renderInventoryItem(GuiGraphics graphics, List<ItemStack> items, int index, int x, int y) {
        if (index < 0 || index >= items.size()) {
            return;
        }
        ItemStack stack = items.get(index);
        if (stack.isEmpty()) {
            return;
        }
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(font, stack, x, y);
    }

    private void renderTabs(GuiGraphics graphics, int left, int top, boolean selectedOnly) {
        for (ControlTab tab : ControlTab.values()) {
            if ((tab == selectedTab) != selectedOnly) {
                continue;
            }
            int x = tabX(left, tab);
            int y = top - 28;
            int bg = 0xFF707070;
            int light = 0xFF9A9A9A;
            int shadow = 0xFF4A4A4A;
            graphics.fill(x, y, x + TAB_W, y + TAB_H, 0xFFFFFFFF);
            graphics.fill(x + 2, y + 2, x + TAB_W - 2, y + TAB_H - 2, bg);
            graphics.fill(x + 2, y + 2, x + TAB_W - 2, y + 3, light);
            graphics.fill(x + 2, y + 2, x + 3, y + TAB_H - 2, light);
            graphics.fill(x + 2, y + TAB_H - 3, x + TAB_W - 2, y + TAB_H - 2, shadow);
            graphics.fill(x + TAB_W - 3, y + 2, x + TAB_W - 2, y + TAB_H - 2, shadow);
            graphics.renderFakeItem(tab.icon, x + 6, y + 8);
        }
    }

    private int tabX(int left, ControlTab tab) {
        int column = tab.ordinal() % 6;
        if (column == 5) {
            return left + PANEL_W - TAB_W;
        }
        return left + 28 * column + (column > 0 ? column : 0);
    }

    private int leftPos() {
        return (width - PANEL_W) / 2;
    }

    private int topPos() {
        return (height - PANEL_H) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private enum ControlTab {
        SETTING(TAB_SETTING_TEXTURE, new ItemStack(RealTrainModUnofficialItems.WRENCH_ITEM.get())),
        FUNCTION(TAB_SETTING_TEXTURE, new ItemStack(RealTrainModUnofficialItems.CROWBAR_ITEM.get())),
        FORMATION(TAB_FORMATION_TEXTURE, new ItemStack(RealTrainModUnofficialItems.TRAIN_ITEM.get())),
        INVENTORY(TAB_INVENTORY_TEXTURE, new ItemStack(Blocks.CHEST));

        final ResourceLocation background;
        final ItemStack icon;

        ControlTab(ResourceLocation background, ItemStack icon) {
            this.background = background;
            this.icon = icon;
        }
    }

    private class DoorButton extends Button {
        private final boolean leftDoor;

        DoorButton(int x, int y, boolean leftDoor) {
            super(x, y, 64, 80, Component.empty(),
                    b -> RtmTrainControlScreen.this.send(leftDoor ? "toggle_door_left" : "toggle_door_right", 0),
                    DEFAULT_NARRATION);
            this.leftDoor = leftDoor;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int doorState = train.getTrainStateData(TrainStateType.State_Door.id);
            boolean opened = leftDoor ? (doorState & 2) == 2 : (doorState & 1) == 1;
            int sliderOffset = opened ? -10 : -4;
            graphics.blit(TAB_INVENTORY_TEXTURE, getX() + 25, getY() + sliderOffset, 242, 80, 14, 100, 256, 256);
            graphics.blit(TAB_INVENTORY_TEXTURE, getX(), getY(), 192, 0, 64, 80, 256, 256);
            graphics.blit(TAB_INVENTORY_TEXTURE, getX() + 44, getY() + 48, 224, opened ? 80 : 88, 8, 8, 256, 256);
        }
    }
}
