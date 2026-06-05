package com.portofino.realtrainmodunofficial.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.portofino.realtrainmodunofficial.client.PackButtonTextureCache;
import com.portofino.realtrainmodunofficial.client.model.MqoModelLoader;
import com.portofino.realtrainmodunofficial.client.renderer.BogieRenderer;
import com.portofino.realtrainmodunofficial.installedobject.InstalledObjectRegistry;
import com.portofino.realtrainmodunofficial.rail.RailRegistry;
import com.portofino.realtrainmodunofficial.vehicle.VehicleDefinition;
import com.portofino.realtrainmodunofficial.vehicle.VehicleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class ModelSelectScreen extends Screen {
    public record SelectionResult(String modelId, String dataMapValue) {}

    public record ModelInfo(String id, String displayName, String packName, String buttonTexture, String category) {
        public ModelInfo(String id, String displayName, String packName, String buttonTexture) {
            this(id, displayName, packName, buttonTexture, "");
        }
    }

    // RTM 本家どおり: ボタン高さ 32px 固定、ボタン幅 160px 固定
    private static final int BTN_W = 160;
    private static final int BTN_H = 32;
    private static final int LIST_TOP = 24;
    private static final int LIST_BOTTOM_MARGIN = 4;

    // 3D プレビューモデルキャッシュ (id → model or null)
    private static final Map<String, MqoModelLoader.MqoModel> MODEL_CACHE = new ConcurrentHashMap<>();
    private static final Set<String> MISSING_MODEL_CACHE = ConcurrentHashMap.newKeySet();

    private final List<ModelInfo> models;
    private final Consumer<SelectionResult> onSelected;
    private final String initialSelectedId;
    private final String initialDataMapValue;

    private ModelList modelList;
    private EditBox dataMapBox;
    private String selectedId = null;

    // 3Dプレビューのマウス操作回転。ユーザーがドラッグするまでは自動回転。
    private float previewYaw = 0.0f;
    private float previewPitch = 15.0f;
    private boolean previewUserRotated = false;
    private boolean previewDragging = false;
    private double lastDragX, lastDragY;
    // マウスホイールでのズーム倍率(1.0=既定)。
    private float previewZoom = 1.0f;
    // プレビューでスクリプトを適用するためのワールド未追加の一時車両エンティティ(車両IDごとにキャッシュ)。
    private com.portofino.realtrainmodunofficial.entity.TrainEntity previewEntity;
    private String previewEntityId;

    public ModelSelectScreen(Component title, List<ModelInfo> models, Consumer<SelectionResult> onSelected) {
        this(title, models, onSelected, null, "");
    }

    public ModelSelectScreen(Component title, List<ModelInfo> models, Consumer<SelectionResult> onSelected,
                             String initialSelectedId, String initialDataMapValue) {
        super(title);
        this.models = models.stream()
            .sorted(Comparator
                .comparing((ModelInfo i) -> safe(i.category()), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(i -> safe(i.displayName()), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(i -> safe(i.id()), String.CASE_INSENSITIVE_ORDER))
            .toList();
        this.onSelected = onSelected;
        this.initialSelectedId = initialSelectedId;
        this.initialDataMapValue = initialDataMapValue == null ? "" : initialDataMapValue;
    }

    private static String safe(String v) { return v == null ? "" : v; }

    // ---- レイアウト計算 ----
    private int listWidth() { return BTN_W + 16; }
    private int rightLeft() { return listWidth() + 4; }
    private int rightWidth() { return Math.max(100, width - rightLeft() - 4); }
    private int previewSize() { return Math.min(rightWidth(), height - LIST_TOP - 60); }

    /** マウス座標が3Dプレビュー領域内か。 */
    private boolean isInPreviewArea(double mx, double my) {
        int rl = rightLeft();
        int rw = rightWidth();
        int ps = previewSize();
        return mx >= rl && mx <= rl + rw && my >= LIST_TOP && my <= LIST_TOP + ps;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && isInPreviewArea(mx, my)) {
            previewDragging = true;
            lastDragX = mx;
            lastDragY = my;
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dragX, double dragY) {
        if (previewDragging && button == 0) {
            previewUserRotated = true;
            previewYaw += (float) (mx - lastDragX) * 0.8f;
            previewPitch += (float) (my - lastDragY) * 0.8f;
            previewPitch = Mth.clamp(previewPitch, -89.0f, 89.0f);
            lastDragX = mx;
            lastDragY = my;
            return true;
        }
        return super.mouseDragged(mx, my, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0 && previewDragging) {
            previewDragging = false;
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (isInPreviewArea(mx, my)) {
            previewZoom = Mth.clamp(previewZoom * (scrollY > 0 ? 1.1f : 1.0f / 1.1f), 0.3f, 6.0f);
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    @Override
    protected void init() {
        int lw = listWidth();
        int listHeight = height - LIST_TOP - LIST_BOTTOM_MARGIN;
        modelList = new ModelList(minecraft, lw, listHeight, LIST_TOP, BTN_H);
        addRenderableWidget(modelList);

        int rl = rightLeft();
        int rw = rightWidth();
        int datamapY = LIST_TOP + previewSize() + 8;
        dataMapBox = new EditBox(font, rl, datamapY, rw, 20, Component.empty());
        dataMapBox.setValue(initialDataMapValue);
        addRenderableWidget(dataMapBox);

        int bw = Math.min(100, Math.max(60, (rw - 4) / 2));
        int btnY = datamapY + 28;
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), btn -> {
            var sel = modelList.getSelected();
            if (sel != null && !sel.header) {
                onSelected.accept(new SelectionResult(sel.id, dataMapBox == null ? "" : dataMapBox.getValue()));
            }
            onClose();
        }).bounds(rl, btnY, bw, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), btn -> onClose())
            .bounds(rl + bw + 4, btnY, bw, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, getTitle(), listWidth() / 2, 7, 0xFFFFFF);

        // 右パネル: 3D プレビュー
        int rl = rightLeft();
        int rw = rightWidth();
        int ps = previewSize();
        int previewLeft = rl;
        int previewTop = LIST_TOP;

        // 背景
        graphics.fill(previewLeft, previewTop, previewLeft + rw, previewTop + ps, 0x88000000);

        var sel = modelList.getSelected();
        if (sel != null && !sel.header) {
            render3DPreview(graphics, previewLeft + rw / 2, previewTop + ps / 2, ps, sel.id, sel.packName);
        }

        if (models.isEmpty()) {
            graphics.drawCenteredString(font,
                Component.translatable("screen.realtrainmodunofficial.no_models"),
                previewLeft + rw / 2, previewTop + ps / 2, 0xAAAAAA);
        }
    }

    /** RTM 本家と同じ: Y 軸回転する 3D モデルプレビューを GUI 空間に描画。 */
    private void render3DPreview(GuiGraphics graphics, int cx, int cy, int areaSize, String id, String packName) {
        MqoModelLoader.MqoModel model = getOrLoadModel(id, packName);
        if (model == null) return;
        VehicleDefinition vehicleDef = VehicleRegistry.getById(id);

        // ドラッグしていない間は自動でゆっくり回転。ドラッグ後はユーザー指定の角度。
        float yaw = previewUserRotated
            ? previewYaw
            : (System.currentTimeMillis() % 12000L) * 360.0f / 12000.0f;
        float pitch = previewUserRotated ? previewPitch : 15.0f;
        float[] bounds = computePreviewBounds(model, vehicleDef);
        float dx = bounds[3] - bounds[0];
        float dy = bounds[4] - bounds[1];
        float dz = bounds[5] - bounds[2];
        float maxDim = Math.max(dx, Math.max(dy, dz));
        if (maxDim < 1e-4f) maxDim = 1.0f;
        // areaSize の 82% に収める(より大きく表示) × ホイールズーム倍率。
        float scale = (areaSize * 0.82f) / maxDim * previewZoom;
        float mcx = (bounds[0] + bounds[3]) / 2f;
        float mcy = (bounds[1] + bounds[4]) / 2f;
        float mcz = (bounds[2] + bounds[5]) / 2f;

        graphics.enableScissor(cx - areaSize / 2, cy - areaSize / 2, cx + areaSize / 2, cy + areaSize / 2);
        PoseStack ps = graphics.pose();
        ps.pushPose();
        try {
            // GUI 空間: z=50 でモデルを GUI の上に重ねる (InventoryScreen 同方式)
            ps.translate(cx, cy, 50.0);
            // Y 反転 (GUI座標系は Y 下向き), Z 反転 (奥行き)
            ps.scale(scale, -scale, scale);
            // オービット: pitch(世界X) を外側、yaw(モデルY) を内側に適用すると、
            // 横ドラッグ=回転 / 縦ドラッグ=見上げ見下ろし、で好きな方向から見られる。
            ps.mulPose(Axis.XP.rotationDegrees(pitch));
            ps.mulPose(Axis.YP.rotationDegrees(yaw));
            // モデルの中心を原点に
            ps.translate(-mcx, -mcy, -mcz);

            Lighting.setupForEntityInInventory();
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            MultiBufferSource.BufferSource buf = Minecraft.getInstance().renderBuffers().bufferSource();
            Object previewEnt = (vehicleDef != null && model.hasRenderScript())
                ? getOrCreatePreviewEntity(vehicleDef, model) : null;
            renderStablePreviewModel(model, vehicleDef, ps, buf, previewEnt);
            buf.endBatch();
            RenderSystem.disableDepthTest();
            Lighting.setupFor3DItems();
        } finally {
            ps.popPose();
            graphics.disableScissor();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private static float[] computePreviewBounds(MqoModelLoader.MqoModel model, VehicleDefinition vehicleDef) {
        float[] bounds = model.computeBounds();
        if (vehicleDef == null) {
            return bounds;
        }

        float scale = vehicleDef.getModelScale();
        Vec3 offset = vehicleDef.getModelOffset();
        float minX = (float) (bounds[0] * scale + offset.x);
        float minY = (float) (bounds[1] * scale + offset.y);
        float minZ = (float) (bounds[2] * scale + offset.z);
        float maxX = (float) (bounds[3] * scale + offset.x);
        float maxY = (float) (bounds[4] * scale + offset.y);
        float maxZ = (float) (bounds[5] * scale + offset.z);

        for (VehicleDefinition.BogieDefinition bogie : vehicleDef.getBogies()) {
            if (bogie == null || bogie.position() == null) {
                continue;
            }
            Vec3 pos = bogie.position();
            float x = (float) (pos.x * scale + offset.x);
            float y = (float) ((pos.y + 0.24D) * scale + offset.y);
            float z = (float) (pos.z * scale + offset.z);
            minX = Math.min(minX, x - 1.0F * scale);
            minY = Math.min(minY, y - 1.0F * scale);
            minZ = Math.min(minZ, z - 1.0F * scale);
            maxX = Math.max(maxX, x + 1.0F * scale);
            maxY = Math.max(maxY, y + 1.0F * scale);
            maxZ = Math.max(maxZ, z + 1.0F * scale);
        }

        return new float[] { minX, minY, minZ, maxX, maxY, maxZ };
    }

    private static void renderStablePreviewModel(MqoModelLoader.MqoModel model, VehicleDefinition vehicleDef,
                                                 PoseStack poseStack, MultiBufferSource.BufferSource buffer,
                                                 Object previewEnt) {
        poseStack.pushPose();
        try {
            if (vehicleDef != null) {
                Vec3 offset = vehicleDef.getModelOffset();
                poseStack.translate(offset.x, offset.y, offset.z);
                float modelScale = vehicleDef.getModelScale();
                poseStack.scale(modelScale, modelScale, modelScale);
            }

            MqoModelLoader.GroupPredicate previewFilter = ModelSelectScreen::shouldRenderPreviewGroup;
            boolean rendered = false;
            if (vehicleDef != null && model.hasRenderScript()) {
                try {
                    // スクリプトは entity を参照するため、プレビュー用の一時車両エンティティを渡す。
                    // null を渡すと多くのスクリプトが例外を投げてフォールバック(=非適用)になっていた。
                    MqoModelLoader.renderModel(model, poseStack, buffer, LightTexture.FULL_BRIGHT, previewFilter, null, previewEnt);
                    rendered = true;
                } catch (Throwable ignored) {
                    rendered = false;
                }
            }
            if (!rendered) {
                MqoModelLoader.renderModelWithoutScript(
                    model, poseStack, buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    false, previewFilter, null);
                MqoModelLoader.renderModelWithoutScript(
                    model, poseStack, buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    true, previewFilter, null);
            }

            if (vehicleDef != null) {
                boolean selfDrawsRunningGear = model.hasOwnWheelGroups();
                for (int i = 0; i < vehicleDef.getBogies().size(); i++) {
                    VehicleDefinition.BogieDefinition bogieDef = vehicleDef.getBogies().get(i);
                    if (shouldSkipPreviewBogie(selfDrawsRunningGear, bogieDef)) {
                        continue;
                    }
                    try {
                        BogieRenderer.renderBogie(
                            poseStack, i, bogieDef, vehicleDef,
                            null, buffer, LightTexture.FULL_BRIGHT, 0.0F, 1.0F);
                    } catch (Throwable ignored) {
                        // モデル選択画面では1つの台車失敗で車体プレビューまで消さない。
                    }
                }
            }
        } finally {
            poseStack.popPose();
        }
    }

    private static boolean shouldSkipPreviewBogie(boolean selfDrawsRunningGear, VehicleDefinition.BogieDefinition bogieDef) {
        if (bogieDef == null || bogieDef.modelFile() == null || bogieDef.modelFile().isBlank()) {
            return true;
        }
        if (BogieRenderer.isDummyBogieModel(bogieDef.modelFile())) {
            return true;
        }
        return selfDrawsRunningGear && bogieDef.modelFile().toLowerCase(Locale.ROOT).endsWith(".class");
    }

    private static boolean shouldRenderPreviewGroup(String groupName) {
        if (groupName == null || groupName.isBlank()) {
            return true;
        }
        String lower = groupName.trim().toLowerCase(Locale.ROOT);
        return !lower.equals("shadow")
            && !lower.equals("_shadow")
            && !lower.equals("shadowplane")
            && !lower.equals("hitbox")
            && !lower.equals("collision")
            && !lower.equals("collider");
    }

    private MqoModelLoader.MqoModel getOrLoadModel(String id, String packName) {
        if (id == null || id.isBlank() || MISSING_MODEL_CACHE.contains(id)) return null;
        if (MODEL_CACHE.containsKey(id)) return MODEL_CACHE.get(id);
        MqoModelLoader.MqoModel model = null;
        try {
            // 1. 車両
            var vd = VehicleRegistry.getById(id);
            if (vd != null && vd.getModelFile() != null && !vd.getModelFile().isBlank()) {
                model = MqoModelLoader.loadModelForVehicle(vd);
            }
            if (model == null) {
                // 2. 設置物
                var iod = InstalledObjectRegistry.getById(id);
                if (iod != null && iod.getModelFile() != null && !iod.getModelFile().isBlank()) {
                    model = MqoModelLoader.loadModelFromPack(
                        iod.getPackName(), iod.getModelFile(), iod.getTextureOverrides(), null, iod.isSmoothing());
                }
            }
            if (model == null) {
                // 3. レール
                var rd = RailRegistry.getById(id);
                if (rd != null && rd.getModelFile() != null && !rd.getModelFile().isBlank()) {
                    model = MqoModelLoader.loadModelFromPack(
                        rd.getPackName(), rd.getModelFile(), rd.getTextureOverrides(), null, false);
                }
            }
        } catch (Exception ignored) {}
        if (model != null) {
            MODEL_CACHE.put(id, model);
        } else {
            MISSING_MODEL_CACHE.add(id);
        }
        return model;
    }

    /**
     * プレビューでスクリプトを適用するための、ワールド未追加の一時車両エンティティを返す。
     * 車両IDごとに1つキャッシュする。スクリプトは entity の状態(既定値)を参照して本体や
     * ドア・ライト等を本来の配置で描く。生成に失敗したら null(=スクリプト無し描画にフォールバック)。
     */
    private com.portofino.realtrainmodunofficial.entity.TrainEntity getOrCreatePreviewEntity(
            VehicleDefinition def, MqoModelLoader.MqoModel model) {
        if (def == null || def.getId() == null) {
            return null;
        }
        if (previewEntity != null && def.getId().equals(previewEntityId)) {
            return previewEntity;
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return null;
            }
            com.portofino.realtrainmodunofficial.entity.TrainEntity e =
                com.portofino.realtrainmodunofficial.entity.TrainEntity.create(
                    mc.level, def.getId(), 0.0D, 0.0D, 0.0D, 0.0F, def.getTrainDistance());
            if (e == null) {
                return null;
            }
            if (model.getScriptEngine() != null) {
                e.setScriptEngine(model.getScriptEngine());
            }
            previewEntity = e;
            previewEntityId = def.getId();
            return e;
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private class ModelList extends ObjectSelectionList<ModelList.ModelEntry> {
        ModelList(Minecraft mc, int width, int height, int top, int itemHeight) {
            super(mc, width, height, top, itemHeight);
            setRenderHeader(false, 0);
            String lastCat = null;
            ModelEntry initialEntry = null;
            int initialRow = -1, row = 0;
            for (ModelInfo info : models) {
                String cat = info.category();
                if (cat != null && !cat.isBlank() && !cat.equals(lastCat)) {
                    addEntry(new ModelEntry(cat));
                    lastCat = cat;
                    row++;
                }
                ModelEntry entry = new ModelEntry(info.id(), info.displayName(), info.packName(), info.buttonTexture());
                addEntry(entry);
                if (initialSelectedId != null && initialSelectedId.equals(info.id())) {
                    initialEntry = entry;
                    initialRow = row;
                }
                row++;
            }
            if (initialEntry == null) {
                for (ModelEntry e : children()) {
                    if (!e.header) { initialEntry = e; initialRow = 0; break; }
                }
            }
            if (initialEntry != null) {
                setSelected(initialEntry);
                selectedId = initialEntry.id;
                setScrollAmount(Math.max(0.0, initialRow * (double) itemHeight - height * 0.5));
            }
        }

        @Override
        protected int getScrollbarPosition() { return width - 6; }

        @Override
        public int getRowWidth() { return width - 8; }

        // 選択ハイライト(青い枠)は描画しない
        @Override
        protected void renderSelection(GuiGraphics graphics, int top, int width, int height, int color, int color2) {}

        class ModelEntry extends ObjectSelectionList.Entry<ModelEntry> {
            public final String id;
            public final String packName;
            private final Component label;
            private final PackButtonTextureCache.ButtonTextureInfo buttonTex;
            public final boolean header;

            ModelEntry(String category) {
                this.id = ""; this.packName = "";
                this.label = Component.literal(category);
                this.buttonTex = null; this.header = true;
            }

            ModelEntry(String id, String displayName, String packName, String buttonTexturePath) {
                this.id = id; this.packName = packName;
                this.label = Component.literal(safe(displayName).isBlank() ? id : displayName);
                this.buttonTex = PackButtonTextureCache.get(packName, buttonTexturePath);
                this.header = false;
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int rowWidth,
                               int height, int mx, int my, boolean hovered, float pt) {
                if (header) {
                    graphics.drawString(ModelSelectScreen.this.font, label, left + 4, top + 11, 0xFFD0D0D0, false);
                    return;
                }
                boolean selected = this == modelList.getSelected();
                if (selected) {
                    // 選択中は薄い白枠だけ (RTM 本家は青いオーバーレイ)
                    graphics.fill(left, top, left + BTN_W, top + BTN_H, 0x44FFFFFF);
                }
                if (buttonTex != null) {
                    // 透明ピクセルがあるテクスチャでもゲーム世界が透けないよう背景を先に塗る
                    graphics.fill(left, top, left + BTN_W, top + BTN_H, 0xFF1A1A2E);
                    // RTM 本家の drawTexturedModalRect は UV を 1/256 固定で計算するため、
                    // 512×512 テクスチャでは「160×32 指定」が実際には 320×64 px を読み取っていた。
                    // MC 1.21 は実テクスチャ寸法を使うので、同じ比率で srcW/srcH を算出する。
                    float rtmScale = Math.max(1f, buttonTex.width() / 256f);
                    int srcW = Math.min(Math.round(160 * rtmScale), buttonTex.width());
                    int srcH = Math.min(Math.round(32  * rtmScale), buttonTex.height());
                    graphics.blit(buttonTex.location(),
                        left, top, BTN_W, BTN_H,
                        0, 0, srcW, srcH,
                        buttonTex.width(), buttonTex.height());
                } else {
                    graphics.drawString(ModelSelectScreen.this.font, label,
                        left + 4, top + (BTN_H - 8) / 2, 0xAAAAAA, false);
                }
            }

            @Override
            public boolean mouseClicked(double mx, double my, int btn) {
                if (header) return false;
                if (btn == 0) {
                    modelList.setSelected(this);
                    selectedId = this.id;
                    return true;
                }
                return false;
            }

            @Override
            public Component getNarration() { return label; }
        }
    }
}
