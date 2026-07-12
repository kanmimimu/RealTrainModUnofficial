//E257系0番台 描画スクリプト (デフォルトパック)
//1つの E257.mqo に全号車のオブジェクトが入っており、号車ごとに表示グループを選択する。
//グループ規則は同梱の編成表/メモに従う (末尾[obj]=グループ空オブジェクト、中身は グループ名_***)。
var renderClass = "jp.ngt.rtm.render.VehiclePartsRenderer";

var PartsClass = Java.type("jp.ngt.rtm.render.Parts");

//号車ごとの構成定義
//prefixes: この前置詞で始まるオブジェクトを表示
//exacts:   完全一致で表示
//excludes: prefixes に一致しても除外 (0番台でない座席バリエーション等)
var CARS = {
    "E257_01": {
        prefixes: ["ex0_", "roll_ex0_", "ex1a_", "ex1cp_", "ex1d1_", "ex1p_", "ex1d2_", "ex0end2_",
                   "in1cp0_", "in0cp0_", "in1cpall_", "in1d1_", "in1p_", "in1d2_"],
        exacts: ["under_panel", "under_01", "mark_01", "mark_cp"],
        excludes: ["ex0end2_step11", "2000"],
        doorsL: ["doorL01a", "doorL01b"], doorsR: ["doorR01a", "doorR01b"], doorDir: -1.0,
        mascon: "plain",
        seatZ: 5.4, seatRows: 13,
        offsetZ: -0.25
    },
    "E257_02": {
        prefixes: ["ex4_", "ex0end1_", "ex0end2_", "in4p_", "in4d_"],
        exacts: ["under_panel", "under_02"],
        excludes: ["ex0end2_step11"],
        doorsL: ["doorL04"], doorsR: ["doorR04"], doorDir: 1.0,
        mascon: "none",
        seatZ: 9.0, seatRows: 18,
        offsetZ: 0.0
    },
    "E257_03": {
        prefixes: ["ex100_", "ex100b_", "roll_ex100_", "ex1a_", "ex1cp_", "ex1d1_", "ex1p_", "ex1d2_", "ex0end2_",
                   "in1cp100_", "in0cp100_", "in1cpall_", "in1d1_", "in1p_", "in1d2_"],
        exacts: ["sub_seat100", "seat7", "seat8", "seat9", "seat_base100", "box_all100",
                 "under_panel", "under_03", "mark_03", "mark_cp"],
        excludes: ["ex0end2_step11", "in1cp100_door_No_joint"],
        doorsL: ["doorL01a", "doorL01b"], doorsR: ["doorR01a", "doorR01b"], doorDir: -1.0,
        mascon: "b",
        seatZ: 5.4, seatRows: 13,
        offsetZ: -0.25
    },
    "E257_04": {
        prefixes: ["ex4_", "ex0end1_", "ex0end2_", "in4p_", "in4d_", "panta_"],
        exacts: ["under_panel", "under_04", "mark_04"],
        excludes: ["ex0end2_step11"],
        doorsL: ["doorL04"], doorsR: ["doorR04"], doorDir: 1.0,
        mascon: "none",
        seatZ: 9.0, seatRows: 18,
        offsetZ: 0.0
    },
    "E257_05": {
        prefixes: ["ex4_", "ex0end1_", "ex0end2_", "in4p_", "in4d_"],
        exacts: ["under_panel", "under_05"],
        excludes: ["ex0end2_step11"],
        doorsL: ["doorL04"], doorsR: ["doorR04"], doorDir: 1.0,
        mascon: "none",
        seatZ: 9.0, seatRows: 18,
        offsetZ: 0.0
    },
    "E257_06": {
        prefixes: ["ex4_", "ex0end1_", "ex0end2_", "in4p_", "in4d_", "panta_"],
        exacts: ["under_panel", "under_06", "mark_06"],
        excludes: ["ex0end2_step11"],
        doorsL: ["doorL04"], doorsR: ["doorR04"], doorDir: 1.0,
        mascon: "none",
        seatZ: 9.0, seatRows: 18,
        offsetZ: 0.0
    },
    "E257_07": {
        prefixes: ["ex4_", "ex0end1_", "ex0end2_", "in4p_", "in4d_"],
        exacts: ["under_panel", "under_07"],
        excludes: ["ex0end2_step11"],
        doorsL: ["doorL04"], doorsR: ["doorR04"], doorDir: 1.0,
        mascon: "none",
        seatZ: 9.0, seatRows: 18,
        offsetZ: 0.0
    },
    "E257_08": {
        prefixes: ["ex4_", "ex0end1_", "ex0end2_", "in4p_", "in4d_"],
        exacts: ["under_panel", "under_08"],
        excludes: ["ex0end2_step11"],
        doorsL: ["doorL04"], doorsR: ["doorR04"], doorDir: 1.0,
        mascon: "none",
        seatZ: 9.0, seatRows: 18,
        offsetZ: 0.0
    },
    "E257_09": {
        prefixes: ["ex4_", "ex0end1_", "ex0end2_", "in4p_", "in4d_"],
        exacts: ["under_panel", "under_09"],
        excludes: ["ex0end2_step11"],
        doorsL: ["doorL04"], doorsR: ["doorR04"], doorDir: 1.0,
        mascon: "none",
        seatZ: 9.0, seatRows: 18,
        offsetZ: 0.0
    },
    "E257_10": {
        prefixes: ["ex4_", "ex0end1_", "ex0end2_", "in4p_", "in4d_"],
        exacts: ["under_panel", "under_10"],
        excludes: ["ex0end2_step11"],
        doorsL: ["doorL04"], doorsR: ["doorR04"], doorDir: 1.0,
        mascon: "none",
        seatZ: 9.0, seatRows: 18,
        offsetZ: 0.0
    },
    "E257_11": {
        prefixes: ["ex0_", "roll_ex0_", "ex1a_", "ex1cp_", "ex1d1_", "ex11p_", "ex0end2_",
                   "in1cp0_", "in0cp0_", "in1cpall_", "in1d1_", "in11p_"],
        exacts: ["under_panel", "under_11", "mark_11", "mark_cp"],
        excludes: ["ex0end2_step0", "2000"],
        doorsL: ["doorL01a"], doorsR: ["doorR01a"], doorDir: -1.0,
        mascon: "plain",
        seatZ: 5.4, seatRows: 16,
        offsetZ: -0.25
    }
};

var SEAT_PITCH = 0.96;
var DOOR_MOVE = 0.89;

var car = null;
var bodyParts = null;
var doorLParts = null;
var doorRParts = null;
var lampLParts = null;   //ドアランプ (開扉中のみ点灯表示)
var lampRParts = null;
var seatLParts = null;
var seatRParts = null;
//マスコン: レバーサー/ノッチ別の単一オブジェクト Parts
var leverParts = {};
var notchParts = {};

function toParts(list) {
    return renderer.registerParts(new PartsClass(Java.to(list, "java.lang.String[]")));
}

function init(modelSet, modelObj) {
    var name = renderer.getModelName();
    car = CARS[name];
    if (car == null) {
        //trainName が一致しないとき: 1号車設定で描画 (何も出ないよりまし)
        car = CARS["E257_01"];
    }

    //MQO の全オブジェクト名から号車の表示対象を選ぶ
    var body = [];
    var lampL = [];
    var lampR = [];
    var groups = modelObj.model.groupObjects;
    for (var i = 0; i < groups.size(); i++) {
        var objName = groups.get(i).name;
        if (objName == null) continue;
        var lower = objName.toLowerCase();
        //空グループ ([obj]) と可動パーツは除外
        if (lower.endsWith("[obj]") || lower.endsWith("[obj]*") || lower.endsWith("[obj]100")) continue;
        if (lower.indexOf("door") == 0 || lower.indexOf("p_seat_") == 0) continue;
        if (lower.indexOf("lever_") == 0 || lower.indexOf("notch_") == 0 || lower.indexOf("brake_") == 0) continue;

        var excluded = false;
        for (var e = 0; e < car.excludes.length; e++) {
            if (objName.indexOf(car.excludes[e]) >= 0) { excluded = true; break; }
        }
        if (excluded) continue;

        var hit = false;
        for (var p = 0; p < car.prefixes.length; p++) {
            if (objName.indexOf(car.prefixes[p]) == 0) { hit = true; break; }
        }
        if (!hit) {
            for (var x = 0; x < car.exacts.length; x++) {
                if (objName == car.exacts[x]) { hit = true; break; }
            }
        }
        if (!hit) continue;

        //ドアランプ (***_doorlamp_Lon / Ron) は開扉中のみ表示
        if (lower.indexOf("doorlamp_lon") >= 0) { lampL.push(objName); continue; }
        if (lower.indexOf("doorlamp_ron") >= 0) { lampR.push(objName); continue; }
        body.push(objName);
    }
    bodyParts = toParts(body);
    lampLParts = lampL.length > 0 ? toParts(lampL) : null;
    lampRParts = lampR.length > 0 ? toParts(lampR) : null;

    doorLParts = toParts(car.doorsL);
    doorRParts = toParts(car.doorsR);
    seatLParts = toParts(["p_seat_base_L", "p_seat_a_L", "p_seat_arm_L", "p_seat_frame10_L", "p_seat_b_L"]);
    seatRParts = toParts(["p_seat_base_R", "p_seat_a_R", "p_seat_arm_R", "p_seat_frame10_R", "p_seat_b_R"]);

    //マスコン (運転台のある号車のみ)
    if (car.mascon != "none") {
        var suffix = (car.mascon == "b") ? "100" : "";
        var nsuffix = (car.mascon == "b") ? "b" : "";
        leverParts["f"] = toParts(["lever_f" + suffix]);
        leverParts["n"] = toParts(["lever_n" + suffix]);
        leverParts["b"] = toParts(["lever_b" + suffix]);
        for (var nt = 0; nt <= 5; nt++) {
            notchParts["n" + nt] = toParts(["notch_" + nt + nsuffix]);
        }
        for (var bk = 0; bk <= 8; bk++) {
            notchParts["b" + bk] = toParts(["brake_" + bk + nsuffix]);
        }
    }
}

function render(entity, pass, partialTicks) {
    if (car == null || bodyParts == null) return;

    GL11.glPushMatrix();
    if (car.offsetZ != 0.0) {
        GL11.glTranslatef(0.0, 0.0, car.offsetZ);
    }

    //車体 (固定部)
    bodyParts.render(renderer);

    //ドア (memo: 1/3/11号車は-Z方向、4/6号車は+Z方向、移動量0.89)
    var dL = renderer.getDoorMovementL(entity);
    var dR = renderer.getDoorMovementR(entity);
    if (dL > 0.0) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0, 0.0, car.doorDir * DOOR_MOVE * dL);
        doorLParts.render(renderer);
        GL11.glPopMatrix();
    } else {
        doorLParts.render(renderer);
    }
    if (dR > 0.0) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0, 0.0, car.doorDir * DOOR_MOVE * dR);
        doorRParts.render(renderer);
        GL11.glPopMatrix();
    } else {
        doorRParts.render(renderer);
    }
    //ドアランプ点灯 (開扉中)
    if (dL > 0.0 && lampLParts != null) lampLParts.render(renderer);
    if (dR > 0.0 && lampRParts != null) lampRParts.render(renderer);

    //客席 (memo: 基本座席を z=0 に格納、シートピッチ 0.96 で号車ごとに並べる)
    for (var row = 0; row < car.seatRows; row++) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0, 0.0, car.seatZ - SEAT_PITCH * row);
        seatLParts.render(renderer);
        seatRParts.render(renderer);
        GL11.glPopMatrix();
    }

    //マスコン (レバーサー: getTrainStateData(10)、ノッチ: getNotch())
    if (car.mascon != "none" && entity != null) {
        var dir = 1;
        var notch = 0;
        try {
            dir = entity.getTrainStateData(10);
            notch = entity.getNotch();
        } catch (err) {
        }
        var lever = (dir == 0) ? leverParts["n"] : ((dir == 2) ? leverParts["b"] : leverParts["f"]);
        if (lever != null) lever.render(renderer);
        var notchKey = (notch >= 0) ? ("n" + Math.min(notch, 5)) : ("b" + Math.min(-notch - 1, 8));
        var notchP = notchParts[notchKey];
        if (notchP != null) notchP.render(renderer);
    }

    GL11.glPopMatrix();
}
