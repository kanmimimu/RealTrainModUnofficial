package com.portofino.realtrainmodunofficial.item;

import com.portofino.realtrainmodunofficial.RealTrainModUnofficialBlocks;
import com.portofino.realtrainmodunofficial.RealTrainModUnofficialComponents;
import com.portofino.realtrainmodunofficial.ClientHooks;
import com.portofino.realtrainmodunofficial.installedobject.InstalledObjectCategory;
import com.portofino.realtrainmodunofficial.installedobject.InstalledObjectDefinition;
import com.portofino.realtrainmodunofficial.installedobject.InstalledObjectRegistry;
import com.portofino.realtrainmodunofficial.blockentity.InstalledObjectBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class InstalledObjectItem extends Item implements ModelSelectableItem {
    // 壁(横倒し)設置で上へ持ち上げる量(ブロック単位)。上げ足りない/上げすぎなら数値を調整する。
    private static final double WALL_MOUNT_RAISE = 0.5D;
    // 逆さ(180°)設置で上へ持ち上げる量(ブロック単位)。天井から吊るす高さ調整用。
    private static final double UPSIDE_DOWN_RAISE = 1.0D;

    private final InstalledObjectCategory category;

    public InstalledObjectItem(InstalledObjectCategory category) {
        super(new Properties());
        this.category = category;
    }

    public InstalledObjectCategory getCategory() {
        return category;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        //コネクタはモデル固定 (本家 Input01/Output01) — 選択画面を出さない
        if (category == InstalledObjectCategory.CONNECTOR_INPUT
                || category == InstalledObjectCategory.CONNECTOR_OUTPUT) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        if (level.isClientSide) {
            ClientHooks.openInstalledObjectSelectScreen(player, player.getItemInHand(hand), category);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    /**
     * コネクタのデフォルト定義 (Input01/Output01 優先、無ければ同カテゴリの先頭)
     */
    private static InstalledObjectDefinition findDefaultConnector(InstalledObjectCategory category) {
        String defaultName = category == InstalledObjectCategory.CONNECTOR_INPUT ? "input01" : "output01";
        InstalledObjectDefinition fallback = null;
        for (InstalledObjectDefinition def : InstalledObjectRegistry.getByCategory(category)) {
            if (fallback == null) {
                fallback = def;
            }
            //定義 ID は "category:pack:name" 形式のため末尾名で判定
            String id = def.getId().toLowerCase(java.util.Locale.ROOT);
            if (id.endsWith(":" + defaultName) || id.equals(defaultName)) {
                return def;
            }
        }
        return fallback;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null) {
            return InteractionResult.PASS;
        }
        // コンポーネントが失われても CUSTOM_DATA から復元する(碍子等の選択がワールド再入場で
        // 消える対策)。setSelectedModelData が両方へ書いているのでフォールバックで確実に読める。
        String selectedId = com.portofino.realtrainmodunofficial.compat.LegacyItemStackBridge.getSelectedModelId(stack);
        InstalledObjectDefinition definition = InstalledObjectRegistry.getById(selectedId);
        if (definition == null || definition.getCategory() != category) {
            //コネクタは本家デフォルトモデル (Input01/Output01) 固定 — 選択画面は出さない
            if (category == InstalledObjectCategory.CONNECTOR_INPUT
                    || category == InstalledObjectCategory.CONNECTOR_OUTPUT) {
                definition = findDefaultConnector(category);
                if (definition == null) {
                    if (!level.isClientSide) {
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                                "コネクタのモデル (Input01/Output01) が見つかりません"), true);
                    }
                    return InteractionResult.FAIL;
                }
            } else {
                if (level.isClientSide) {
                    ClientHooks.openInstalledObjectSelectScreen(player, stack, category);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        net.minecraft.core.Direction clickedFace = context.getClickedFace();
        BlockPos placePos = context.getClickedPos().relative(clickedFace);
        BlockState state = level.getBlockState(placePos);
        if (!state.canBeReplaced()) {
            return InteractionResult.FAIL;
        }
        // クリックした面で設置向きを決める(踏切などの設置系共通)。
        //  ・ブロック下面(天井)に付けた → 逆さ(180°)
        //  ・横面(壁)に付けた          → 横倒し(90°)、面から外向き(プレイヤー側)
        //  ・上面/通常                 → プレイヤー向き(縦置き)
        // WIRE は専用描画、SIGNAL は柱への押し込み挙動を維持するため対象外。
        float placeYaw = player.getYRot();
        float placeMountPitch = 0.0F;
        boolean wallMounted = false;
        boolean upsideDown = false;
        //碍子: 本家 ItemInstalledObject 準拠 — クリック面 (meta 0-5) だけを保存し、
        //描画は本家 RenderElectricalWiring と同じ (ブロック中心ピボット+面回転)。
        //持ち上げ/横倒しハックは廃止 (当たり判定に対してモデルがずれる原因だった)。
        boolean honkeFaceMount = category == InstalledObjectCategory.INSULATOR;
        if (!honkeFaceMount
                && category != InstalledObjectCategory.WIRE && category != InstalledObjectCategory.SIGNAL) {
            if (clickedFace == net.minecraft.core.Direction.DOWN) {
                upsideDown = true;
                placeMountPitch = 180.0F;
            } else if (clickedFace.getAxis().isHorizontal()) {
                wallMounted = true;
                placeYaw = clickedFace.getOpposite().toYRot();
                placeMountPitch = 90.0F;
            }
        }
        if (!level.isClientSide) {
            level.setBlock(placePos, RealTrainModUnofficialBlocks.INSTALLED_OBJECT.get().defaultBlockState(), 3);
            if (level.getBlockEntity(placePos) instanceof InstalledObjectBlockEntity blockEntity) {
                blockEntity.setDefinition(definition.getId(), category, placeYaw);
                blockEntity.setMountPitch(placeMountPitch);
                if (honkeFaceMount) {
                    //本家 meta = クリック面 (1.7.10 side と 1.21 Direction.ordinal は同一)
                    blockEntity.setMountFace(clickedFace.ordinal());
                    blockEntity.setRenderOffset(0.0D, 0.0D, 0.0D);
                } else if (category == InstalledObjectCategory.SIGNAL) {
                    // 当たり判定はそのままで、見た目だけ「クリックした柱」の中へ押し込む。
                    // 本家は信号が柱ブロックを置き換えてそこに描くため、横面設置のみ押し込む。
                    // 上面/下面設置で埋め込むとモデルが 1 ブロック下に出るため無効化。
                    if (clickedFace.getAxis().isHorizontal()) {
                        double yawRad = Math.toRadians(player.getYRot());
                        double faceX = context.getClickedFace().getStepX();
                        double faceZ = context.getClickedFace().getStepZ();
                        double facingDot = Math.abs((-Math.sin(yawRad) * faceX) + (Math.cos(yawRad) * faceZ));
                        double embedDepth = facingDot < 0.85D ? 0.905D : 0.92D;
                        blockEntity.setRenderOffset(-faceX * embedDepth, 0.0D, -faceZ * embedDepth);
                    } else {
                        blockEntity.setRenderOffset(0.0D, 0.0D, 0.0D);
                    }
                } else if (upsideDown) {
                    // 逆さ(180°)は反転でモデルが下へ出るので、1ブロック持ち上げて天井から吊るす。
                    blockEntity.setRenderOffset(0.0D, UPSIDE_DOWN_RAISE, 0.0D);
                } else if (wallMounted) {
                    // 横倒し(90°)でモデルが下にずれるので、少し上へ持ち上げる(接続点も一緒に上がる)。
                    blockEntity.setRenderOffset(0.0D, WALL_MOUNT_RAISE, 0.0D);
                } else {
                    blockEntity.setRenderOffset(0.0D, 0.0D, 0.0D);
                }
                level.sendBlockUpdated(placePos, blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        String selectedId = com.portofino.realtrainmodunofficial.compat.LegacyItemStackBridge.getSelectedModelId(stack);
        if (selectedId != null && !selectedId.isBlank()) {
            InstalledObjectDefinition def = InstalledObjectRegistry.getById(selectedId);
            String name = def != null ? def.getDisplayName() : selectedId;
            lines.add(Component.translatable("tooltip.realtrainmodunofficial.model.selected", name).withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(Component.translatable("tooltip.realtrainmodunofficial.model.none").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public List<SelectableModelInfo> getSelectableModels() {
        return InstalledObjectRegistry.getByCategory(category).stream()
            .map(def -> new SelectableModelInfo(def.getId(), def.getDisplayName(), def.getPackName(), def.getButtonTexture()))
            .toList();
    }
}
