package jp.ngt.ngtlib.renderer.model;

import jp.ngt.ngtlib.io.NGTFileLoader;
import jp.ngt.ngtlib.io.NGTLog;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 本家 jp.ngt.ngtlib.renderer.model.ModelLoader のスクリプト互換移植。
 * パック内 .mqo/.mqoz を PolygonModel (グループ/面/頂点グラフ) として読む。
 * 頂点スケールは既存 MqoModelLoader と同じ 0.01 (座標系一致が必須)。
 */
public final class ModelLoader {
    private static final Map<String, PolygonModel> CACHE = new ConcurrentHashMap<>();
    private static final Pattern OBJECT_PATTERN = Pattern.compile("^Object\\s+\"(.+?)\"");
    private static final Pattern V_PATTERN = Pattern.compile("V\\(([^)]*)\\)");
    private static final Pattern M_PATTERN = Pattern.compile("M\\(([^)]*)\\)");
    private static final Pattern UV_PATTERN = Pattern.compile("UV\\(([^)]*)\\)");

    private ModelLoader() {
    }

    public static PolygonModel loadModel(Object resource, VecAccuracy accuracy) {
        return loadModel(resource, accuracy, null);
    }

    /**
     * スクリプト形: ModelLoader.loadModel(resource, VecAccuracy.LOW, [])
     */
    public static PolygonModel loadModel(Object resource, VecAccuracy accuracy, Object options) {
        String path = pathOf(resource);
        if (path == null) {
            return new PolygonModel();
        }
        return CACHE.computeIfAbsent(path.toLowerCase(Locale.ROOT), key -> {
            byte[] bytes = NGTFileLoader.findAsset(path);
            if (bytes == null) {
                //models/ 接頭辞のゆれに対応
                bytes = NGTFileLoader.findAsset("models/" + path);
            }
            if (bytes == null) {
                NGTLog.debug("[ModelLoader] model not found: " + path);
                return new PolygonModel();
            }
            try {
                return parse(bytes, path);
            } catch (Exception e) {
                NGTLog.debug("[ModelLoader] failed to parse " + path + ": " + e);
                return new PolygonModel();
            }
        });
    }

    /**
     * MQO テキストから直接 (VehicleScriptRenderers が車体モデルグラフ生成に使用)。
     */
    public static PolygonModel parse(byte[] bytes, String name) throws IOException {
        String text = extractMqoText(bytes, name);
        PolygonModel model = new PolygonModel();
        if (text == null) {
            return model;
        }

        GroupObject current = null;
        List<float[]> verts = new ArrayList<>();
        int mode = 0;//0:none, 1:vertex, 2:face
        for (String rawLine : text.split("\r?\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            Matcher om = OBJECT_PATTERN.matcher(line);
            if (om.find()) {
                current = new GroupObject(om.group(1));
                model.groupObjects.add(current);
                verts = new ArrayList<>();
                mode = 0;
                continue;
            }
            if (current == null) {
                continue;
            }
            if (line.startsWith("vertex ")) {
                mode = 1;
                continue;
            }
            if (line.startsWith("BVertex")) {
                mode = 3;//バイナリ頂点は未対応
                continue;
            }
            if (line.startsWith("face ")) {
                mode = 2;
                continue;
            }
            if (line.equals("}")) {
                mode = 0;
                continue;
            }
            if (mode == 1) {
                String[] t = line.split("\\s+");
                if (t.length >= 3) {
                    try {
                        verts.add(new float[]{
                                Float.parseFloat(t[0]) * 0.01F,
                                Float.parseFloat(t[1]) * 0.01F,
                                Float.parseFloat(t[2]) * 0.01F});
                    } catch (NumberFormatException ignored) {
                    }
                }
            } else if (mode == 2) {
                parseFaceLine(line, verts, current);
            }
        }
        return model;
    }

    private static void parseFaceLine(String line, List<float[]> verts, GroupObject group) {
        int sp = line.indexOf(' ');
        if (sp <= 0) {
            return;
        }
        int count;
        try {
            count = Integer.parseInt(line.substring(0, sp).trim());
        } catch (NumberFormatException e) {
            return;
        }
        if (count < 3) {
            return;
        }
        Matcher vm = V_PATTERN.matcher(line);
        if (!vm.find()) {
            return;
        }
        String[] vidx = vm.group(1).trim().split("\\s+");
        if (vidx.length < count) {
            return;
        }
        int matId = 0;
        Matcher mm = M_PATTERN.matcher(line);
        if (mm.find()) {
            try {
                matId = Integer.parseInt(mm.group(1).trim());
            } catch (NumberFormatException ignored) {
            }
        }
        float[] uvs = null;
        Matcher um = UV_PATTERN.matcher(line);
        if (um.find()) {
            String[] ut = um.group(1).trim().split("\\s+");
            if (ut.length >= count * 2) {
                uvs = new float[count * 2];
                try {
                    for (int i = 0; i < count * 2; i++) {
                        uvs[i] = Float.parseFloat(ut[i]);
                    }
                } catch (NumberFormatException e) {
                    uvs = null;
                }
            }
        }
        Vertex[] faceVerts = new Vertex[count];
        try {
            for (int i = 0; i < count; i++) {
                float[] v = verts.get(Integer.parseInt(vidx[i]));
                faceVerts[i] = new Vertex(v[0], v[1], v[2]);
            }
        } catch (Exception e) {
            return;
        }
        group.faces.add(new Face(faceVerts, uvs, matId));
    }

    private static String extractMqoText(byte[] bytes, String name) throws IOException {
        //mqoz = zip 内に .mqo
        if (bytes.length > 4 && bytes[0] == 'P' && bytes[1] == 'K') {
            try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    if (entry.getName().toLowerCase(Locale.ROOT).endsWith(".mqo")) {
                        return new String(zip.readAllBytes(), Charset.forName("Shift_JIS"));
                    }
                }
            }
            return null;
        }
        return new String(bytes, Charset.forName("Shift_JIS"));
    }

    private static String pathOf(Object resource) {
        if (resource instanceof jp.ngt.mccompat.ResourceLocation compat) {
            return compat.func_110623_a();
        }
        if (resource instanceof net.minecraft.resources.ResourceLocation rl) {
            return rl.getPath();
        }
        if (resource instanceof String s) {
            return s;
        }
        return null;
    }
}
