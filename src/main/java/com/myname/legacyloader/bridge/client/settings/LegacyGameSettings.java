package com.myname.legacyloader.bridge.client.settings;

import net.minecraft.client.Minecraft;

import java.io.File;

public class LegacyGameSettings {
    public float fovSetting = 0.0F;
    public float gammaSetting = 0.0F;
    public float saturation = 0.0F;
    public float mouseSensitivity = 0.5F;
    public float chatOpacity = 1.0F;
    public float chatHeightFocused = 1.0F;
    public float chatHeightUnfocused = 0.44366196F;
    public float chatScale = 1.0F;
    public float chatWidth = 1.0F;
    public float anisotropicFiltering = 1.0F;
    public int mipmapLevels = 4;
    public int renderDistanceChunks = 12;
    public int limitFramerate = 120;
    public int ambientOcclusion = 2;
    public int thirdPersonView = 0;
    public int overrideWidth;
    public int overrideHeight;

    public boolean advancedOpengl;
    public boolean field_74349_h;
    public boolean field_74337_g;
    public boolean field_151448_g = true;
    public boolean fancyGraphics = true;
    public boolean enableVsync = true;
    public boolean fullScreen;
    public boolean forceUnicodeFont;
    public boolean showDebugInfo;
    public boolean showDebugProfilerChart;
    public boolean advancedItemTooltips;
    public boolean pauseOnLostFocus = true;
    public boolean hideGUI;
    public boolean noclip;
    public boolean clouds = true;
    public boolean chatColours = true;
    public boolean snooperEnabled = true;
    public boolean showCape = true;

    public String language = "en_US";
    public final Minecraft mc;
    public final File optionsFile;

    public LegacyGameSettings() {
        this(Minecraft.getInstance(), Minecraft.getInstance().gameDirectory);
    }

    public LegacyGameSettings(Minecraft minecraft, File gameDirectory) {
        this.mc = minecraft;
        this.optionsFile = new File(gameDirectory, "options.txt");
        syncFromModern();
    }

    public void loadOptions() {
        syncFromModern();
    }

    public void saveOptions() {
        if (mc != null && mc.options != null) {
            mc.options.save();
        }
    }

    public void setOptionValue(Options option, int delta) {
        if (option == Options.RENDER_DISTANCE) {
            renderDistanceChunks = Math.max(2, renderDistanceChunks + delta);
        }
    }

    public void setOptionFloatValue(Options option, float value) {
        switch (option) {
            case FOV -> fovSetting = value;
            case GAMMA -> gammaSetting = value;
            case SENSITIVITY -> mouseSensitivity = value;
            case CHAT_OPACITY -> chatOpacity = value;
            case CHAT_HEIGHT_FOCUSED -> chatHeightFocused = value;
            case CHAT_HEIGHT_UNFOCUSED -> chatHeightUnfocused = value;
            case CHAT_SCALE -> chatScale = value;
            case CHAT_WIDTH -> chatWidth = value;
            case ANISOTROPIC_FILTERING -> anisotropicFiltering = value;
            case MIPMAP_LEVELS -> mipmapLevels = (int) value;
            case RENDER_DISTANCE -> renderDistanceChunks = (int) value;
            default -> {
            }
        }
    }

    public float getOptionFloatValue(Options option) {
        return switch (option) {
            case FOV -> fovSetting;
            case GAMMA -> gammaSetting;
            case SATURATION -> saturation;
            case SENSITIVITY -> mouseSensitivity;
            case CHAT_OPACITY -> chatOpacity;
            case CHAT_HEIGHT_FOCUSED -> chatHeightFocused;
            case CHAT_HEIGHT_UNFOCUSED -> chatHeightUnfocused;
            case CHAT_SCALE -> chatScale;
            case CHAT_WIDTH -> chatWidth;
            case ANISOTROPIC_FILTERING -> anisotropicFiltering;
            case MIPMAP_LEVELS -> mipmapLevels;
            case RENDER_DISTANCE -> renderDistanceChunks;
            case FRAMERATE_LIMIT -> limitFramerate;
            default -> 0.0F;
        };
    }

    public boolean getOptionOrdinalValue(Options option) {
        return switch (option) {
            case ADVANCED_OPENGL -> advancedOpengl;
            case FANCY_GRAPHICS -> fancyGraphics;
            case AMBIENT_OCCLUSION -> ambientOcclusion != 0;
            case CLOUDS -> clouds;
            default -> false;
        };
    }

    public String getKeyBinding(Options option) {
        return option == null ? "" : option.name();
    }

    public void sendSettingsToServer() {
    }

    private void syncFromModern() {
        if (mc == null || mc.options == null) {
            return;
        }
        renderDistanceChunks = mc.options.renderDistance().get();
        fancyGraphics = mc.options.graphicsMode().get().getKey().contains("fancy");
        field_74349_h = advancedOpengl;
        field_151448_g = true;
        enableVsync = mc.options.enableVsync().get();
        forceUnicodeFont = mc.options.forceUnicodeFont().get();
        hideGUI = mc.options.hideGui;
        gammaSetting = mc.options.gamma().get().floatValue();
        language = mc.options.languageCode;
    }

    public enum Options {
        FOV,
        GAMMA,
        SATURATION,
        SENSITIVITY,
        CHAT_OPACITY,
        CHAT_HEIGHT_FOCUSED,
        CHAT_HEIGHT_UNFOCUSED,
        CHAT_SCALE,
        CHAT_WIDTH,
        FRAMERATE_LIMIT,
        ANISOTROPIC_FILTERING,
        MIPMAP_LEVELS,
        RENDER_DISTANCE,
        ADVANCED_OPENGL,
        FANCY_GRAPHICS,
        AMBIENT_OCCLUSION,
        CLOUDS,
        STREAM_BYTES_PER_PIXEL,
        STREAM_VOLUME_MIC,
        STREAM_VOLUME_SYSTEM,
        STREAM_KBPS,
        STREAM_FPS
    }
}
