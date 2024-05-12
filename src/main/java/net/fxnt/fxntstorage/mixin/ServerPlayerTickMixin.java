package net.fxnt.fxntstorage.mixin;

import net.fxnt.fxntstorage.backpacks.upgrades.BackPackOnBackUpgradeHandler;
import net.fxnt.fxntstorage.backpacks.util.BackPackHelper;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerTickMixin {
    @Unique
    private boolean doMixin = true;
    private int slowTick = 0;
    private int mediumTick = 0;
    private final int slowTicks = 30;
    private final int mediumTicks = 15;

    @Inject(method = "tick", at = @At("HEAD"))
    public void fxnt$onTick(CallbackInfo info) {
        if (!doMixin) return;
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (player == null || player.isSpectator() || player.level().isClientSide || !player.isAlive() || player.isSleeping() || player.isDeadOrDying()) return;
        if (!new BackPackHelper().isWearingBackPack(player)) {
            mediumTick = 0;
            slowTick = 0;
            return;
        }
        mediumTick++;
        slowTick++;
        if (mediumTick >= mediumTicks) {
            BackPackOnBackUpgradeHandler handler = new BackPackOnBackUpgradeHandler(player);
            handler.applyRefillUpgrade();
            handler.applyFeederUpgrade();
            mediumTick = 0;
        }
        if (slowTick >= slowTicks) {
            new BackPackOnBackUpgradeHandler(player).applyMagnetUpgrade();
            slowTick = 0;
        }
    }
}