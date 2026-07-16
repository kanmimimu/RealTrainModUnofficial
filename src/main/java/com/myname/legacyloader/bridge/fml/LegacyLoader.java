package com.myname.legacyloader.bridge.fml;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModInfo; // 笘・㍾隕・ 縺薙ｌ繧偵う繝ｳ繝昴・繝・

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LegacyLoader {
    private static final LegacyLoader INSTANCE = new LegacyLoader();

    public static LegacyLoader instance() {
        return INSTANCE;
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static boolean func_72363_a(String modId) {
        return isModLoaded(modId);
    }

    public LegacyModMetadata getMinecraftModContainer() {
        LegacyModMetadata metadata = new LegacyModMetadata();
        metadata.modId = "minecraft";
        metadata.name = "Minecraft";
        metadata.version = "1.7.10";
        return metadata;
    }

    // 笘・ｿｮ豁｣: ModList.get().getMods().forEach(...) 縺ｫ螟画峩
    public Map<String, Object> getModObjectList() {
        Map<String, Object> map = new HashMap<>();

        // ModList.get() 縺縺代〒縺ｯ forEach 縺ｧ縺阪∪縺帙ｓ縲・.getMods() 繧貞他縺ｳ縺ｾ縺吶・
        for (IModInfo modInfo : ModList.get().getMods()) {
            // ModID縺ｨ繝繝溘・繧ｪ繝悶ず繧ｧ繧ｯ繝医・繝壹い繧堤匳骭ｲ
            // 繝繝溘・繧ｪ繝悶ず繧ｧ繧ｯ繝医・1.7.10縺ｮModContainer縺ｮ莉｣繧上ｊ縺ｧ縺・
            map.put(modInfo.getModId(), new Object());
        }

        return map;
    }

    // SRG蜷・
    public Map<String, Object> func_72366_b() {
        return getModObjectList();
    }

    public File getConfigDir() {
        return FMLPaths.CONFIGDIR.get().toFile();
    }

    public java.util.List<Object> getActiveModList() {
        return java.util.Collections.emptyList();
    }
    public java.util.List<Object> func_72364_h() { return getActiveModList(); }
}
