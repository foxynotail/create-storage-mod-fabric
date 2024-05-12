package net.fxnt.fxntstorage.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fxnt.fxntstorage.backpacks.main.BackPackScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {

    @Shadow @Final private PoseStack pose;
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at=@At("HEAD"), cancellable = true)
    public void fxnt$renderItemDecorations(Font font, ItemStack stack, int x, int y, @Nullable String text, CallbackInfo ci) {

        GuiGraphics guiGraphics = (GuiGraphics) (Object) this;

        Screen screen = this.minecraft.screen;
        if (!(screen instanceof BackPackScreen)) return;

        if (!stack.isEmpty()) {
            this.pose.pushPose();

            if (stack.getCount() != 1) {
                int count = stack.getCount();
                String countText = "" + count;
                this.pose.translate(0.0D, 0.0D, 200.0F);
                float scale = Math.min(1f, (float) 16 / font.width(countText));
                if (scale < 1f) {
                    this.pose.scale(scale, scale, 1.0F);
                }
                guiGraphics.drawString(font, countText, (int) ((x + 19 - 2 - (font.width(countText) * scale)) / scale),
                        (int) ((y + 6 + 3 + (1 / (scale * scale) - 1)) / scale), 16777215, true);
            }

            int k;
            int l;
            if (stack.isBarVisible()) {
                int i = stack.getBarWidth();
                int j = stack.getBarColor();
                k = x + 2;
                l = y + 13;
                guiGraphics.fill(RenderType.guiOverlay(), k, l, k + 13, l + 2, -16777216);
                guiGraphics.fill(RenderType.guiOverlay(), k, l, k + i, l + 1, j | -16777216);
            }

            LocalPlayer localPlayer = this.minecraft.player;
            float f = localPlayer == null ? 0.0F : localPlayer.getCooldowns().getCooldownPercent(stack.getItem(), this.minecraft.getFrameTime());
            if (f > 0.0F) {
                k = y + Mth.floor(16.0F * (1.0F - f));
                l = k + Mth.ceil(16.0F * f);
                guiGraphics.fill(RenderType.guiOverlay(), x, k, x + 16, l, Integer.MAX_VALUE);
            }
            this.pose.popPose();

        }
        ci.cancel();
    }
}
