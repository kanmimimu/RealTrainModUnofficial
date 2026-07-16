package com.myname.legacyloader.bridge.item;

import com.myname.legacyloader.LegacyLoaderMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class LegacyItemGroup extends CreativeModeTab {

    public static final List<LegacyItemGroup> TABS = new ArrayList<>();
    private final String label;
    private final String ownerModId;

    public LegacyItemGroup(String label) {
        super(CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                // 笘・ｿｮ豁｣: 蜊倥↑繧区枚蟄励〒縺ｯ縺ｪ縺上・itemGroup." 繧剃ｻ倥￠縺溽ｿｻ險ｳ繧ｳ繝ｳ繝昴・繝阪Φ繝医↓縺吶ｋ
                // 縺薙ｌ縺ｧ lang 繝輔ぃ繧､繝ｫ蜀・・ "itemGroup.examplemod_tab" 繧貞盾辣ｧ縺吶ｋ繧医≧縺ｫ縺ｪ繧翫∪縺・
                .title(Component.translatable("itemGroup." + label))
                .icon(() -> new ItemStack(Items.BARRIER))
                .displayItems((p, o) -> {})
        );
        this.label = label;
        this.ownerModId = LegacyLoaderMod.CURRENT_LOADING_MODID;

        forceSetBackground();

        TABS.add(this);
    }

    private void forceSetBackground() {
        try {
            ResourceLocation defaultBg = ResourceLocation.parse("textures/gui/container/creative_inventory/tab_items.png");
            Class<?> clazz = CreativeModeTab.class;
            boolean success = false;

            String[] candidates = {"backgroundTexture", "f_260656_", "field_260656_"};

            for (String name : candidates) {
                try {
                    Field f = clazz.getDeclaredField(name);
                    f.setAccessible(true);
                    f.set(this, defaultBg);
                    success = true;
                    break;
                } catch (NoSuchFieldException e) {
                }
            }

            if (!success) {
                for (Field f : clazz.getDeclaredFields()) {
                    if (f.getType() == ResourceLocation.class) {
                        f.setAccessible(true);
                        Object val = f.get(this);
                        if (val == null) {
                            f.set(this, defaultBg);
                            success = true;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ItemStack getIconItem() {
        return makeIcon();
    }

    public ItemStack makeIcon() {
        try {
            Class<?> clazz = this.getClass();
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getParameterCount() == 0 && ItemStack.class.isAssignableFrom(m.getReturnType())) {
                    if (m.getName().equals("makeIcon") || m.getName().equals("getIconItem")) {
                        continue;
                    }
                    m.setAccessible(true);
                    Object result = m.invoke(this);
                    if (result instanceof ItemStack) {
                        return (ItemStack) result;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ItemStack(Items.BARRIER);
    }

    public String getLabel() { return this.label; }
    public String getOwnerModId() { return this.ownerModId; }
}