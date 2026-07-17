package com.portofino.realtrainmodunofficial.item;

import jp.ngt.ngtlib.block.BlockSet;
import jp.ngt.ngtlib.block.NGTObject;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * MCTE ミニチュアの最低限実装。
 * 使い方: ①ブロックを右クリック = 始点 ②別のブロックを右クリック = 終点+キャプチャ
 * (範囲のブロックが BlocksData としてアイテムに保存され、NGTO Builder が使える)
 * ③スニーク右クリック = クリア。
 */
public class MiniatureItem extends Item {
    /** キャプチャ範囲の上限 (負荷対策) */
    private static final int MAX_VOLUME = 33 * 33 * 33;

    public MiniatureItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        CompoundTag tag = getTag(stack);

        if (player.isShiftKeyDown()) {
            //クリア
            stack.setTag(null);
            player.displayClientMessage(Component.literal("ミニチュア: 選択をクリアしました"), true);
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        if (!tag.contains("SelStart")) {
            tag.putIntArray("SelStart", new int[]{pos.getX(), pos.getY(), pos.getZ()});
            setTag(stack, tag);
            player.displayClientMessage(Component.literal(
                    "ミニチュア: 始点 " + pos.toShortString() + " (もう一方の角を右クリック)"), true);
            return InteractionResult.SUCCESS;
        }

        int[] s = tag.getIntArray("SelStart");
        BlockPos start = new BlockPos(s[0], s[1], s[2]);
        int minX = Math.min(start.getX(), pos.getX());
        int minY = Math.min(start.getY(), pos.getY());
        int minZ = Math.min(start.getZ(), pos.getZ());
        int maxX = Math.max(start.getX(), pos.getX());
        int maxY = Math.max(start.getY(), pos.getY());
        int maxZ = Math.max(start.getZ(), pos.getZ());
        int w = maxX - minX + 1;
        int h = maxY - minY + 1;
        int d = maxZ - minZ + 1;
        if ((long) w * h * d > MAX_VOLUME) {
            tag.remove("SelStart");
            setTag(stack, tag);
            player.displayClientMessage(Component.literal("ミニチュア: 範囲が大きすぎます (" + w + "x" + h + "x" + d + ")"), true);
            return InteractionResult.SUCCESS;
        }

        //キャプチャ
        List<BlockSet> blocks = new ArrayList<>();
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    BlockPos bp = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(bp);
                    if (state.isAir()) {
                        continue;
                    }
                    CompoundTag beTag = null;
                    var be = level.getBlockEntity(bp);
                    if (be != null) {
                        beTag = be.saveWithoutMetadata(level.registryAccess());
                    }
                    blocks.add(new BlockSet(x - minX, y - minY, z - minZ, state, beTag));
                }
            }
        }
        NGTObject obj = NGTObject.createNGTO(blocks, w, h, d, 0, 0, 0);
        CompoundTag data = obj.writeToNBT();
        //NGTO Builder は item.func_77978_p() 直下の BlocksData を見る
        for (String key : data.getAllKeys()) {
            tag.put(key, data.get(key).copy());
        }
        tag.remove("SelStart");
        setTag(stack, tag);
        player.displayClientMessage(Component.literal(
                "ミニチュア: " + w + "x" + h + "x" + d + " (" + blocks.size() + " ブロック) をキャプチャしました"), true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        CompoundTag tag = getTag(stack);
        if (tag.contains("BlocksData")) {
            lines.add(Component.literal(String.format("%dx%dx%d",
                    tag.getInt("SizeX"), tag.getInt("SizeY"), tag.getInt("SizeZ"))).withStyle(ChatFormatting.GRAY));
        } else if (tag.contains("SelStart")) {
            lines.add(Component.literal("始点選択済み — 終点を右クリック").withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(Component.literal("ブロックを右クリックして範囲を選択").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static CompoundTag getTag(ItemStack stack) {
        CompoundTag data = stack.getTag();
        return data != null ? data.copy() : new CompoundTag();
    }

    private static void setTag(ItemStack stack, CompoundTag tag) {
        stack.setTag(tag);
    }
}
