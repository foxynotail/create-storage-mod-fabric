package net.fxnt.fxntstorage.mixin;

import net.fxnt.fxntstorage.backpacks.main.BackPackItem;
import net.fxnt.fxntstorage.backpacks.upgrades.BackPackOnBackUpgradeHandler;
import net.fxnt.fxntstorage.backpacks.util.BackPackHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    // Never Despawn Back Pack
    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V", at = @At("RETURN"))
    private void fxnt$init(CallbackInfo info) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;
        if (itemEntity.getItem().getItem() instanceof BackPackItem) {
            itemEntity.setUnlimitedLifetime();
        }
    }

    @Shadow private int pickupDelay;
    @Shadow private @Nullable UUID target;
    @Unique
    private final boolean doMixin = true;

    @Inject(method = "playerTouch", at = @At(value = "HEAD"), cancellable = true)
    private void fxnt$onPlayerPickUpItem(Player player, CallbackInfo ci) {
        if (!doMixin) return;
        ItemEntity itemEntity = (ItemEntity) (Object) this;
        if (player == null || player.isSpectator() || player.level().isClientSide || !player.isAlive() || player.isSleeping() || player.isDeadOrDying()) return;
        if (!new BackPackHelper().isWearingBackPack(player)) return;
        if(new BackPackOnBackUpgradeHandler(player).applyItemPickupUpgrade(itemEntity, target, pickupDelay))  {
            ci.cancel();
        }
    }
}