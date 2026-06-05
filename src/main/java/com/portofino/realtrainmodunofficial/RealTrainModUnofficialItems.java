package com.portofino.realtrainmodunofficial;

import com.portofino.realtrainmodunofficial.item.CarItem;
import com.portofino.realtrainmodunofficial.item.CrowbarItem;
import com.portofino.realtrainmodunofficial.item.IcCardItem;
import com.portofino.realtrainmodunofficial.item.MarkerItem;
import com.portofino.realtrainmodunofficial.item.RailItem;
import com.portofino.realtrainmodunofficial.item.TrainItem;
import com.portofino.realtrainmodunofficial.item.InstalledObjectItem;
import com.portofino.realtrainmodunofficial.item.TrainVehicleItem;
import com.portofino.realtrainmodunofficial.item.WireItem;
import com.portofino.realtrainmodunofficial.item.WrenchItem;
import com.portofino.realtrainmodunofficial.installedobject.InstalledObjectCategory;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RealTrainModUnofficialItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RealTrainModUnofficial.MODID);

    public static final DeferredItem<InstalledObjectItem> CROSSING_GATE_ITEM = ITEMS.register(
        "crossing_gate", () -> new InstalledObjectItem(InstalledObjectCategory.CROSSING)
    );
    // 以下のアイテムはユーザー要望により削除:
    //   受信機(signal_receiver) / 受信機シグナル値(signal_value_receiver) / 電車検知ブロック(train_detector)
    //   状態ブロック(signal_state) / スクリプトブロック(script_block) / 通信機(signal_communicator)
    // 道床(ballast)アイテムも廃止済み。ブロック自体は残るがアイテム(入手手段)は登録しない。
    public static final DeferredItem<MarkerItem> MARKER_ITEM = ITEMS.register(
        "marker", () -> new MarkerItem(RealTrainModUnofficialBlocks.MARKER.get(), false)
    );
    public static final DeferredItem<MarkerItem> MARKER_DIAGONAL_ITEM = ITEMS.register(
        "marker_diagonal", () -> new MarkerItem(RealTrainModUnofficialBlocks.MARKER.get(), true)
    );
    public static final DeferredItem<MarkerItem> MARKER_SWITCH_ITEM = ITEMS.register(
        "marker_switch", () -> new MarkerItem(RealTrainModUnofficialBlocks.MARKER_SWITCH.get(), false)
    );
    public static final DeferredItem<MarkerItem> MARKER_SWITCH_DIAGONAL_ITEM = ITEMS.register(
        "marker_switch_diagonal", () -> new MarkerItem(RealTrainModUnofficialBlocks.MARKER_SWITCH.get(), true)
    );
    public static final DeferredItem<RailItem> RAIL_ITEM = ITEMS.register(
        "rail", RailItem::new
    );
    public static final DeferredItem<TrainItem> TRAIN_ITEM = ITEMS.register(
        "train", () -> new TrainItem(TrainItem.Category.ELECTRIC)
    );
    // 試験用車両(test_train)はユーザー要望により削除。
    public static final DeferredItem<TrainVehicleItem> TRAIN_VEHICLE_ITEM = ITEMS.register(
        "train_vehicle", TrainVehicleItem::new
    );
    public static final DeferredItem<CarItem> CAR_ITEM = ITEMS.register(
        "car", CarItem::new
    );
    public static final DeferredItem<IcCardItem> IC_CARD_ITEM = ITEMS.register(
        "ic_card", IcCardItem::new
    );
    public static final DeferredItem<CrowbarItem> CROWBAR_ITEM = ITEMS.register(
        "crowbar", CrowbarItem::new
    );
    public static final DeferredItem<WrenchItem> WRENCH_ITEM = ITEMS.register(
        "wrench", WrenchItem::new
    );
    public static final DeferredItem<WireItem> WIRE_ITEM = ITEMS.register(
        "wire", WireItem::new
    );
    // 照明(light): 本家RTM の照明アイテム。外部パックのモデルを使用し、レッドストーン
    // 信号を受けると点灯する(InstalledObjectBlock 側で発光処理)。
    public static final DeferredItem<InstalledObjectItem> LIGHT_ITEM = ITEMS.register(
        "light", () -> new InstalledObjectItem(InstalledObjectCategory.LIGHT)
    );
    public static final DeferredItem<InstalledObjectItem> INSULATOR_ITEM = ITEMS.register(
        "insulator", () -> new InstalledObjectItem(InstalledObjectCategory.INSULATOR)
    );
    public static final DeferredItem<InstalledObjectItem> SIGNAL_ITEM = ITEMS.register(
        "signal", () -> new InstalledObjectItem(InstalledObjectCategory.SIGNAL)
    );
    public static final DeferredItem<InstalledObjectItem> OVERHEAD_LINE_POLE_ITEM = ITEMS.register(
        "overhead_line_pole", () -> new InstalledObjectItem(InstalledObjectCategory.OVERHEAD_LINE_POLE)
    );
    public static final DeferredItem<InstalledObjectItem> TICKET_GATE_ITEM = ITEMS.register(
        "ticket_gate", () -> new InstalledObjectItem(InstalledObjectCategory.TICKET_GATE)
    );
    public static final DeferredItem<InstalledObjectItem> SPEAKER_ITEM = ITEMS.register(
        "speaker", () -> new InstalledObjectItem(InstalledObjectCategory.SPEAKER)
    );
}
