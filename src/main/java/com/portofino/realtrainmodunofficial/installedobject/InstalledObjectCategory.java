package com.portofino.realtrainmodunofficial.installedobject;

public enum InstalledObjectCategory {
    LIGHT,
    SIGNBOARD,
    INSULATOR,
    OVERHEAD_LINE_POLE,
    WIRE,
    SIGNAL,
    CROSSING,
    TICKET_GATE,
    SPEAKER,
    /**
     * 本家: 列車検知器 (EntityTrainDetector / ModelMachine machineType="Antenna_Receive")。
     * レールの上に置き、真下のレールに列車が乗っているかを見る。
     */
    TRAIN_DETECTOR,
    /**
     * 本家: 入力コネクタ (レッドストーン→配線網)
     */
    CONNECTOR_INPUT,
    /**
     * 本家: 出力コネクタ (配線網→レッドストーン)
     */
    CONNECTOR_OUTPUT
}
