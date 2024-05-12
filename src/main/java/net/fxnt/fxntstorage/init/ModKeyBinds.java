package net.fxnt.fxntstorage.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class ModKeyBinds {

    private static final String KEYBIND_FXNTSTORAGE_CATEGORY = "keybind.fxntstorage.category";
    public static final KeyMapping BACKPACK_OPEN_KEYBIND = new KeyMapping(
            "key.fxntstorage.open_backpack",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.fxntstorage"
    );

    public static final KeyMapping CLEAR_BACKPACK_SHAPE_CACHE = new KeyMapping(
            "key.fxntstorage.clear_backpack_shape_cache",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F10,
            "key.categories.fxntstorage"
    );

    public static final KeyMapping BACKPACK_HOVER_KEYBIND = new KeyMapping(
            "key.fxntstorage.backpack_hover",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.fxntstorage"
    );

    public static void register() {
        KeyBindingHelper.registerKeyBinding(BACKPACK_OPEN_KEYBIND);
        KeyBindingHelper.registerKeyBinding(CLEAR_BACKPACK_SHAPE_CACHE);
        KeyBindingHelper.registerKeyBinding(BACKPACK_HOVER_KEYBIND);
    }


}
