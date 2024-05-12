package net.fxnt.fxntstorage.backpacks.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class BackPackModelPlayer<T extends LivingEntity> extends HumanoidModel<T> {

    public final ModelPart modelPart;

    public BackPackModelPlayer(ModelPart root) {
        super(root);
        this.modelPart = root.getChild("bone");
    }

    public void setupAnim(HumanoidModel<T> model) {
        this.modelPart.copyFrom(model.body);
    }


    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        modelPart.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

}
