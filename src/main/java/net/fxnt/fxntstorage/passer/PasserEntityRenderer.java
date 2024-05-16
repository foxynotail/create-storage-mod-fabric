package net.fxnt.fxntstorage.passer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class PasserEntityRenderer extends SmartBlockEntityRenderer<PasserSmartEntity> {
    public PasserEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(PasserSmartEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        FilteringRenderer.renderOnBlockEntity(blockEntity, partialTick, poseStack, buffer, 255, packedOverlay);
    }
}
