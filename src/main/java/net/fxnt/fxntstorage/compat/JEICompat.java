package net.fxnt.fxntstorage.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpacks.main.BackPackScreen;
import net.fxnt.fxntstorage.containers.StorageBoxScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@JeiPlugin
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class JEICompat implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(FXNTStorage.MOD_ID, "jei_comapat");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(StorageBoxScreen.class, new IGuiContainerHandler<>() {
            @NotNull
            @Override
            public List<Rect2i> getGuiExtraAreas(StorageBoxScreen screen) {
                return screen.getExclusionZones();
            }
        });
        registration.addGuiContainerHandler(BackPackScreen.class, new IGuiContainerHandler<>() {
            @NotNull
            @Override
            public List<Rect2i> getGuiExtraAreas(BackPackScreen screen) {
                return screen.getExclusionZones();
            }
        });
    }
}
