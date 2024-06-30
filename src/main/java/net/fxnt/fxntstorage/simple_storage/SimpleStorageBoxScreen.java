package net.fxnt.fxntstorage.simple_storage;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class SimpleStorageBoxScreen extends AbstractContainerScreen<SimpleStorageBoxMenu> {
    private final ResourceLocation guiTexture = new ResourceLocation(FXNTStorage.MOD_ID,  "textures/gui/container/simple_storage_box_screen.png");
    private final int guiTextureWidth = 176;
    private final int guiTextureHeight = 176;

    @Nullable
    public static SimpleStorageBoxScreen createScreen(SimpleStorageBoxMenu menu, Inventory playerInventory, Component title) {
        return new SimpleStorageBoxScreen(menu, playerInventory, title);
    }
    public SimpleStorageBoxScreen(SimpleStorageBoxMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = guiTextureWidth;
        imageHeight = guiTextureHeight;
    }
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        renderFilterItem(graphics, leftPos + 30, topPos + 20);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(guiTexture, leftPos, topPos, 0, 0, imageWidth, imageHeight, guiTextureWidth, guiTextureHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 7, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 7, 93 - 11, 0x404040, false);
        graphics.drawString(font, "Stored: " + menu.simpleStorageBoxEntity.getStoredAmount(), 66, 20, 0x404040, false);
        graphics.drawString(font, "Capacity: " + menu.simpleStorageBoxEntity.getMaxItemCapacity(), 66, 32, 0x404040, false);
        graphics.drawString(font, "Void Mode: " + (menu.simpleStorageBoxEntity.hasVoidUpgrade() ? "Enabled" : "Disabled"), 66, 44, 0x404040, false);
    }

    private void renderFilterItem(GuiGraphics graphics, int x, int y) {
        ItemStack itemStack = menu.simpleStorageBoxEntity.filterItem;
        graphics.pose().pushPose();
        if (!itemStack.isEmpty()) {
            renderFilterItemStack(graphics, itemStack, x + 8f, y + 8f);
        }
        renderFilterItemDecoration(graphics, font, itemStack, x, y, menu.simpleStorageBoxEntity.getStoredAmount());
        graphics.pose().popPose();
    }

    private void renderFilterItemStack(GuiGraphics graphics, ItemStack stack, float x, float y) {
        if (!stack.isEmpty()) {

            PoseStack poseStack = graphics.pose();
            MultiBufferSource buffer = graphics.bufferSource();

            BakedModel bakedModel = this.minecraft.getItemRenderer().getModel(stack, null, null, 0);
            poseStack.pushPose();
            poseStack.translate(x + 8, y + 8, 150);

            try {
                poseStack.mulPoseMatrix((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
                poseStack.scale(24.0F, 24.0F, 24.0F);
                boolean bl = !bakedModel.usesBlockLight();
                if (bl) {
                    Lighting.setupForFlatItems();
                }

                this.minecraft.getItemRenderer().render(stack, ItemDisplayContext.GUI, false, poseStack, buffer, 15728880, OverlayTexture.NO_OVERLAY, bakedModel);
                graphics.flush();
                if (bl) {
                    Lighting.setupFor3DItems();
                }
            } catch (Throwable var12) {
                CrashReport crashReport = CrashReport.forThrowable(var12, "Rendering item");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Item being rendered");
                crashReportCategory.setDetail("Item Type", () -> {
                    return String.valueOf(stack.getItem());
                });
                crashReportCategory.setDetail("Item Damage", () -> {
                    return String.valueOf(stack.getDamageValue());
                });
                crashReportCategory.setDetail("Item NBT", () -> {
                    return String.valueOf(stack.getTag());
                });
                crashReportCategory.setDetail("Item Foil", () -> {
                    return String.valueOf(stack.hasFoil());
                });
                throw new ReportedException(crashReport);
            }

            poseStack.popPose();
        }
    }

    public void renderFilterItemDecoration(GuiGraphics graphics, Font font, ItemStack stack, int x, int y, int amount) {

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        if (!stack.isEmpty() && stack.isBarVisible()) {
            int i = stack.getBarWidth();
            int j = stack.getBarColor();
            int xOffset = x + 3;
            int yOffset = y + 28;
            int width = 26;
            int height = 2;
            graphics.fill(RenderType.guiOverlay(), xOffset, yOffset, xOffset + width, yOffset + height, -16777216);
            graphics.fill(RenderType.guiOverlay(), xOffset, yOffset, xOffset + (i*2), yOffset + (height/2), j | -16777216);
        }

        String string = Util.formatNumber(amount);
        poseStack.translate(0.0F, 0.0F, 200.0F);
        graphics.drawString(font, string, x + 33 - font.width(string), y + 25, 16777215, true);

        poseStack.popPose();
    }

}
