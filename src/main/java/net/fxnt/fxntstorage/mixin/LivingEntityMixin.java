package net.fxnt.fxntstorage.mixin;

import net.fxnt.fxntstorage.backpacks.upgrades.BackPackOnBackUpgradeHandler;
import net.fxnt.fxntstorage.backpacks.util.BackPackHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Unique
    private final boolean doMixin = true;

    @Inject(method = "causeFallDamage(FFLnet/minecraft/world/damagesource/DamageSource;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void fxnt$onCauseFallDamage(float fallDistance, float damageMultiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (!doMixin) return;
        if (((Object)this) instanceof Player player) {
            if (player == null || player.isSpectator() || player.level().isClientSide || !player.isAlive() || player.isSleeping() || player.isDeadOrDying()) return;
            if (!new BackPackHelper().isWearingBackPack(player)) return;
            if(new BackPackOnBackUpgradeHandler(player).applyFallDamageUpgrade())  {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }
}
