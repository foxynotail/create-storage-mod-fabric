package net.fxnt.fxntstorage.compat;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import net.fxnt.fxntstorage.backpacks.main.BackPackScreen;
import net.fxnt.fxntstorage.containers.StorageBoxScreen;
import net.minecraft.client.renderer.Rect2i;

public class EmiCompat implements EmiPlugin {
    private static Bounds asEmiRect(Rect2i rect) {
        return new Bounds(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }
    @Override
    public void register(EmiRegistry registry) {

        registry.addGenericExclusionArea((screen, consumer) -> {
            if (screen instanceof StorageBoxScreen storageBoxScreen) {
                storageBoxScreen.getExclusionZones().stream().map(EmiCompat::asEmiRect).forEach(consumer);
            }
        });

        registry.addGenericExclusionArea((screen, consumer) -> {
            if (screen instanceof BackPackScreen backPackScreen) {
                backPackScreen.getExclusionZones().stream().map(EmiCompat::asEmiRect).forEach(consumer);
            }
        });
    }
}
