package com.myname.legacyloader.bridge.network;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class LegacyPacketBufferHelper {

    public static FriendlyByteBuf writeItem(FriendlyByteBuf buf, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            buf.writeShort(-1);
            return buf;
        }
        try {
            int id = BuiltInRegistries.ITEM.getId(stack.getItem());
            buf.writeShort(id);
            buf.writeByte(stack.getCount());
        } catch (Exception e) {
            buf.writeShort(-1);
        }
        return buf;
    }

    public static ItemStack readItem(FriendlyByteBuf buf) {
        try {
            short itemId = buf.readShort();
            if (itemId == -1) {
                return ItemStack.EMPTY;
            }
            int count = buf.readByte() & 0xFF;
            Item item = BuiltInRegistries.ITEM.byId(itemId);
            if (item == null) return ItemStack.EMPTY;
            return new ItemStack(item, count);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }
}
