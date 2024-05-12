package net.fxnt.fxntstorage.containers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public class StorageBoxEntityRenderer extends SmartBlockEntityRenderer<StorageBoxEntity> {
    private final BlockEntityRendererProvider.Context context;
    private static final float[] sideRotationY2D = { 0, 0, 2, 0, 3, 1 };
    private static final int TEXT_COLOR_TRANSPARENT = FastColor.ARGB32.color(0, 255, 255, 255);
    public StorageBoxEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.context = context;
    }

    private float getRotationYForSide2D (Direction side) {
        return sideRotationY2D[side.ordinal()] * 90 * (float)Math.PI / 180f;
    }

    @Override
    protected void renderSafe(StorageBoxEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {

        FilteringRenderer.renderOnBlockEntity(blockEntity, partialTick, poseStack, buffer, 255, packedOverlay);

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        Level level = blockEntity.getLevel();
        if (level == null) return;

        int amount = blockEntity.getStoredAmount();
        int percentUsed = blockEntity.getPercentageUsed();

        String amountText = Util.formatNumber(amount);
        String percentText = percentUsed + "% Used";

        //int color = Util.interpolateColor(0, 15, maxBright);
        float distance = (float)Math.sqrt(blockEntity.getBlockPos().distToCenterSqr(player.position()));
        float alpha = Math.max(1f - ((distance) / 10), 0.05f);

        if (distance > 10) return;

        float Line1Offset = 7f;
        float Line2Offset = 4f;

        renderLine(amountText, Line1Offset, blockEntity, partialTick, poseStack, buffer, packedLight, alpha);
        renderLine(percentText, Line2Offset, blockEntity, partialTick, poseStack, buffer, packedLight, alpha);

    }

    private void renderLine(String text, float YOffset, StorageBoxEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, float alpha) {

        Font textRenderer = this.context.getFont();
        int textWidth = textRenderer.width(text);

        BlockState blockState = blockEntity.getBlockState();
        Direction side = blockState.getValue(HorizontalDirectionalBlock.FACING);

        poseStack.pushPose();

        poseStack.translate(0.5f, 0, 0.5f);
        poseStack.mulPoseMatrix((new Matrix4f()).rotateYXZ(getRotationYForSide2D(side), 0, 0));
        poseStack.translate(-0.5f, 0, -0.5f);

        // Adjust position to render on the block face
        float ZOffset = 15.05f;
        poseStack.translate(0.5f, YOffset/16f, ZOffset/16f);  // Adjust these values as needed

        // Flip Text Upside Down & Shrink
        poseStack.scale(1/64f, -1/64f, 1f);

        int color = 0xFFFFFF;
        color = (int) (255 * alpha) << 24 | TEXT_COLOR_TRANSPARENT;
        float x = (float) -textWidth / 2;
        float y = 0;
        boolean dropShadow = false;
        Matrix4f matrix = poseStack.last().pose();
        MultiBufferSource buf = buffer;
        Font.DisplayMode displayMode = Font.DisplayMode.NORMAL;
        int backgroundColor = 0;
        packedLight = 250;

        textRenderer.drawInBatch(text, x, y, color, dropShadow, matrix, buf, displayMode, backgroundColor, packedLight);

        poseStack.popPose();
    }
}
