package net.fxnt.fxntstorage.backpacks.upgrades;

import com.simibubi.create.AllSoundEvents;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpacks.util.BackPackNetworkHelper;
import net.fxnt.fxntstorage.network.BackPackPackets;
import net.fxnt.fxntstorage.util.ParticleHelper;
import net.minecraft.client.player.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

public class JetpackController {

    private final Player player;
    private Input input;
    public static boolean isHovering = false;
    public static double hoverHeight = 0;
    private static boolean hasJumpedFromGround = false;
    private float jetPackFuelRemaining;
    private final double gravity = -1.7;//-0.7;
    private final double thrust = 0.5;
    private final int maxAllowedHeight = 32;
    private final double defaultPlayerWalkSpeed = 0.17;
    private final double defaultPlayerSneakSpeed = 0.08;
    private final double defaultPlayerSprintSpeed = 0.21;
    private final double baseFlySpeedBoost = 0.5;
    private final double baseHoverSpeedBoost = 0.25;

    public JetpackController(Player player, Input input) {
        this.player = player;
        this.input = input;
    }

    public JetpackController(Player player) {
        this.player = player;
    }

    public void doJetPack() {
        if (player.onGround() && !input.jumping) {
            deactivateHovering();
            player.setNoGravity(false);
            return;
        }
        getJetPackFuel();
        // Check if any fuel in backpack first
        if(jetPackFuelRemaining <= 0) {
            endHovering(false);
            player.setNoGravity(false);
            return;
        }
        if (player.isShiftKeyDown() && input.jumping) {
            startHovering(true);
        } else if (isHovering && input.jumping) {
            endHovering(true);
        }
        updateJetpackMovement();
        if (input.jumping || isHovering) {
            depleteJetPackFuel();
            ParticleHelper.jetPackParticles(player);
        }
        if (player.onGround() && isHovering) {
            deactivateHovering();
        }
        if (player.onGround() && input.jumping && !hasJumpedFromGround) {
            player.level().playSound(player, player.blockPosition(), AllSoundEvents.STEAM.getMainEvent(), SoundSource.PLAYERS, 0.1f, 1.0f);
            hasJumpedFromGround = true;
        }
        if (!player.onGround()) {
            hasJumpedFromGround = false;
        }

    }

    public void updateJetpackMovement() {

        Vec3 viewDirection = player.getViewVector(1.0f);
        Vec3 strafeDirection = viewDirection.cross(new Vec3(0, 1, 0)).normalize();

        // Determine movement direction based on input
        double forward = input.forwardImpulse;
        double strafe = -input.leftImpulse;

        Vec3 movementDirection = viewDirection.scale(forward).add(strafeDirection.scale(strafe));

        // Apply acceleration and max speed
        double acceleration = isHovering ? 0.01 : 0.05;

        double horizontalSpeed = calculateHorizontalSpeed();
        Vec3 horizontalVelocity = applyMovementPhysics(player.getDeltaMovement(), movementDirection.normalize(), acceleration, horizontalSpeed);

        double flySpeed = calculateVerticalSpeed();
        double verticalHoverSpeed = isHovering ? calculateVerticalHoveringSpeed(hoverHeight) : 0;
        double verticalSpeed = player.getDeltaMovement().y;

        int distanceToGround = getDistance(player);

        if ((jetPackFuelRemaining < 10.0f || distanceToGround > maxAllowedHeight) && input.jumping) {
            // Prevent player going up (Slow fall speed)
            verticalSpeed = lerp(verticalSpeed, gravity/10, 0.5);
        } else if (isHovering && Math.abs(verticalSpeed) > 0.1) {
            // Slow to Hover Speed (0)
            verticalSpeed = lerp(verticalSpeed, verticalHoverSpeed, 0.5);
        } else if (isHovering) {
            player.resetFallDistance();
            verticalSpeed = verticalHoverSpeed;
        } else {
            // Normal Flight
            verticalSpeed = flySpeed;
        }
        if (input.jumping) {
            player.resetFallDistance();
        }
        player.setDeltaMovement(horizontalVelocity.x, verticalSpeed, horizontalVelocity.z);
    }
    private Vec3 applyMovementPhysics(Vec3 currentVelocity, Vec3 direction, double acceleration, double maxSpeed) {
        Vec3 targetVelocity = currentVelocity.add(direction.scale(acceleration));
        double speed = Math.sqrt(targetVelocity.x * targetVelocity.x + targetVelocity.z * targetVelocity.z);
        if (speed > maxSpeed) {
            double scale = maxSpeed / speed;
            targetVelocity = new Vec3(targetVelocity.x * scale, currentVelocity.y, targetVelocity.z * scale);
        }
        return targetVelocity;
    }

    private double calculateHorizontalSpeed() {
        double enchantedSpeedMultiplier = EnchantmentHelper.getSneakingSpeedBonus(player);
        double mobEffectSpeedMultiplier = player.hasEffect(MobEffects.MOVEMENT_SPEED) ? player.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier() : 0.0;

        double baseSpeed = defaultPlayerWalkSpeed;
        if (player.isSprinting()) baseSpeed = defaultPlayerSprintSpeed;
        if (isHovering) baseSpeed = defaultPlayerSneakSpeed;

        double horizontalSpeed = baseSpeed + baseFlySpeedBoost + (mobEffectSpeedMultiplier/10);
        if (isHovering) {
            horizontalSpeed = baseSpeed + baseHoverSpeedBoost + enchantedSpeedMultiplier + (mobEffectSpeedMultiplier/10);
        }
        return horizontalSpeed;
    }

    private double calculateVerticalSpeed() {
        Vec3 motion = player.getDeltaMovement();
        double currentVerticalSpeed = motion.y;
        double dampingFactor = 0.15;  // Adjust this for smoother or more responsive changes 0.1, 0.05 (Not enough), 0.2 (Too Much) 0.175
        if (currentVerticalSpeed < 0 && !input.jumping) {
            dampingFactor = 0.05; // Smooth the transition using a damping factor (Less is smoother, more is more abrupt)
        }

        // Define the target vertical speed based on player input
        double verticalTarget;
        if (input.jumping) {
            verticalTarget = thrust;  // Increase for stronger upward thrust  0.1, 0.25, 0.5, 0.75, 0.7 too fast
        } else {
            verticalTarget = gravity;  // Custom gravity when not using the jetpack -0.05, -0.2, -0.5, -0.75
        }

        double newVerticalSpeed = currentVerticalSpeed * (1 - dampingFactor) + verticalTarget * dampingFactor;

        return newVerticalSpeed;
    }

    private double calculateVerticalHoveringSpeed(double targetHeight) {
        double currentHeight = player.getY();
        double heightDifference = targetHeight - currentHeight;

        long timeInMillis = System.currentTimeMillis();
        double bobbingFrequency = 0.3; // Lower the frequency to slow down the bobbing
        double bobbingAmplitude = 0.2; // The amplitude of the bobbing effect
        double cycleDuration = 8000;  // Duration of one full cycle in milliseconds
        double bobbing = Math.sin(2 * Math.PI * bobbingFrequency * (timeInMillis % cycleDuration) / 200);
        bobbing = bobbingAmplitude * bobbing;

        // Basic PID controller for height adjustment
        double P = 0.5; // Proportional gain
        double D = 0.1; // Damping factor to avoid overshooting

        return P * heightDifference + D * (heightDifference - bobbing);
    }

    private double lerp(double start, double end, double factor) {
        return start + factor * (end - start);
    }

    private static int getDistance(Player player){
        BlockPos blockPos = player.blockPosition();
        BlockPos offset = blockPos;
        int y = player.getBlockY();
        int distance = 0;
        for (int i = y; i >= -64; i--){
            offset = blockPos.atY(i);
            if(!player.level().getBlockState(offset).getBlock().defaultBlockState().isAir()) break;
            distance++;
        }
        return distance - 1;
    }
    public void deactivateHovering() {
        endHovering(true);
    }
    public void toggleHover() {
        if (player.onGround()) {
            deactivateHovering();
            return;
        }
        if (!isHovering) startHovering(true);
        else endHovering(true);
    }

    public void startHovering(boolean announce) {
        isHovering = true;
        hoverHeight = player.getY();
        if (announce) player.displayClientMessage(Component.literal("Hovering Activated"), true);
    }
    public void endHovering(boolean announce) {
        isHovering = false;
        hoverHeight = 0;
        if (announce) player.displayClientMessage(Component.literal("Hovering Deactivated"), true);
    }

    // SERVER SIDE METHODS
    public void getJetPackFuel() {
        jetPackFuelRemaining = 0.0f;
        BackPackNetworkHelper.updateJetPackFuel();
        jetPackFuelRemaining = JetpackState.getFuelLevel();
    }
    public void depleteJetPackFuel() {
        if (player.isCreative()) return;
        BackPackNetworkHelper.depleteJetPackFuel();
    }
    public class JetpackState {
        private static float fuelLevel = 0;
        private static boolean hasFlightUpgrade = false;

        public static synchronized float getFuelLevel() {
            return fuelLevel;
        }

        public static synchronized void setFuelLevel(float fuel) {
            fuelLevel = fuel;
        }

        public static synchronized boolean checkHasFlightUpgrade() {
            return hasFlightUpgrade;
        }

        public static synchronized void setHasFlightUpgrade(boolean result) {
            hasFlightUpgrade = result;
        }
    }

}
