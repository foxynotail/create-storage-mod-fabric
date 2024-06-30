package net.fxnt.fxntstorage.mixin;

import net.fxnt.fxntstorage.backpacks.util.BackPackHelper;
import net.fxnt.fxntstorage.backpacks.util.BackPackNetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class PickBlockMixin {
    @Unique
    private final boolean doMixin = true;

    @Shadow @Nullable public LocalPlayer player;
    @Shadow @Nullable public HitResult hitResult;

    @Shadow @Nullable public ClientLevel level;

    @Inject(method = "pickBlock", at = @At("RETURN"))
    private void fxnt$checkBackPack(CallbackInfo ci) {
        if (!doMixin) return;
        if (player == null || level == null || player.isSpectator() || !player.level().isClientSide || !player.isAlive() || player.isSleeping() || player.isDeadOrDying()) return;
        if (!new BackPackHelper().isWearingBackPack(player)) return;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
            BackPackNetworkHelper.doPickBlock(blockPos);
        }
    }
}
