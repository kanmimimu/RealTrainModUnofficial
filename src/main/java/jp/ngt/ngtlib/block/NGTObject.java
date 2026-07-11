package jp.ngt.ngtlib.block;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * 本家 jp.ngt.ngtlib.block.NGTObject (MCTE ミニチュアの中身 = ブロックの3D集合) の
 * 最低限実装。NGTO Builder のスクリプトが xSize/ySize/zSize/getBlockSet を使う。
 * ブロックは (x,y,z) → index = x + z*xSize + y*xSize*zSize で平坦化して保持。
 */
public class NGTObject {
    public long objId;
    public List<BlockSet> blockList = new ArrayList<>();
    public int xSize;
    public int ySize;
    public int zSize;
    public int origX;
    public int origY;
    public int origZ;

    private BlockSet[] grid;

    protected NGTObject() {
    }

    public static NGTObject createNGTO(Object blocks, int w, int h, int d, int x, int y, int z) {
        NGTObject obj = new NGTObject();
        obj.objId = jp.ngt.ngtlib.util.NGTUtil.getUniqueId();
        obj.xSize = Math.max(w, 1);
        obj.ySize = Math.max(h, 1);
        obj.zSize = Math.max(d, 1);
        obj.origX = x;
        obj.origY = y;
        obj.origZ = z;
        obj.grid = new BlockSet[obj.xSize * obj.ySize * obj.zSize];
        for (Object o : coerceList(blocks)) {
            if (o instanceof BlockSet set) {
                obj.blockList.add(set);
                obj.put(set);
            }
        }
        return obj;
    }

    private static List<Object> coerceList(Object blocks) {
        List<Object> out = new ArrayList<>();
        if (blocks instanceof List<?> l) {
            out.addAll(l);
        } else if (blocks instanceof Object[] arr) {
            java.util.Collections.addAll(out, arr);
        } else if (blocks != null) {
            out.add(blocks);
        }
        return out;
    }

    private void put(BlockSet set) {
        int x = set.x;
        int y = Math.max(set.y, 0);
        int z = set.z;
        if (x >= 0 && x < xSize && y < ySize && z >= 0 && z < zSize) {
            grid[x + z * xSize + y * xSize * zSize] = set;
        }
    }

    public BlockSet getBlockSet(int x, int y, int z) {
        if (x >= 0 && y >= 0 && z >= 0 && x < xSize && y < ySize && z < zSize) {
            BlockSet set = grid[x + z * xSize + y * xSize * zSize];
            return set != null ? set : BlockSet.AIR;
        }
        return BlockSet.AIR;
    }

    public CompoundTag writeToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("ObjId", this.objId);
        tag.putInt("SizeX", this.xSize);
        tag.putInt("SizeY", this.ySize);
        tag.putInt("SizeZ", this.zSize);
        tag.putInt("OrigX", this.origX);
        tag.putInt("OrigY", this.origY);
        tag.putInt("OrigZ", this.origZ);
        ListTag list = new ListTag();
        for (BlockSet set : this.blockList) {
            list.add(set.writeToNBT());
        }
        tag.put("BlocksData", list);
        return tag;
    }

    public static NGTObject readFromNBT(CompoundTag tag) {
        NGTObject obj = new NGTObject();
        obj.objId = tag.getLong("ObjId");
        obj.xSize = Math.max(tag.getInt("SizeX"), 1);
        obj.ySize = Math.max(tag.getInt("SizeY"), 1);
        obj.zSize = Math.max(tag.getInt("SizeZ"), 1);
        obj.origX = tag.getInt("OrigX");
        obj.origY = tag.getInt("OrigY");
        obj.origZ = tag.getInt("OrigZ");
        obj.grid = new BlockSet[obj.xSize * obj.ySize * obj.zSize];
        net.minecraft.core.HolderGetter<net.minecraft.world.level.block.Block> blocks =
                net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup();
        ListTag list = tag.getList("BlocksData", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            BlockSet set = BlockSet.readFromNBT(list.getCompound(i), blocks);
            obj.blockList.add(set);
            obj.put(set);
        }
        return obj;
    }
}
