package net.fxnt.fxntstorage.mixin;

import net.fxnt.fxntstorage.backpacks.upgrades.JetpackController;
import net.fxnt.fxntstorage.backpacks.util.BackPackNetworkHelper;
import net.fxnt.fxntstorage.network.BackPackPackets;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerTickMixin {
    @Unique
    private boolean doMixin = true;

    @Inject(method = "aiStep", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/Input;jumping:Z", ordinal = 0), cancellable = true)
    private void fxnt$OnJumpingInput(CallbackInfo ci) {

        if (!doMixin) return;
        boolean doFlight = false;

        LocalPlayer player = (LocalPlayer) (Object) this;
        if (player == null || player.isSpectator() || !player.level().isClientSide || !player.isAlive() || player.isSleeping() || player.isDeadOrDying()) return;

        BackPackNetworkHelper.checkHasFlightUpgrade();
        boolean hasFlightUpgrade = JetpackController.JetpackState.checkHasFlightUpgrade();
        if (hasFlightUpgrade) {
            if (player.input.jumping) {
                player.resetFallDistance();
                doFlight = true;
            } else if (!player.onGround()) {
                doFlight = true;
            }
            if (doFlight && !player.getAbilities().flying && !player.isFallFlying()) {
                player.setNoGravity(true);
                JetpackController jetpackController = new JetpackController(player, player.input);
                jetpackController.doJetPack();
            } else if (!player.getAbilities().flying) {
                player.setNoGravity(false);
            }
        } else if (!player.getAbilities().flying) {
            player.setNoGravity(false);
            doFlight = false;
        }

    }

}
