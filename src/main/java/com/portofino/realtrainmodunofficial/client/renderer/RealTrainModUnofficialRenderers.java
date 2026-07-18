package com.portofino.realtrainmodunofficial.client.renderer;

import com.portofino.realtrainmodunofficial.registry.RealTrainModUnofficialEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;

public class RealTrainModUnofficialRenderers {
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(RealTrainModUnofficialEntities.CAR.get(), CarRenderer::new);
    }
}
