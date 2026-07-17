package jp.ngt.ngtlib.io;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 本家 jp.ngt.ngtlib.io.NGTFileLoader のスクリプト互換ファサード。
 * パックスクリプトは getInputStream(ResourceLocation) でパック内アセット
 * (scripts/xxx.gif 等) を読む。全パック zip/フォルダを走査して解決する。
 */
public final class NGTFileLoader {
    /**
     * 正規化パス (assets/minecraft/ 以降, 小文字) → コンテナ
     */
    private static Map<String, AssetRef> index;
    private static final Map<String, byte[]> CONTENT_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, net.minecraft.resources.ResourceLocation> TEXTURE_CACHE = new ConcurrentHashMap<>();

    private record AssetRef(Path container, String entryName) {
    }

    private NGTFileLoader() {
    }

    /**
     * スクリプト用: ResourceLocation (mccompat/実物) からパックアセットのストリームを開く。
     */
    public static InputStream getInputStream(Object resource) {
        String path = pathOf(resource);
        if (path == null) {
            return null;
        }
        byte[] bytes = findAsset(path);
        return bytes != null ? new ByteArrayInputStream(bytes) : null;
    }

    public static byte[] findAsset(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String key = normalize(path);
        byte[] cached = CONTENT_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        AssetRef ref = getIndex().get(key);
        if (ref == null) {
            //サフィックス一致 (パス表記ゆれ対策)
            String leaf = key.contains("/") ? key.substring(key.lastIndexOf('/') + 1) : key;
            for (Map.Entry<String, AssetRef> e : getIndex().entrySet()) {
                if (e.getKey().endsWith("/" + key) || e.getKey().equals(leaf)) {
                    ref = e.getValue();
                    break;
                }
            }
        }
        if (ref == null) {
            return null;
        }
        try {
            byte[] bytes;
            if (Files.isDirectory(ref.container)) {
                bytes = Files.readAllBytes(ref.container.resolve(ref.entryName));
            } else {
                try (ZipFile zip = new ZipFile(ref.container.toFile())) {
                    ZipEntry entry = zip.getEntry(ref.entryName);
                    if (entry == null) {
                        return null;
                    }
                    bytes = zip.getInputStream(entry).readAllBytes();
                }
            }
            if (bytes.length < 4 * 1024 * 1024) {
                CONTENT_CACHE.put(key, bytes);
            }
            return bytes;
        } catch (IOException e) {
            NGTLog.debug("[NGTFileLoader] failed to read " + path + ": " + e);
            return null;
        }
    }

    /**
     * パックアセットをテキスト行として読む安定 API (単一引数・オーバーロード無し)。
     * スクリプトの自前 include (eval(append(NGTText.readText(getResource(path)))) ) は
     * 最終的にここへ来る。見つからなければ空リスト。
     * <p>Nashorn の Java オーバーロード解決が不安定なので、あえて 1 引数 String 固定。
     */
    public static List<String> readAssetLines(String path) {
        List<String> lines = new ArrayList<>();
        byte[] bytes = findAsset(path);
        if (bytes == null) {
            return lines;
        }
        String text = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        for (String line : text.split("\n", -1)) {
            lines.add(line);
        }
        return lines;
    }

    /**
     * パック内画像を動的テクスチャとして登録し RL を返す (NGTUtilClient.bindTexture 用)。
     */
    public static net.minecraft.resources.ResourceLocation resolvePackTexture(String path) {
        if (path == null) {
            return null;
        }
        String key = normalize(path);
        net.minecraft.resources.ResourceLocation cached = TEXTURE_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        byte[] bytes = findAsset(key);
        if (bytes == null) {
            return null;
        }
        try {
            var img = com.mojang.blaze3d.platform.NativeImage.read(new ByteArrayInputStream(bytes));
            var rl = net.minecraft.client.Minecraft.getInstance().getTextureManager()
                    .register("rtmu_pack_tex", new net.minecraft.client.renderer.texture.DynamicTexture(img));
            TEXTURE_CACHE.put(key, rl);
            //発光テクスチャ (***_light*.png): 黒地=非発光として加算合成で描くための印
            if (key.toLowerCase(java.util.Locale.ROOT).contains("_light")) {
                LIGHT_OVERLAY_TEXTURES.add(rl);
            }
            return rl;
        } catch (IOException e) {
            return null;
        }
    }

    private static final java.util.Set<net.minecraft.resources.ResourceLocation> LIGHT_OVERLAY_TEXTURES =
            java.util.concurrent.ConcurrentHashMap.newKeySet();

    /**
     * resolvePackTexture で登録した発光系テクスチャ (パスに _light を含む) か。
     * 描画側はこれを加算合成 (黒=寄与なし) + フルブライトで描く。
     */
    public static boolean isLightOverlayTexture(net.minecraft.resources.ResourceLocation rl) {
        return rl != null && LIGHT_OVERLAY_TEXTURES.contains(rl);
    }

    private static String pathOf(Object resource) {
        if (resource instanceof jp.ngt.mccompat.ResourceLocation compat) {
            return compat.func_110623_a();
        }
        if (resource instanceof net.minecraft.resources.ResourceLocation rl) {
            return rl.getPath();
        }
        return resource != null ? resource.toString() : null;
    }

    private static String normalize(String path) {
        String p = path.replace('\\', '/').toLowerCase(Locale.ROOT);
        while (p.startsWith("/")) {
            p = p.substring(1);
        }
        if (p.startsWith("assets/minecraft/")) {
            p = p.substring("assets/minecraft/".length());
        }
        return p;
    }

    private static synchronized Map<String, AssetRef> getIndex() {
        if (index == null) {
            Map<String, AssetRef> map = new ConcurrentHashMap<>();
            for (Path container : collectContainers()) {
                try {
                    if (Files.isDirectory(container)) {
                        try (var stream = Files.walk(container)) {
                            for (Path file : (Iterable<Path>) stream::iterator) {
                                if (Files.isRegularFile(file)) {
                                    String rel = container.relativize(file).toString();
                                    map.putIfAbsent(normalize(rel), new AssetRef(container, rel));
                                }
                            }
                        }
                    } else {
                        try (ZipFile zip = new ZipFile(container.toFile())) {
                            var entries = zip.entries();
                            while (entries.hasMoreElements()) {
                                ZipEntry e = entries.nextElement();
                                if (!e.isDirectory()) {
                                    map.putIfAbsent(normalize(e.getName()), new AssetRef(container, e.getName()));
                                }
                            }
                        }
                    }
                } catch (IOException ignored) {
                }
            }
            index = map;
            NGTLog.debug("[NGTFileLoader] indexed " + map.size() + " pack assets");
        }
        return index;
    }

    private static List<Path> collectContainers() {
        List<Path> out = new ArrayList<>();
        Path gameDir = FMLPaths.GAMEDIR.get();
        addZips(out, gameDir.resolve("mods"));
        addZips(out, com.portofino.realtrainmodunofficial.DefaultAssetsFolder.get());
        Path cfg = gameDir.resolve("config").resolve("realtrainmodunofficial");
        for (String sub : new String[]{"vehicle_packs", "rail_packs", "packs", "nested_pack_cache"}) {
            addZips(out, cfg.resolve(sub));
        }
        return out;
    }

    private static void addZips(List<Path> out, Path dir) {
        if (!Files.isDirectory(dir)) {
            return;
        }
        try (var stream = Files.list(dir)) {
            for (Path p : (Iterable<Path>) stream::iterator) {
                String name = p.getFileName().toString().toLowerCase(Locale.ROOT);
                if (Files.isRegularFile(p) && (name.endsWith(".zip") || name.endsWith(".jar"))) {
                    out.add(p);
                } else if (Files.isDirectory(p)) {
                    out.add(p);
                }
            }
        } catch (IOException ignored) {
        }
    }
}
