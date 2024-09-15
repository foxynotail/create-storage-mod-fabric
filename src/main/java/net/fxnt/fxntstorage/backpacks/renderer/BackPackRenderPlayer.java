package net.fxnt.fxntstorage.backpacks.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpacks.util.BackPackHelper;
import net.fxnt.fxntstorage.init.ModBlocks;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BackPackRenderPlayer extends RenderLayer<AbstractClientPlayer, HumanoidModel<AbstractClientPlayer>> {
    private ResourceLocation TEXTURE_LOCATION;
    ModelPart modelPart = BackPackModelBase.createModel(true).bakeRoot();
    public BackPackModelPlayer model = new BackPackModelPlayer<>(modelPart);

    public BackPackRenderPlayer(RenderLayerParent<AbstractClientPlayer, HumanoidModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer livingEntity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        // Check if the current player being rendered (livingEntity) has a backpack equipped
        ItemStack backPack = new BackPackHelper().getWornBackPackStack(livingEntity);
        if (backPack.isEmpty()) {
            return;  // If no backpack is equipped, stop rendering
        }

        // Set the texture location based on the backpack type
        if (backPack.getItem().equals(ModBlocks.BACK_PACK_ITEM)) {
            TEXTURE_LOCATION = new ResourceLocation(FXNTStorage.MOD_ID, "textures/block/back_pack.png");
        } else if (backPack.getItem().equals(ModBlocks.ANDESITE_BACK_PACK_ITEM)) {
            TEXTURE_LOCATION = new ResourceLocation(FXNTStorage.MOD_ID, "textures/block/andesite_back_pack.png");
        } else if (backPack.getItem().equals(ModBlocks.COPPER_BACK_PACK_ITEM)) {
            TEXTURE_LOCATION = new ResourceLocation(FXNTStorage.MOD_ID, "textures/block/copper_back_pack.png");
        } else if (backPack.getItem().equals(ModBlocks.BRASS_BACK_PACK_ITEM)) {
            TEXTURE_LOCATION = new ResourceLocation(FXNTStorage.MOD_ID, "textures/block/brass_back_pack.png");
        } else if (backPack.getItem().equals(ModBlocks.HARDENED_BACK_PACK_ITEM)) {
            TEXTURE_LOCATION = new ResourceLocation(FXNTStorage.MOD_ID, "textures/block/hardened_back_pack.png");
        }

        // Prepare the vertex consumer for rendering the texture
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(TEXTURE_LOCATION));

        poseStack.pushPose();

        // Apply transformations if the player is sneaking
        if (livingEntity.isShiftKeyDown()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(50f));
            poseStack.translate(0f, 0.0f, -0.60f);
        }

        // Sync model with the player's model
        this.getParentModel().copyPropertiesTo(model);
        model.setupAnim(this.getParentModel());

        // Adjust the backpack position and scale
        poseStack.mulPose(Axis.YP.rotationDegrees(180F));
        poseStack.translate(0f, 0.65f, -0.3f);
        poseStack.scale(0.85f, 0.85f, 0.85f);

        // Render the backpack
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);

        poseStack.popPose();
    }
}
