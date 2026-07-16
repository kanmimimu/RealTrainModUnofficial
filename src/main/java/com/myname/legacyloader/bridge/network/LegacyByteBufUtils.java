package com.myname.legacyloader.bridge.network;
import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import com.myname.legacyloader.bridge.item.LegacyItemStackHelper;
public class LegacyByteBufUtils {
 public static void writeUTF8String(ByteBuf buf, String s){ byte[] b=(s==null?"":s).getBytes(StandardCharsets.UTF_8); writeVarInt(buf,b.length,5); buf.writeBytes(b);}
 public static String readUTF8String(ByteBuf buf){ int l=readVarInt(buf,5); byte[] b=new byte[Math.max(0,l)]; buf.readBytes(b); return new String(b,StandardCharsets.UTF_8);}
 public static void writeVarInt(ByteBuf buf,int value,int max){ while((value & -128)!=0){buf.writeByte(value & 127 | 128); value >>>=7;} buf.writeByte(value);}
 public static int readVarInt(ByteBuf buf,int max){ int i=0,j=0,b; do{ b=buf.readByte(); i|=(b&127) << j++*7; if(j>max) throw new RuntimeException("VarInt too big"); }while((b&128)==128); return i;}
 public static void writeItemStack(ByteBuf buf, ItemStack stack){ writeVarInt(buf, stack==null?0:stack.getCount(), 5); writeVarInt(buf, LegacyItemStackHelper.func_77960_j(stack), 5);}
 public static ItemStack readItemStack(ByteBuf buf){ readVarInt(buf,5); readVarInt(buf,5); return ItemStack.EMPTY;}
 public static void writeTag(ByteBuf buf, CompoundTag tag){ writeUTF8String(buf, tag==null?"":tag.toString());}
 public static CompoundTag readTag(ByteBuf buf){ readUTF8String(buf); return new CompoundTag();}
}
