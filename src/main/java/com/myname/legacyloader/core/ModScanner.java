package com.myname.legacyloader.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ModScanner {
    public static class ModInfo {
        public final String mainClass;
        public final String modId;
        public ModInfo(String mainClass, String modId) {
            this.mainClass = mainClass;
            this.modId = modId;
        }
    }

    public static ModInfo scanForModInfo(URL jarUrl) {
        List<ModInfo> infos = scanForModInfos(jarUrl);
        return infos.isEmpty() ? null : infos.get(0);
    }

    public static List<ModInfo> scanForModInfos(URL jarUrl) {
        List<ModInfo> mods = new ArrayList<>();
        try (InputStream is = jarUrl.openStream();
             JarInputStream jis = new JarInputStream(is)) {

            JarEntry entry;
            while ((entry = jis.getNextJarEntry()) != null) {
                if (entry.getName().endsWith(".class")) {
                    try {
                        ClassReader reader = new ClassReader(jis);
                        ClassNode node = new ClassNode();
                        reader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);

                        if (node.visibleAnnotations != null) {
                            for (AnnotationNode annotation : node.visibleAnnotations) {
                                // 1.7.10: cpw.mods.fml.common.Mod
                                if (annotation.desc.contains("cpw/mods/fml/common/Mod")) {
                                    String className = node.name.replace('/', '.');
                                    String modId = null;

                                    if (annotation.values != null) {
                                        for (int i = 0; i < annotation.values.size(); i += 2) {
                                            if (annotation.values.get(i).equals("modid")) { // 1.7.10縺ｯ value 縺ｧ縺ｯ縺ｪ縺・modid=... 縺ｨ譖ｸ縺上％縺ｨ縺悟､壹＞
                                                modId = (String) annotation.values.get(i + 1);
                                                break;
                                            }
                                            // value縺ｮ蝣ｴ蜷医ｂ縺ゅｋ
                                            if (annotation.values.get(i).equals("value")) {
                                                modId = (String) annotation.values.get(i + 1);
                                            }
                                        }
                                    }
                                    if (modId == null) modId = className.toLowerCase();
                                    mods.add(new ModInfo(className, modId));
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mods;
    }
}
