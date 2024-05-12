package net.fxnt.fxntstorage.init;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpacks.main.BackPackBlockMenu;
import net.fxnt.fxntstorage.backpacks.main.BackPackItemMenu;
import net.fxnt.fxntstorage.containers.StorageBoxMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class ModMenuTypes {
    public static final ExtendedScreenHandlerType<StorageBoxMenu> STORAGE_BOX_MENU = new ExtendedScreenHandlerType<>(StorageBoxMenu::new);
    public static final ExtendedScreenHandlerType<BackPackItemMenu> BACK_PACK_ITEM_MENU = new ExtendedScreenHandlerType<>(BackPackItemMenu::new);
    public static final ExtendedScreenHandlerType<BackPackBlockMenu> BACK_PACK_BLOCK_MENU = new ExtendedScreenHandlerType<>(BackPackBlockMenu::new);

    public static void register() {
        Registry.register(BuiltInRegistries.MENU, new ResourceLocation(FXNTStorage.MOD_ID, "storage_box_menu"), STORAGE_BOX_MENU);
        Registry.register(BuiltInRegistries.MENU, new ResourceLocation(FXNTStorage.MOD_ID, "back_pack_item_menu"), BACK_PACK_ITEM_MENU);
        Registry.register(BuiltInRegistries.MENU, new ResourceLocation(FXNTStorage.MOD_ID, "back_pack_block_menu"), BACK_PACK_BLOCK_MENU);
    }
}