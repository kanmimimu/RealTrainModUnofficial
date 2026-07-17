package com.portofino.realtrainmodunofficial.network.compat;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.IntFunction;

/**
 * Forge 1.20.1 shim for the {@code ByteBufCodecs} constants used by RTMU payloads.
 * The underlying buffer is always a {@link FriendlyByteBuf} at runtime (Forge's SimpleChannel
 * passes one), so codecs needing MC-specific helpers wrap through {@link #friendly(ByteBuf)}.
 */
public final class ByteBufCodecs {
    private ByteBufCodecs() {
    }

    public static final StreamCodec<ByteBuf, Integer> INT = StreamCodec.of(
            (buffer, value) -> buffer.writeInt(value), ByteBuf::readInt);

    public static final StreamCodec<ByteBuf, Integer> VAR_INT = StreamCodec.of(
            (buffer, value) -> friendly(buffer).writeVarInt(value), buffer -> friendly(buffer).readVarInt());

    public static final StreamCodec<ByteBuf, Float> FLOAT = StreamCodec.of(
            (buffer, value) -> buffer.writeFloat(value), ByteBuf::readFloat);

    public static final StreamCodec<ByteBuf, Double> DOUBLE = StreamCodec.of(
            (buffer, value) -> buffer.writeDouble(value), ByteBuf::readDouble);

    public static final StreamCodec<ByteBuf, Boolean> BOOL = StreamCodec.of(
            (buffer, value) -> buffer.writeBoolean(value), ByteBuf::readBoolean);

    public static final StreamCodec<ByteBuf, String> STRING_UTF8 = StreamCodec.of(
            (buffer, value) -> friendly(buffer).writeUtf(value), buffer -> friendly(buffer).readUtf());

    public static final StreamCodec<ByteBuf, CompoundTag> COMPOUND_TAG = StreamCodec.of(
            (buffer, value) -> friendly(buffer).writeNbt(value), buffer -> friendly(buffer).readNbt());

    public static final StreamCodec<ByteBuf, BlockPos> BLOCK_POS = StreamCodec.of(
            (buffer, value) -> friendly(buffer).writeBlockPos(value), buffer -> friendly(buffer).readBlockPos());

    public static <T, C extends Collection<T>> StreamCodec<ByteBuf, C> collection(
            IntFunction<C> factory, StreamCodec<? super ByteBuf, T> elementCodec) {
        return StreamCodec.of(
                (buffer, collection) -> {
                    friendly(buffer).writeVarInt(collection.size());
                    for (T element : collection) {
                        elementCodec.encode(buffer, element);
                    }
                },
                buffer -> {
                    int size = friendly(buffer).readVarInt();
                    C collection = factory.apply(size);
                    for (int i = 0; i < size; i++) {
                        collection.add(elementCodec.decode(buffer));
                    }
                    return collection;
                });
    }

    public static <T> StreamCodec.CodecOperation<ByteBuf, T, List<T>> list() {
        return elementCodec -> collection(ArrayList::new, elementCodec);
    }

    private static FriendlyByteBuf friendly(ByteBuf buffer) {
        return buffer instanceof FriendlyByteBuf friendly ? friendly : new FriendlyByteBuf(buffer);
    }
}
