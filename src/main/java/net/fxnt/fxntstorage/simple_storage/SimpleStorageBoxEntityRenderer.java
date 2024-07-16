package net.fxnt.fxntstorage.simple_storage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.fxnt.fxntstorage.init.ModItems;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public class SimpleStorageBoxEntityRenderer extends SmartBlockEntityRenderer<SimpleStorageBoxEntity> {
    private final BlockEntityRendererProvider.Context context;
    private static final float[] sideRotationY2D = { 0, 0, 2, 0, 3, 1 };
    private static final int TEXT_COLOR_TRANSPARENT = FastColor.ARGB32.color(0, 255, 255, 255);
    public SimpleStorageBoxEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.context = context;
    }

    private float getRotationYForSide2D (Direction side) {
        return sideRotationY2D[side.ordinal()] * 90 * (float)Math.PI / 180f;
    }

    @Override
    protected void renderSafe(SimpleStorageBoxEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        Level level = blockEntity.getLevel();
        if (level == null) return;

        int amount = blockEntity.getStoredAmount();

        String line1 = Util.formatNumber(amount);
        String line2;

        int percentUsed = blockEntity.getPercentageUsed();
        line2 = percentUsed + "% Used";
        //line2 = Util.formatNumber(blockEntity.getMaxItemCapacity());

        float distance = (float)Math.sqrt(blockEntity.getBlockPos().distToCenterSqr(player.position()));
        float alpha = Math.max(1f - ((distance) / 10), 0.05f);

        if (distance > 10) return;

        float Line1Offset = -1f/16f;
        float Line2Offset = -4f/16f;

        BlockState blockState = blockEntity.getBlockState();
        Direction side = blockState.getValue(HorizontalDirectionalBlock.FACING);

        poseStack.pushPose();

        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.mulPoseMatrix((new Matrix4f()).rotateYXZ(getRotationYForSide2D(side), 0, 0));
        poseStack.translate(-0.5f, 0, -0.5f);

        // Adjust position to render on the block face
        float zOffset = 15.05f/16f;

        packedLight = 255;

        renderLine(line1, Line1Offset, zOffset, packedLight, poseStack, buffer, alpha);
        renderLine(line2, Line2Offset, zOffset, packedLight, poseStack, buffer, alpha);


        ItemStack filterItem = blockEntity.getFilterItem();
        if (!filterItem.isEmpty()) {
            renderItem(filterItem, zOffset, poseStack, buffer, packedLight, packedOverlay);
        }

        if (blockEntity.hasVoidUpgrade()) {
            renderVoidIcon(zOffset, poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private void renderLine(String text, float yOffset, float zOffset, int packedLight, PoseStack poseStack, MultiBufferSource buffer, float alpha) {

        Font textRenderer = this.context.getFont();
        int textWidth = textRenderer.width(text);

        poseStack.pushPose();
        // Adjust position to render on the block face
        poseStack.translate(0.5f, yOffset, zOffset);
        // Flip Text Upside Down & Shrink
        poseStack.scale(1/64f, -1/64f, 1f);

        int color = 0xFFFFFF;
        color = (int) (255 * alpha) << 24 | TEXT_COLOR_TRANSPARENT;
        float x = (float) -textWidth / 2;
        float y = 0;
        boolean dropShadow = false;
        Matrix4f matrix = poseStack.last().pose();
        Font.DisplayMode displayMode = Font.DisplayMode.NORMAL;
        int backgroundColor = 0;

        textRenderer.drawInBatch(text, x, y, color, dropShadow, matrix, buffer, displayMode, backgroundColor, packedLight);
        poseStack.popPose();
    }

    private void renderItem(ItemStack filter, float zOffset, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        BakedModel modelWithOverrides = itemRenderer.getModel(filter, null, null, 0);
        boolean flatItem = !modelWithOverrides.isGui3d();

        zOffset += flatItem ? 0 : 0f;
        poseStack.translate(0.5f, 0.175f, zOffset);
        if (!flatItem) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
        }

        float scale = (flatItem ? 0.25f : 0.5f) + 1 / 64f;
        poseStack.scale(scale, scale, scale);

        itemRenderer.renderStatic(filter, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffer, mc.level, 0);
        poseStack.popPose();
    }

    private void renderVoidIcon(float zOffset, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        ItemStack icon = new ItemStack(ModItems.STORAGE_BOX_VOID_UPGRADE);
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();

        poseStack.translate(0.8f, 0.3f, zOffset);

        float scale = 0.25f + (1 / 64f);
        poseStack.scale(scale, scale, scale);

        itemRenderer.renderStatic(icon, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffer, mc.level, 0);
        poseStack.popPose();
    }
}
