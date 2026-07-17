package com.portofino.realtrainmodunofficial.network;

import com.portofino.realtrainmodunofficial.formation.TrainFormation;
import com.portofino.realtrainmodunofficial.formation.TrainFormationData;
import com.portofino.realtrainmodunofficial.item.TrainVehicleItem;
import com.portofino.realtrainmodunofficial.item.VehicleFormationItem;
import com.portofino.realtrainmodunofficial.network.compat.ByteBufCodecs;
import com.portofino.realtrainmodunofficial.network.compat.CustomPacketPayload;
import com.portofino.realtrainmodunofficial.network.compat.IPayloadContext;
import com.portofino.realtrainmodunofficial.network.compat.StreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record SaveFormationPayload(String name, List<String> vehicleIds, List<Boolean> reversedFlags) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<SaveFormationPayload> TYPE = new CustomPacketPayload.Type(new ResourceLocation("realtrainmodunofficial", "save_formation"));
   public static final StreamCodec<ByteBuf, SaveFormationPayload> STREAM_CODEC;

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handleOnServer(SaveFormationPayload payload, IPayloadContext context) {
      context.enqueueWork(() -> {
         Player player = context.player();
         TrainFormation formation = new TrainFormation();
         formation.setName(payload.name());

         for(int i = 0; i < payload.vehicleIds().size(); ++i) {
            String vehicleId = (String)payload.vehicleIds().get(i);
            if (vehicleId != null && !vehicleId.isBlank()) {
               formation.addVehicle(vehicleId);
            }
         }

         for(InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof VehicleFormationItem || stack.getItem() instanceof TrainVehicleItem) {
               TrainFormationData.setFormation(stack, formation);
               break;
            }
         }

      });
   }

   static {
      STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, SaveFormationPayload::name, ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8), SaveFormationPayload::vehicleIds, ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.BOOL), SaveFormationPayload::reversedFlags, SaveFormationPayload::new);
   }
}
