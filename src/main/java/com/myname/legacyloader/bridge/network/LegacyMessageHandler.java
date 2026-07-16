package com.myname.legacyloader.bridge.network;

/**
 * 1.7.10縺ｮ IMessageHandler 莠呈鋤繧､繝ｳ繧ｿ繝ｼ繝輔ぉ繝ｼ繧ｹ
 */
public interface LegacyMessageHandler<REQ extends LegacyMessage, REPLY extends LegacyMessage> {

    /**
     * 繝｡繝・そ繝ｼ繧ｸ繧貞・逅・
     */
    REPLY onMessage(REQ message, LegacyMessageContext ctx);
}