package com.portofino.realtrainmodunofficial.network.compat;

import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Forge 1.20.1 shim for Mojang/NeoForge {@code StreamCodec} (introduced in 1.20.5).
 * Only the surface used by RTMU payloads is provided: {@link #of}, {@link #unit} and
 * {@code composite} for 1..6 components.
 */
public interface StreamCodec<B, V> {
    V decode(B buffer);

    void encode(B buffer, V value);

    interface StreamEncoder<B, V> {
        void encode(B buffer, V value);
    }

    interface StreamDecoder<B, V> {
        V decode(B buffer);
    }

    /** Transforms an element codec into a codec for a derived type (e.g. a list). */
    interface CodecOperation<B, S, T> {
        StreamCodec<B, T> apply(StreamCodec<B, S> codec);
    }

    default <T> StreamCodec<B, T> apply(CodecOperation<B, V, T> operation) {
        return operation.apply(this);
    }

    static <B, V> StreamCodec<B, V> of(StreamEncoder<B, V> encoder, StreamDecoder<B, V> decoder) {
        return new StreamCodec<>() {
            @Override
            public V decode(B buffer) {
                return decoder.decode(buffer);
            }

            @Override
            public void encode(B buffer, V value) {
                encoder.encode(buffer, value);
            }
        };
    }

    static <B, V> StreamCodec<B, V> unit(V value) {
        return new StreamCodec<>() {
            @Override
            public V decode(B buffer) {
                return value;
            }

            @Override
            public void encode(B buffer, V ignored) {
            }
        };
    }

    static <B, C, T1> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            Function<T1, C> constructor) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                return constructor.apply(codec1.decode(buffer));
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
            }
        };
    }

    static <B, C, T1, T2> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            BiFunction<T1, T2, C> constructor) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                return constructor.apply(codec1.decode(buffer), codec2.decode(buffer));
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            Function3<T1, T2, T3, C> constructor) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                return constructor.apply(codec1.decode(buffer), codec2.decode(buffer), codec3.decode(buffer));
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            Function4<T1, T2, T3, T4, C> constructor) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                return constructor.apply(codec1.decode(buffer), codec2.decode(buffer),
                        codec3.decode(buffer), codec4.decode(buffer));
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            Function5<T1, T2, T3, T4, T5, C> constructor) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                return constructor.apply(codec1.decode(buffer), codec2.decode(buffer),
                        codec3.decode(buffer), codec4.decode(buffer), codec5.decode(buffer));
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
            Function6<T1, T2, T3, T4, T5, T6, C> constructor) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                return constructor.apply(codec1.decode(buffer), codec2.decode(buffer),
                        codec3.decode(buffer), codec4.decode(buffer), codec5.decode(buffer), codec6.decode(buffer));
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
                codec6.encode(buffer, getter6.apply(value));
            }
        };
    }
}
