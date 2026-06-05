package jp.kaiz.atsassistmod.registry;

import jp.kaiz.atsassistmod.ATSAssistMod;
import jp.kaiz.atsassistmod.block.GroundUnitBlock;
import jp.kaiz.atsassistmod.block.IftttBlock;
import jp.kaiz.atsassistmod.block.StationAnnounceBlock;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ATSAModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ATSAssistMod.MODID);

    public static final DeferredBlock<GroundUnitBlock> GROUND_UNIT =
            BLOCKS.register("groundunit", () -> new GroundUnitBlock());
    public static final DeferredBlock<IftttBlock> IFTTT =
            BLOCKS.register("ifttt", () -> new IftttBlock());
    public static final DeferredBlock<StationAnnounceBlock> STATION_ANNOUNCE =
            BLOCKS.register("station_announce", () -> new StationAnnounceBlock());

    private ATSAModBlocks() {}

    public static void register(net.neoforged.bus.api.IEventBus bus) {
        BLOCKS.register(bus);
    }
}
