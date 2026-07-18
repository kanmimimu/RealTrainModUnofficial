package com.myname.legacyloader.bridge.network;

import io.netty.buffer.ByteBuf;

/**
 * 1.7.10縺ｮ IMessage 莠呈鋤繧､繝ｳ繧ｿ繝ｼ繝輔ぉ繝ｼ繧ｹ
 */
public interface LegacyMessage {

    /**
     * 繝舌ャ繝輔ぃ縺九ｉ繝・・繧ｿ繧定ｪｭ縺ｿ霎ｼ繧
     */
    void fromBytes(ByteBuf buf);

    /**
     * 繝舌ャ繝輔ぃ縺ｫ繝・・繧ｿ繧呈嶌縺崎ｾｼ繧
     */
    void toBytes(ByteBuf buf);
}