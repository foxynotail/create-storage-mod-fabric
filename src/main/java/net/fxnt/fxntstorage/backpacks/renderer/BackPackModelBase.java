package net.fxnt.fxntstorage.backpacks.renderer;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;

public class BackPackModelBase {
    public static LayerDefinition createModel(boolean isOnEntity) {

        MeshDefinition meshDefinition;

        if (isOnEntity) {
            CubeDeformation cube = CubeDeformation.NONE;
            meshDefinition = PlayerModel.createMesh(cube, 0.0F);
        } else {
            meshDefinition = new MeshDefinition();
        }

        meshDefinition.getRoot().addOrReplaceChild("bone", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-5.0F, -11.0F, -3.0F, 10.0F, 11.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(7, 6).addBox(-4.5F, -10.5F, 3.0F, 9.0F, 10.0F, 0.25F, new CubeDeformation(0.0F))
            .texOffs(54, 5).addBox(-5.25F, -10.5F, -2.5F, 0.25F, 10.0F, 5.0F, new CubeDeformation(0.0F))
            .texOffs(55, 5).addBox(5.0F, -10.5F, -2.5F, 0.25F, 10.0F, 5.0F, new CubeDeformation(0.0F))
            .texOffs(31, 5).addBox(-4.5F, 0.0F, -2.5F, 9.0F, 0.25F, 5.0F, new CubeDeformation(0.0F))
            .texOffs(52, 11).addBox(-4.0F, -7.5F, -5.0F, 8.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(0, 17).addBox(-3.0F, -6.5F, -5.5F, 6.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(14, 17).addBox(-2.0F, -14.0F, 0.0F, 4.0F, 0.25F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0).addBox(-2.0F, -13.75F, 0.0F, 1.0F, 0.75F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(26, 0).addBox(1.0F, -13.75F, 0.0F, 1.0F, 0.75F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(5, 3).addBox(-4.5F, -13.0F, -1.0F, 9.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(0, 7).addBox(-4.75F, -12.8F, -0.75F, 0.25F, 1.8F, 2.75F, new CubeDeformation(0.0F))
            .texOffs(0, 6).addBox(4.5F, -12.8F, -0.75F, 0.25F, 1.8F, 2.75F, new CubeDeformation(0.0F))
            .texOffs(6, 5).addBox(-4.5F, -12.5F, -2.0F, 9.0F, 1.5F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(63, 21).addBox(-4.0F, -11.1F, -2.1F, 8.0F, 0.1F, 0.1F, new CubeDeformation(0.0F))
            .texOffs(33, 21).addBox(-2.0F, -6.1F, -5.6F, 4.0F, 0.1F, 0.1F, new CubeDeformation(0.0F))
            .texOffs(24, 17).addBox(-2.0F, -6.0F, -5.6F, 0.1F, 0.2F, 0.1F, new CubeDeformation(0.0F))
            .texOffs(30, 18).addBox(-4.0F, -11.1F, -2.3F, 0.1F, 0.1F, 0.2F, new CubeDeformation(0.0F))
            .texOffs(6, 5).addBox(-4.5F, -12.8F, 2.0F, 9.0F, 1.8F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(55, 22).addBox(-3.5F, -8.0F, -4.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(2, 23).addBox(-3.5F, -12.0F, 3.0F, 2.0F, 11.0F, 0.75F, new CubeDeformation(0.0F))
            .texOffs(1, 23).addBox(1.5F, -12.0F, 3.0F, 2.0F, 11.0F, 0.75F, new CubeDeformation(0.0F))
            .texOffs(35, 21).addBox(-3.25F, -12.75F, 3.0F, 1.5F, 0.75F, 0.25F, new CubeDeformation(0.0F))
            .texOffs(35, 21).addBox(1.75F, -12.75F, 3.0F, 1.5F, 0.75F, 0.25F, new CubeDeformation(0.0F))
            .texOffs(35, 20).addBox(1.75F, -1.05F, 3.0F, 1.5F, 0.8F, 0.4F, new CubeDeformation(0.0F))
            .texOffs(35, 20).addBox(-3.25F, -1.05F, 3.0F, 1.5F, 0.8F, 0.4F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0, 0, 0, 0, 0, 0));

        return LayerDefinition.create(meshDefinition, 128, 128);
    }
}