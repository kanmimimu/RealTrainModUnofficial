package jp.ngt.mcte.item;

import jp.ngt.ngtlib.block.NGTObject;
import net.minecraft.nbt.CompoundTag;

/**
 * MCTE jp.ngt.mcte.item.ItemMiniature のスクリプト互換 (static API のみ)。
 * 実アイテムは com.portofino...item.MiniatureItem (最低限のブロック範囲キャプチャ)。
 * NGTO Builder は ItemMiniature.getNGTObject(item.func_77978_p()) でミニチュアの
 * 中身 (NGTObject) を取得する。
 */
@SuppressWarnings("unused")
public final class ItemMiniature {
    private ItemMiniature() {
    }

    /**
     * NBT (BlocksData を含む) から NGTObject を復元。
     * ラッパー NBT (jp.ngt.mccompat.nbt.NBTTagCompound) / 実 CompoundTag 両対応。
     */
    public static NGTObject getNGTObject(Object nbtLike) {
        CompoundTag tag = jp.ngt.mccompat.nbt.NBTTagCompound.unwrap(nbtLike);
        if (tag == null) {
            return null;
        }
        if (tag.contains("Miniature")) {
            tag = tag.getCompound("Miniature");
        }
        if (!tag.contains("BlocksData")) {
            return null;
        }
        try {
            return NGTObject.readFromNBT(tag);
        } catch (Exception e) {
            jp.ngt.ngtlib.io.NGTLog.debug("[ItemMiniature] failed to read NGTObject: " + e);
            return null;
        }
    }
}
