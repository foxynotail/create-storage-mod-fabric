package net.fxnt.fxntstorage.mixin;

import net.fxnt.fxntstorage.backpacks.main.BackPackItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public abstract class PlayerInventoryMixin {
    @Inject(method = "setItem", at = @At("HEAD"))
    private void fxnt$setItem(int slotIndex, ItemStack stack, CallbackInfo ci) {
        Inventory inventory = (Inventory) (Object) this;
        Player player = inventory.player;
        if (!player.level().isClientSide) {
            if (slotIndex == 38 && stack.getItem() instanceof BackPackItem && !stack.isEmpty()) {
                player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.PLAYERS, 0.5F, 1.0F);
            }
        }
    }
}