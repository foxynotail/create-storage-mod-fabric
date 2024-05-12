package net.fxnt.fxntstorage.util;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ParticleHelper {
    public static void jetPackParticles(Player player) {

        Level level = player.level();
        ParticleOptions particleType = ParticleTypes.CLOUD;

        Vec3 position = player.position();
        double posX = position.x;
        double posY = position.y + 0.55;
        double posZ = position.z;

        // Get the backward direction based on the player's yaw
        float yawRadians = (float) Math.toRadians(player.yBodyRot);
        double backwardX = Math.sin(yawRadians) * 0.35; // Offset to spawn particles slightly behind the player
        double backwardZ = -Math.cos(yawRadians) * 0.35; // Offset to spawn particles slightly behind the player

        double finalPosX = posX + backwardX;
        double finalPosZ = posZ + backwardZ;

        double speed = -(Math.random() / 10);

        // Spawn the particle
        level.addParticle(particleType, finalPosX, posY, finalPosZ, 0, speed, 0);
    }
}
