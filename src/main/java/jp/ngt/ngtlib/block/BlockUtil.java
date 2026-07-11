package jp.ngt.ngtlib.block;

import jp.ngt.mccompat.MovingObjectPosition;
import jp.ngt.mccompat.PlayerCompat;
import jp.ngt.mccompat.WorldCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 本家 jp.ngt.ngtlib.block.BlockUtil のスクリプト互換 (SRB3/NGTO Builder が使用)。
 * world 引数は WorldCompat / Level のどちらでも受ける。
 */
@SuppressWarnings("unused")
public final class BlockUtil {
    private BlockUtil() {
    }

    public static Level toLevel(Object world) {
        if (world instanceof WorldCompat wc) {
            return wc.getLevel();
        }
        if (world instanceof Level l) {
            return l;
        }
        return null;
    }

    public static boolean setBlock(Object world, double x, double y, double z, Object block, int meta, int flag) {
        Level level = toLevel(world);
        if (level == null || !(block instanceof Block b)) {
            return false;
        }
        return level.setBlock(new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z)), b.defaultBlockState(), flag);
    }

    public static Block getBlock(Object world, double x, double y, double z) {
        Level level = toLevel(world);
        if (level == null) {
            return net.minecraft.world.level.block.Blocks.AIR;
        }
        return level.getBlockState(new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z))).getBlock();
    }

    public static int getMetadata(Object world, double x, double y, double z) {
        return 0;
    }

    public static net.minecraft.world.level.block.entity.BlockEntity getTileEntity(Object world, double x, double y, double z) {
        Level level = toLevel(world);
        if (level == null) {
            return null;
        }
        return level.getBlockEntity(new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z)));
    }

    /**
     * 本家 getMOPFromPlayer(EntityPlayer, distance, liquid): 視線レイキャスト。
     * player は PlayerCompat / 実 Player のどちらでも受ける。
     */
    public static MovingObjectPosition getMOPFromPlayer(Object player, double distance, boolean liquid) {
        Player p = PlayerCompat.unwrap(player);
        if (p == null) {
            return null;
        }
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(distance));
        HitResult hit = p.level().clip(new ClipContext(eye, end,
                ClipContext.Block.OUTLINE,
                liquid ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, p));
        return MovingObjectPosition.of(hit);
    }
}
