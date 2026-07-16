package com.myname.legacyloader.bridge.forge;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class LegacyForgeHooks {

    public static boolean canHarvestBlock(Block block, Player player, int meta) {
        return true;
    }

    public static boolean canHarvestBlock(Block block, Player player) {
        return true;
    }

    public static boolean canToolHarvestBlock(Block block, int meta, ItemStack stack) {
        return true;
    }

    public static boolean canToolHarvestBlock(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, ItemStack stack) {
        return true;
    }

    public static ItemStack getContainerItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
        if (stack.getItem().hasCraftingRemainingItem()) {
            return new ItemStack(stack.getItem().getCraftingRemainingItem());
        }
        return ItemStack.EMPTY;
    }

    public static boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, Player player) {
        return false;
    }

    public static boolean onBlockActivated(net.minecraft.world.level.Level world, int x, int y, int z, Player player, int side, float hitX, float hitY, float hitZ) {
        return false;
    }

    public static int onHoeUse(ItemStack stack, Player player, net.minecraft.world.level.Level world, int x, int y, int z) {
        return 0;
    }

    public static float blockStrength(Block block, Player player, net.minecraft.world.level.Level world, int x, int y, int z) {
        return 1.0f;
    }

    public static int getLightValue(net.minecraft.world.level.BlockGetter world, int x, int y, int z) {
        return 0;
    }
}
