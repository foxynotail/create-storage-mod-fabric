package net.fxnt.fxntstorage.compat;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpacks.main.BackPackScreen;
import net.fxnt.fxntstorage.containers.StorageBoxScreen;
import net.minecraft.resources.ResourceLocation;

public class REICompat implements REIClientPlugin {

    private static final ResourceLocation ID = new ResourceLocation(FXNTStorage.MOD_ID, "rei_compat");

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(StorageBoxScreen.class, StorageBoxScreen::getREIExclusionZones);
        zones.register(BackPackScreen.class, BackPackScreen::getREIExclusionZones);
    }


}
