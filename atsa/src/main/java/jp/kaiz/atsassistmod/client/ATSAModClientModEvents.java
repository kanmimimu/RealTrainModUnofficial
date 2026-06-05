package jp.kaiz.atsassistmod.client;

import jp.kaiz.atsassistmod.ATSAssistMod;
import jp.kaiz.atsassistmod.client.render.GroundUnitBeamRenderer;
import jp.kaiz.atsassistmod.registry.ATSAModBlockEntities;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** Mod-bus client setup: HUD layer + key mappings. */
@EventBusSubscriber(modid = ATSAssistMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ATSAModClientModEvents {
    private ATSAModClientModEvents() {}

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(ATSAssistMod.MODID, "train_hud"),
                ATSAModHud.INSTANCE);
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ATSAModKeys.EMERGENCY_BRAKE);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ATSAModBlockEntities.GROUND_UNIT.get(), GroundUnitBeamRenderer::new);
    }
}
