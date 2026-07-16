package com.portofino.realtrainmodunofficial.client.sound;

import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import com.portofino.realtrainmodunofficial.entity.TrainEntity;
import net.minecraft.world.entity.Entity;
import com.portofino.realtrainmodunofficial.vehicle.VehicleDefinition;
import com.portofino.realtrainmodunofficial.vehicle.VehicleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LegacyScriptSoundManager {
    private static final Map<String, LoopingTrainSound> ACTIVE = new ConcurrentHashMap<>();
    private static final Map<UUID, AutoRunningSoundState> AUTO_RUNNING = new ConcurrentHashMap<>();
    private static final Map<String, Long> ONE_SHOT_LAST_PLAY_TICK = new ConcurrentHashMap<>();
    //本家 SoundUpdater 互換: 前回鳴らした一発音がまだ鳴っている間は鳴らし直さないための保持。
    //MugenLib 等のスクリプトは一発音 (コンプレッサ CPActive/CPEnd 等) を毎 tick playSound する。
    //固定間隔デバウンスだけだと 180ms ごとに鳴り直して「てんてんてん」とスタッターするため、
    //まだ再生中 (isActive) の一発音は鳴らし直さないようにする。
    private static final Map<String, SimpleSoundInstance> ONE_SHOT_ACTIVE = new ConcurrentHashMap<>();
    // スピーカー等 playAt の在世界音を位置キーで保持(ブロック破壊時に stopAt で停止するため)。
    private static final Map<String, SimpleSoundInstance> SPEAKER_SOUNDS = new ConcurrentHashMap<>();
    private static final long ONE_SHOT_DEBOUNCE_MS = 180L;

    private LegacyScriptSoundManager() {
    }

    // ---- 列車エンティティの両対応 ----
    //
    // RTMU には列車エンティティが 2 系統ある:
    //   ・com.portofino...entity.TrainEntity          (旧, レガシー)
    //   ・jp.ngt.rtm.entity.train.EntityTrainBase     (本家忠実移植。列車アイテムが出すのはこちら)
    // サウンド一式は旧 TrainEntity 決め打ちで書かれていたため、実際に出る本家側の列車では
    // <b>音が一切鳴らなかった</b>。どちらでも鳴るように、必要な値だけを型に依存せず取り出す。

    /** その Entity が列車か。 */
    public static boolean isTrain(Entity e) {
        return e instanceof TrainEntity || e instanceof jp.ngt.rtm.entity.train.EntityTrainBase;
    }

    /** 車両定義 ID (パックの ModelTrain_*.json の name)。 */
    private static String vehicleIdOf(Entity e) {
        if (e instanceof TrainEntity t) {
            return t.getVehicleId();
        }
        if (e instanceof jp.ngt.rtm.entity.train.EntityTrainBase t) {
            //本家側は getModelName() が定義 ID にあたる
            return t.getModelName();
        }
        return "";
    }

    /** ノッチ (負=ブレーキ, 正=力行)。 */
    private static int notchOf(Entity e) {
        if (e instanceof TrainEntity t) {
            return t.getNotch();
        }
        if (e instanceof jp.ngt.rtm.entity.train.EntityTrainBase t) {
            return t.getNotch();
        }
        return 0;
    }

    /** 速度。 */
    private static float speedOf(Entity e) {
        if (e instanceof TrainEntity t) {
            return t.getSpeed();
        }
        if (e instanceof jp.ngt.rtm.entity.train.EntityTrainBase t) {
            return t.getSpeed();
        }
        return 0.0F;
    }

    public static void play(Entity train, String namespace, String soundName, float volume, float pitch) {
        play(train, namespace, soundName, volume, pitch, true);
    }

    public static void playLegacyId(Entity train, String legacySoundId, float volume, float pitch, boolean looping) {
        playLegacyId(train, legacySoundId, volume, pitch, looping, false);
    }

    public static void playLegacyId(Entity train, String legacySoundId, float volume, float pitch,
                                    boolean looping, boolean bypassOneShotSuppression) {
        if (legacySoundId == null || legacySoundId.isBlank()) {
            return;
        }
        String namespace = "rtm";
        String soundName = legacySoundId;
        int separator = legacySoundId.indexOf(':');
        if (separator >= 0) {
            namespace = legacySoundId.substring(0, separator);
            soundName = legacySoundId.substring(separator + 1);
        }
        play(train, namespace, soundName, volume, pitch, looping, bypassOneShotSuppression);
    }

    public static void play(Entity train, String namespace, String soundName, float volume, float pitch, boolean looping) {
        play(train, namespace, soundName, volume, pitch, looping, false);
    }

    /**
     * @param bypassOneShotSuppression true = 一発音の「再生中は鳴らし直さない」抑制とデバウンスを無視する。
     *        サーバー発の離散イベント音 (マスコンのレバー音・警笛など) 用。スクリプトが毎tick要求する
     *        一発音 (コンプレッサ等) と違い、送られてきた回数だけ鳴ってよい
     *        (連続ノッチ操作でレバー音がガタガタ鳴るのが正: 本家挙動)。
     */
    public static void play(Entity train, String namespace, String soundName, float volume, float pitch,
                            boolean looping, boolean bypassOneShotSuppression) {
        if (train == null || !train.level().isClientSide()) {
            return;
        }
        ResourceLocation soundId = toSoundId(namespace, soundName);
        if (soundId == null) {
            return;
        }
        if (volume <= 0.001F) {
            if (looping) {
                stop(train, soundId);
            }
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getSoundManager() == null) {
            return;
        }
        if (!looping && bypassOneShotSuppression) {
            minecraft.getSoundManager().play(new SimpleSoundInstance(
                soundId,
                SoundSource.NEUTRAL,
                Mth.clamp(volume, 0.0F, 8.0F),
                Mth.clamp(pitch, 0.05F, 4.0F),
                SoundInstance.createUnseededRandom(),
                false,
                0,
                SoundInstance.Attenuation.LINEAR,
                train.getX(),
                train.getY(),
                train.getZ(),
                false
            ));
            return;
        }
        if (!looping) {
            String oneShotKey = key(train.getUUID(), soundId);
            long now = System.currentTimeMillis();
            //サウンドスクリプト tick 内 (begin/endScriptTick で挟まれている) の一発音は
            //エッジトリガー: 「呼ばれ続けている間は最初の 1 回だけ」鳴らす (本家 SoundUpdater 互換)。
            //MugenTrainSoundLib / MhnElecSoundlib 等はコンプレッサ音 (CPActive/CPEnd) を
            //毎 tick playSound し続ける設計で、再生終了のたびに鳴らし直すと短い音が
            //無限リピートする (車両設置直後の「プシュー」が延々鳴る不具合)。
            //呼ばれなくなった tick でラッチが解除され、次に呼ばれた時にまた 1 回鳴る。
            java.util.Set<ResourceLocation> requestedOneShot = REQUESTED_ONE_SHOT.get(train.getUUID());
            if (requestedOneShot != null) {
                requestedOneShot.add(soundId);
                java.util.Set<ResourceLocation> latch = ONE_SHOT_LATCH.computeIfAbsent(
                    train.getUUID(), k -> ConcurrentHashMap.newKeySet());
                if (!latch.add(soundId)) {
                    return; //ラッチ済み = 前 tick から呼ばれ続けている → 再発火しない
                }
            }
            //本家 SoundUpdater と同じ: まだ鳴っている同一の一発音は鳴らし直さない。
            //(毎 tick playSound される一発音が 180ms 間隔で連打され「てんてんてん」と
            // スタッターする不具合の対策。209 系のコンプレッサ音 CPActive/CPEnd 等。)
            SimpleSoundInstance prevInstance = ONE_SHOT_ACTIVE.get(oneShotKey);
            if (prevInstance != null && minecraft.getSoundManager().isActive(prevInstance)) {
                return;
            }
            //キュー反映ラグで isActive が一瞬 false を返す場合の二重再生を防ぐ最小間隔。
            Long lastPlay = ONE_SHOT_LAST_PLAY_TICK.get(oneShotKey);
            if (lastPlay != null && now - lastPlay < ONE_SHOT_DEBOUNCE_MS) {
                return;
            }
            ONE_SHOT_LAST_PLAY_TICK.put(oneShotKey, now);
            SimpleSoundInstance instance = new SimpleSoundInstance(
                soundId,
                SoundSource.NEUTRAL,
                Mth.clamp(volume, 0.0F, 8.0F),
                Mth.clamp(pitch, 0.05F, 4.0F),
                SoundInstance.createUnseededRandom(),
                false,
                0,
                SoundInstance.Attenuation.LINEAR,
                train.getX(),
                train.getY(),
                train.getZ(),
                false
            );
            ONE_SHOT_ACTIVE.put(oneShotKey, instance);
            minecraft.getSoundManager().play(instance);
            return;
        }
        String key = key(train.getUUID(), soundId);
        LoopingTrainSound sound = ACTIVE.get(key);
        if (sound == null || sound.isStopped()) {
            if (sound != null) {
                ACTIVE.remove(key, sound);
            }
            sound = new LoopingTrainSound(train, soundId);
            sound.update(volume, pitch);
            ACTIVE.put(key, sound);
            minecraft.getSoundManager().play(sound);
        } else {
            sound.update(volume, pitch);
        }
        //本家 SoundUpdater 互換: 今 tick この列車で鳴らしたループ音を記録しておき、
        //endScriptTick で「今 tick 鳴らされなかったループ音」を止める (= 無限ループ防止)。
        //サウンドスクリプト tick 内で開始/更新されたループだけを管理対象にする
        //(描画スクリプト起点のループ音を誤って止めないため)。
        java.util.Set<ResourceLocation> requested = REQUESTED_LOOP.get(train.getUUID());
        if (requested != null) {
            requested.add(soundId);
            MANAGED_LOOP_KEYS.add(key);
        }
    }

    //--- 本家 SoundUpdater の「呼ばれなかったループ音を止める」機構 -----------------------
    //RTM のサウンドスクリプトは、鳴らし続けたいループ音を毎 tick playSound し、鳴り止めたい音は
    //「呼ばない」ことで止める設計 (本家 SoundUpdater が未更新のループ音を停止する)。RTMU には
    //この自動停止が無く、明示 stopSound しないスクリプト (パトカー等) のループ音が無限に鳴り続けた。
    //サウンドスクリプト onUpdate を beginScriptTick / endScriptTick で挟むことで本家挙動を再現する。
    private static final Map<java.util.UUID, java.util.Set<ResourceLocation>> REQUESTED_LOOP =
        new ConcurrentHashMap<>();
    /** 今 tick に要求された一発音 (エッジトリガー判定用)。 */
    private static final Map<java.util.UUID, java.util.Set<ResourceLocation>> REQUESTED_ONE_SHOT =
        new ConcurrentHashMap<>();
    /** 呼ばれ続けている一発音のラッチ。呼ばれなくなった tick で解除される。 */
    private static final Map<java.util.UUID, java.util.Set<ResourceLocation>> ONE_SHOT_LATCH =
        new ConcurrentHashMap<>();
    /** サウンドスクリプト tick 内で開始されたループ音のキー (endScriptTick の自動停止対象)。 */
    private static final java.util.Set<String> MANAGED_LOOP_KEYS = ConcurrentHashMap.newKeySet();

    /** サウンドスクリプト onUpdate の直前に呼ぶ。今 tick の playSound 記録を開始する。 */
    public static void beginScriptTick(Entity train) {
        if (train == null) {
            return;
        }
        REQUESTED_LOOP.put(train.getUUID(), ConcurrentHashMap.newKeySet());
        REQUESTED_ONE_SHOT.put(train.getUUID(), ConcurrentHashMap.newKeySet());
    }

    /** サウンドスクリプト onUpdate の直後に呼ぶ。今 tick 鳴らされなかったループ音を止める。 */
    public static void endScriptTick(Entity train) {
        if (train == null) {
            return;
        }
        java.util.Set<ResourceLocation> requested = REQUESTED_LOOP.remove(train.getUUID());
        //一発音のラッチ整理: 今 tick 呼ばれなかった音はラッチ解除 (次に呼ばれたら 1 回鳴る)
        java.util.Set<ResourceLocation> requestedOneShot = REQUESTED_ONE_SHOT.remove(train.getUUID());
        java.util.Set<ResourceLocation> latch = ONE_SHOT_LATCH.get(train.getUUID());
        if (latch != null) {
            if (requestedOneShot == null || requestedOneShot.isEmpty()) {
                ONE_SHOT_LATCH.remove(train.getUUID());
            } else {
                latch.retainAll(requestedOneShot);
            }
        }
        if (requested == null) {
            return;
        }
        String prefix = train.getUUID() + "|";
        ACTIVE.entrySet().removeIf(entry -> {
            if (!entry.getKey().startsWith(prefix)) {
                return false;
            }
            //サウンドスクリプト起点のループだけ自動停止 (描画スクリプトのループは対象外)
            if (!MANAGED_LOOP_KEYS.contains(entry.getKey())) {
                return false;
            }
            if (requested.contains(entry.getValue().getLocation())) {
                return false;
            }
            entry.getValue().requestStop();
            MANAGED_LOOP_KEYS.remove(entry.getKey());
            return true;
        });
    }

    /**
     * 任意のワールド座標で 1 回サウンドを鳴らす（スピーカー用）。
     * soundIdStr は "namespace:path" 形式のサウンドイベントID。
     * volume を上げると可聴範囲が広がる（MC の LINEAR 減衰は概ね volume×16 ブロック）。
     */
    public static void playAt(double x, double y, double z, String soundIdStr, float volume, float pitch) {
        if (soundIdStr == null || soundIdStr.isBlank()) {
            return;
        }
        ResourceLocation soundId = ResourceLocation.tryParse(soundIdStr.trim().toLowerCase(java.util.Locale.ROOT));
        if (soundId == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getSoundManager() == null) {
            return;
        }
        SimpleSoundInstance instance = new SimpleSoundInstance(
            soundId,
            SoundSource.RECORDS,
            Mth.clamp(volume, 0.0F, 16.0F),
            Mth.clamp(pitch, 0.05F, 4.0F),
            SoundInstance.createUnseededRandom(),
            false,
            0,
            SoundInstance.Attenuation.LINEAR,
            x,
            y,
            z,
            false
        );
        // 位置キーで保持し、ブロック破壊時に stopAt() で止められるようにする
        // (スピーカーの長い音がブロックを壊しても鳴り続ける問題の対策)。
        String key = posKey(x, y, z);
        SimpleSoundInstance prev = SPEAKER_SOUNDS.put(key, instance);
        if (prev != null) {
            minecraft.getSoundManager().stop(prev);
        }
        minecraft.getSoundManager().play(instance);
    }

    /** 位置キー(整数ブロック座標)。同一ブロックの再生を1つに保つ。 */
    private static String posKey(double x, double y, double z) {
        return (int) Math.floor(x) + "," + (int) Math.floor(y) + "," + (int) Math.floor(z);
    }

    /** 指定位置(ブロック)で playAt した音を停止する。スピーカーブロック破壊時に呼ぶ。 */
    public static void stopAt(double x, double y, double z) {
        SimpleSoundInstance s = SPEAKER_SOUNDS.remove(posKey(x, y, z));
        if (s != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getSoundManager() != null) {
                mc.getSoundManager().stop(s);
            }
        }
    }

    public static void tickJsonRunningSound(Entity train) {
        if (train == null || !train.level().isClientSide()) {
            return;
        }
        VehicleDefinition definition = VehicleRegistry.getById(vehicleIdOf(train));
        if (definition == null || definition.hasSoundScript() || !definition.hasJsonRunningSounds()) {
            stopAutoRunningSound(train);
            return;
        }

        AutoRunningSoundState state = AUTO_RUNNING.computeIfAbsent(train.getUUID(), ignored -> new AutoRunningSoundState());
        float speed = Math.abs(speedOf(train));
        boolean moving = speed > 0.0025F;
        boolean powering = notchOf(train) > 0;
        boolean accelerating = powering || speed > state.previousSpeed + 0.0005F;
        String sound = selectJsonRunningSound(definition, train, speed, moving, accelerating);
        state.previousSpeed = speed;

        if (sound == null || sound.isBlank()) {
            stopAutoRunningSound(train);
            return;
        }
        ResourceLocation soundId = toSoundIdFromLegacyString(sound);
        if (soundId == null) {
            stopAutoRunningSound(train);
            return;
        }
        if (state.currentSoundId != null && !state.currentSoundId.equals(soundId)) {
            stop(train, state.currentSoundId);
        }
        state.currentSoundId = soundId;

        float volume = moving ? Mth.clamp(0.45F + speed * 7.5F, 0.35F, 1.35F) : 0.55F;
        float pitch = shouldPitchJsonRunningSound(definition, speed)
            ? Mth.clamp(0.65F + speed * 5.0F, 0.65F, 1.75F)
            : 1.0F;
        play(train, soundId.getNamespace(), soundId.getPath(), volume, pitch, true);
    }

    private static String selectJsonRunningSound(VehicleDefinition definition, Entity train,
                                                 float speed, boolean moving, boolean accelerating) {
        if (!moving) {
            return definition.getSoundStop();
        }
        float startSpeed = getFirstConfiguredMaxSpeed(definition);
        if (speed < startSpeed) {
            return accelerating
                ? firstNonBlank(definition.getSoundStartAcceleration(), definition.getSoundAcceleration())
                : firstNonBlank(definition.getSoundDecelerationStop(), definition.getSoundDeceleration(), definition.getSoundStop());
        }
        return accelerating
            ? firstNonBlank(definition.getSoundAcceleration(), definition.getSoundStartAcceleration())
            : firstNonBlank(definition.getSoundDeceleration(), definition.getSoundDecelerationStop(), definition.getSoundStop());
    }

    private static boolean shouldPitchJsonRunningSound(VehicleDefinition definition, float speed) {
        float startSpeed = getFirstConfiguredMaxSpeed(definition);
        return speed >= startSpeed;
    }

    private static float getFirstConfiguredMaxSpeed(VehicleDefinition definition) {
        if (definition == null || definition.getNotchMaxSpeeds().isEmpty()) {
            return 0.06F;
        }
        for (Float speed : definition.getNotchMaxSpeeds()) {
            if (speed != null && speed > 0.0F) {
                return Math.max(0.005F, speed / 72.0F);
            }
        }
        return 0.06F;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    public static void stop(Entity train, String namespace, String soundName) {
        if (train == null) {
            return;
        }
        ResourceLocation soundId = toSoundId(namespace, soundName);
        if (soundId == null) {
            return;
        }
        String key = key(train.getUUID(), soundId);
        LoopingTrainSound sound = ACTIVE.remove(key);
        if (sound != null) {
            sound.requestStop();
        }
        MANAGED_LOOP_KEYS.remove(key);
        //明示 stopSound された一発音はラッチ解除 (次の playSound でまた 1 回鳴らせる)
        java.util.Set<ResourceLocation> latch = ONE_SHOT_LATCH.get(train.getUUID());
        if (latch != null) {
            latch.remove(soundId);
        }
    }

    private static void stop(Entity train, ResourceLocation soundId) {
        if (train == null || soundId == null) {
            return;
        }
        String key = key(train.getUUID(), soundId);
        LoopingTrainSound sound = ACTIVE.remove(key);
        if (sound != null) {
            sound.requestStop();
        }
        MANAGED_LOOP_KEYS.remove(key);
    }

    /** その列車が鳴らしているループ音を全部止める (本家 SoundUpdater.stopAllSounds 相当)。 */
    public static void stopAll(Entity train) {
        if (train == null) {
            return;
        }
        String prefix = train.getUUID() + "|";
        ACTIVE.entrySet().removeIf(entry -> {
            if (!entry.getKey().startsWith(prefix)) {
                return false;
            }
            entry.getValue().requestStop();
            MANAGED_LOOP_KEYS.remove(entry.getKey());
            return true;
        });
        ONE_SHOT_LATCH.remove(train.getUUID());
        stopAutoRunningSound(train);
    }

    public static void stopAutoRunningSound(Entity train) {
        if (train == null) {
            return;
        }
        AutoRunningSoundState state = AUTO_RUNNING.remove(train.getUUID());
        if (state != null && state.currentSoundId != null) {
            stop(train, state.currentSoundId);
        }
    }

    private static long lastLeverClickMs = 0L;
    private static final long LEVER_CLICK_DEBOUNCE_MS = 70L;

    public static void playLeverClick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getSoundManager() == null) {
            return;
        }
        // 何らかの経路でノッチ操作が毎tick/毎フレーム発火すると、レバー音が「だだだだ」と高速連続する。
        // 最短間隔(70ms)のデバウンスで連続スパムを抑える(1段ずつの操作は普通に鳴る)。
        long now = System.currentTimeMillis();
        if (now - lastLeverClickMs < LEVER_CLICK_DEBOUNCE_MS) {
            return;
        }
        lastLeverClickMs = now;
        ResourceLocation soundId = ResourceLocation.fromNamespaceAndPath("rtm", "train.lever");
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvent.createVariableRangeEvent(soundId), 1.0F, 0.55F));
    }

    private static String key(UUID trainId, ResourceLocation soundId) {
        return trainId + "|" + soundId;
    }

    private static ResourceLocation toSoundId(String namespace, String soundName) {
        if (soundName == null || soundName.isBlank()) {
            return null;
        }
        //生成側 (ExternalSoundPackBridge) と同じ規則で空白・大文字を安全化してから ResourceLocation 化する。
        String resolvedNamespace = namespace == null || namespace.isBlank() ? "minecraft" : ExternalSoundPackBridge.sanitizeSoundPath(namespace);
        String resolvedPath = ExternalSoundPackBridge.sanitizeSoundPath(soundName.trim().replace('\\', '/'));
        if (resolvedPath.startsWith("sounds/")) {
            resolvedPath = resolvedPath.substring("sounds/".length());
        }
        if (resolvedPath.endsWith(".ogg")) {
            resolvedPath = resolvedPath.substring(0, resolvedPath.length() - ".ogg".length());
        }
        if (resolvedNamespace.equals("rtm") && resolvedPath.indexOf('/') >= 0) {
            resolvedPath = resolvedPath.replace('/', '.');
        }
        try {
            return ResourceLocation.fromNamespaceAndPath(resolvedNamespace, resolvedPath);
        } catch (Exception e) {
            RealTrainModUnofficial.LOGGER.warn("Invalid legacy sound id {}:{}", resolvedNamespace, soundName);
            return null;
        }
    }

    private static ResourceLocation toSoundIdFromLegacyString(String legacySoundId) {
        if (legacySoundId == null || legacySoundId.isBlank()) {
            return null;
        }
        String namespace = "rtm";
        String soundName = legacySoundId;
        int separator = legacySoundId.indexOf(':');
        if (separator >= 0) {
            namespace = legacySoundId.substring(0, separator);
            soundName = legacySoundId.substring(separator + 1);
        }
        return toSoundId(namespace, soundName);
    }

    private static final class AutoRunningSoundState {
        private ResourceLocation currentSoundId;
        private float previousSpeed;
    }

    private static final class LoopingTrainSound extends AbstractTickableSoundInstance {
        private final Entity train;

        private LoopingTrainSound(Entity train, ResourceLocation soundId) {
            super(SoundEvent.createVariableRangeEvent(soundId), SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
            this.train = train;
            this.looping = true;
            this.delay = 0;
            this.volume = 0.0F;
            this.pitch = 1.0F;
            this.relative = false;
            this.x = train.getX();
            this.y = train.getY();
            this.z = train.getZ();
        }

        private void update(float volume, float pitch) {
            this.volume = Mth.clamp(volume, 0.0F, 8.0F);
            this.pitch = Mth.clamp(pitch, 0.05F, 4.0F);
            this.x = train.getX();
            this.y = train.getY();
            this.z = train.getZ();
        }

        private void requestStop() {
            stop();
        }

        @Override
        public void tick() {
            if (!train.isAlive()) {
                ACTIVE.remove(key(train.getUUID(), this.getLocation()), this);
                AUTO_RUNNING.remove(train.getUUID());
                stop();
                return;
            }
            this.x = train.getX();
            this.y = train.getY();
            this.z = train.getZ();
        }
    }
}
