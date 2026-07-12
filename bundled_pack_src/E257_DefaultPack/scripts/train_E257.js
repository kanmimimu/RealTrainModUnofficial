//E257系0番台 描画スクリプト (デフォルトパック)
//1つの E257.mqo に全号車のオブジェクトが入っており、号車ごとに表示グループを選択する。
//グループ規則は同梱の編成表/メモに従う (末尾[obj]=グループ空オブジェクト、中身は グループ名_***)。
var renderClass = "jp.ngt.rtm.render.VehiclePartsRenderer";

var PartsClass = Java.type("jp.ngt.rtm.render.Parts");
var RLClass = Java.type("jp.ngt.mccompat.ResourceLocation");

//号車ごとの構成定義 (1/3/4/6/11号車のみ)
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
        mascon: "plain", panta: false,
        seatZ: 5.4, seatRows: 13,
        offsetZ: -0.25
    },
    "E257_03": {
        prefixes: ["ex100_", "ex100b_", "roll_ex100_", "ex1a_", "ex1cp_", "ex1d1_", "ex1p_", "ex1d2_", "ex0end2_",
                   "in1cp100_", "in0cp100_", "in1cpall_", "in1d1_", "in1p_", "in1d2_"],
        exacts: ["sub_seat100", "seat7", "seat8", "seat9", "seat_base100", "box_all100",
                 "under_panel", "under_03", "mark_03", "mark_cp"],
        excludes: ["ex0end2_step11", "in1cp100_door_No_joint"],
        doorsL: ["doorL01a", "doorL01b"], doorsR: ["doorR01a", "doorR01b"], doorDir: -1.0,
        mascon: "b", panta: false,
        seatZ: 5.4, seatRows: 13,
        offsetZ: -0.25
    },
    "E257_04": {
        prefixes: ["ex4_", "ex0end1_", "ex0end2_", "in4p_", "in4d_", "panta_"],
        exacts: ["under_panel", "under_04", "mark_04"],
        excludes: ["ex0end2_step11", "panta_C"],
        doorsL: ["doorL04"], doorsR: ["doorR04"], doorDir: 1.0,
        mascon: "none", panta: true,
        seatZ: 9.0, seatRows: 18,
        offsetZ: 0.0
    },
    "E257_06": {
        prefixes: ["ex4_", "ex0end1_", "ex0end2_", "in4p_", "in4d_", "panta_"],
        exacts: ["under_panel", "under_06", "mark_06"],
        excludes: ["ex0end2_step11", "panta_C"],
        doorsL: ["doorL04"], doorsR: ["doorR04"], doorDir: 1.0,
        mascon: "none", panta: true,
        seatZ: 9.0, seatRows: 18,
        offsetZ: 0.0
    },
    "E257_11": {
        prefixes: ["ex0_", "roll_ex0_", "ex1a_", "ex1cp_", "ex1d1_", "ex11p_", "ex0end2_",
                   "in1cp0_", "in0cp0_", "in1cpall_", "in1d1_", "in11p_"],
        exacts: ["under_panel", "under_11", "mark_11", "mark_cp"],
        excludes: ["ex0end2_step0", "2000"],
        doorsL: ["doorL01a"], doorsR: ["doorR01a"], doorDir: -1.0,
        mascon: "plain", panta: false,
        seatZ: 5.4, seatRows: 16,
        offsetZ: -0.25
    }
};

//パンタグラフ可動部 (memo.txt の pantograph_front 記述、本家 VehicleParts と同形式)
//pos=回転中心、rot=全上昇時の X 軸回転角。モデルは下降状態で格納されている。
var PANTA = {
    objects: ["panta_C1"], pos: [0.0, 2.85, 6.76], rot: -32.42,
    children: [
        {
            objects: ["panta_C2"], pos: [0.0, 3.49, 5.91], rot: 54.25,
            children: [
                { objects: ["panta_C3_Nm", "panta_C3_30", "panta_C3_65"], pos: [0.0, 4.03, 7.075], rot: -21.83, children: [] },
                { objects: ["panta_C5"], pos: [0.0, 3.35, 5.79], rot: -61.18, children: [] }
            ]
        },
        { objects: ["panta_C4"], pos: [0.0, 3.43, 5.945], rot: 56.14, children: [] }
    ]
};

var SEAT_PITCH = 0.96;
var DOOR_MOVE = 0.89;
//転換座席の回転中心 (memo: x±0.8)。init でモデル実座標から確定する。
var seatPivotL = -0.8;
var seatPivotR = 0.8;

var car = null;
var bodyParts = null;
var doorLParts = null;
var doorRParts = null;
var lampLParts = null;   //ドアランプ (開扉中のみ点灯表示)
var lampRParts = null;
var rollParts = null;    //方向幕 (mat3、交互表示の発光オーバーレイ対象)
var seatBaseL = null;    //固定台座 (memo: p_seat_base のみ固定)
var seatBaseR = null;
var seatTopL = null;     //回転部
var seatTopR = null;
//マスコン: レバーサー/ノッチ別の単一オブジェクト Parts
var leverParts = {};
var notchParts = {};
//パンタ可動 Parts (PANTA 構造に対応)
var pantaParts = null;
//方向幕の交互表示テクスチャ
var rollTex1 = new RLClass("minecraft", "textures/mat3_light1.png");
var rollTex2 = new RLClass("minecraft", "textures/mat3_light2.png");

function toParts(list) {
    return renderer.registerParts(new PartsClass(Java.to(list, "java.lang.String[]")));
}

function buildPantaParts(node) {
    return {
        parts: toParts(node.objects),
        pos: node.pos,
        rot: node.rot,
        children: node.children.map(buildPantaParts)
    };
}

//グループの頂点 X 中心 (座席の回転ピボット確定用)
function groupCenterX(model, name) {
    var g = model.getGroupObject(name);
    if (g == null || g.faces.size() == 0) return null;
    var min = 1.0e9;
    var max = -1.0e9;
    for (var i = 0; i < g.faces.size(); i++) {
        var verts = g.faces.get(i).vertices;
        for (var v = 0; v < verts.length; v++) {
            var x = verts[v].x;
            if (x < min) min = x;
            if (x > max) max = x;
        }
    }
    return (min + max) * 0.5;
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
    var rolls = [];
    var groups = modelObj.model.groupObjects;
    for (var i = 0; i < groups.size(); i++) {
        var objName = groups.get(i).name;
        if (objName == null) continue;
        var lower = objName.toLowerCase();
        //空グループ ([obj]) と可動パーツは除外
        if (lower.indexOf("[obj]") >= 0) continue;
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
        //方向幕 (roll_*** / ***_side_roll) は交互発光の対象にも登録 (本体描画にも含める)
        if (lower.indexOf("roll_") == 0 || lower.indexOf("_side_roll") >= 0) rolls.push(objName);
        body.push(objName);
    }
    bodyParts = toParts(body);
    lampLParts = lampL.length > 0 ? toParts(lampL) : null;
    lampRParts = lampR.length > 0 ? toParts(lampR) : null;
    rollParts = rolls.length > 0 ? toParts(rolls) : null;

    doorLParts = toParts(car.doorsL);
    doorRParts = toParts(car.doorsR);
    seatBaseL = toParts(["p_seat_base_L"]);
    seatBaseR = toParts(["p_seat_base_R"]);
    seatTopL = toParts(["p_seat_a_L", "p_seat_arm_L", "p_seat_frame10_L", "p_seat_b_L"]);
    seatTopR = toParts(["p_seat_a_R", "p_seat_arm_R", "p_seat_frame10_R", "p_seat_b_R"]);
    //転換ピボットは実モデルの座席中心 X から確定 (memo: ±0.8 のはずだが左右の符号を推測しない)
    var pl = groupCenterX(modelObj.model, "p_seat_base_L");
    var pr = groupCenterX(modelObj.model, "p_seat_base_R");
    if (pl != null) seatPivotL = pl;
    if (pr != null) seatPivotR = pr;

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

    if (car.panta) {
        pantaParts = buildPantaParts(PANTA);
    }
}

//本家 BasicVehiclePartsRenderer.renderParts と同じ入れ子変換
function renderPantaNode(node, move) {
    GL11.glPushMatrix();
    GL11.glTranslatef(node.pos[0], node.pos[1], node.pos[2]);
    GL11.glRotatef(node.rot * move, 1.0, 0.0, 0.0);
    GL11.glTranslatef(-node.pos[0], -node.pos[1], -node.pos[2]);
    node.parts.render(renderer);
    for (var i = 0; i < node.children.length; i++) {
        renderPantaNode(node.children[i], move);
    }
    GL11.glPopMatrix();
}

function render(entity, pass, partialTicks) {
    if (car == null || bodyParts == null) return;

    GL11.glPushMatrix();
    if (car.offsetZ != 0.0) {
        GL11.glTranslatef(0.0, 0.0, car.offsetZ);
    }

    //車体 (固定部)
    bodyParts.render(renderer);

    //方向幕の交互表示 (mat3_light1/2 を約3秒ごとに切替、発光オーバーレイ)
    if (rollParts != null) {
        var phase = Math.floor(new Date().getTime() / 3000) % 2;
        renderer.bindTexture(phase == 0 ? rollTex1 : rollTex2);
        rollParts.render(renderer);
        renderer.bindTexture(null);
    }

    //ドア (memo: 1/3/11号車は-Z方向、4/6号車は+Z方向、移動量0.89。本家同様 sigmoid 補間)
    var dL = renderer.sigmoid(renderer.getDoorMovementL(entity));
    var dR = renderer.sigmoid(renderer.getDoorMovementR(entity));
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

    //レバーサー (回転方向): 座席の向きにも使う
    var dir = 1;
    var notch = 0;
    if (entity != null) {
        try {
            dir = entity.getTrainStateData(10);
            notch = entity.getNotch();
        } catch (err) {
        }
    }

    //客席 (memo: 基本座席を z=0 に格納、シートピッチ 0.96 で号車ごとに並べる。
    //台座 p_seat_base のみ固定、他は回転中心 x±0.8 で転換 — 後進時に180°)
    var seatYaw = (dir == 2) ? 180.0 : 0.0;
    for (var row = 0; row < car.seatRows; row++) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0, 0.0, car.seatZ - SEAT_PITCH * row);
        seatBaseL.render(renderer);
        seatBaseR.render(renderer);
        if (seatYaw != 0.0) {
            //座席自身の中心 (x=±0.8) を軸に回転: T(pivot)・R・T(-pivot)
            GL11.glPushMatrix();
            GL11.glTranslatef(seatPivotL, 0.0, 0.0);
            GL11.glRotatef(seatYaw, 0.0, 1.0, 0.0);
            GL11.glTranslatef(-seatPivotL, 0.0, 0.0);
            seatTopL.render(renderer);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            GL11.glTranslatef(seatPivotR, 0.0, 0.0);
            GL11.glRotatef(seatYaw, 0.0, 1.0, 0.0);
            GL11.glTranslatef(-seatPivotR, 0.0, 0.0);
            seatTopR.render(renderer);
            GL11.glPopMatrix();
        } else {
            seatTopL.render(renderer);
            seatTopR.render(renderer);
        }
        GL11.glPopMatrix();
    }

    //マスコン (レバーサー: getTrainStateData(10)、ノッチ: getNotch())
    if (car.mascon != "none" && entity != null) {
        var lever = (dir == 0) ? leverParts["n"] : ((dir == 2) ? leverParts["b"] : leverParts["f"]);
        if (lever != null) lever.render(renderer);
        var notchKey = (notch >= 0) ? ("n" + Math.min(notch, 5)) : ("b" + Math.min(-notch - 1, 8));
        var notchP = notchParts[notchKey];
        if (notchP != null) notchP.render(renderer);
    }

    //パンタグラフ可動 (4/6号車、本家 childParts 方式の入れ子回転)
    if (pantaParts != null) {
        var move = renderer.sigmoid(renderer.getPantographMovementFront(entity));
        renderPantaNode(pantaParts, move);
    }

    GL11.glPopMatrix();
}
