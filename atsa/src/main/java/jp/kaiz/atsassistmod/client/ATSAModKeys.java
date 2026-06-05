package jp.kaiz.atsassistmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/** Client key mappings. EB = emergency brake (was bound to RTM's KEY_EB). */
public final class ATSAModKeys {
    public static final String CATEGORY = "key.categories.atsassistmod";

    public static final KeyMapping EMERGENCY_BRAKE = new KeyMapping(
            "key.atsassistmod.emergency_brake",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY);

    private ATSAModKeys() {}
}
