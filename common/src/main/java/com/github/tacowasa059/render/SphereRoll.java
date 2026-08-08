package com.github.tacowasa059.render;

import com.github.tacowasa059.roll.RollMath;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Tracks how far each Sulfur Cube has rolled and applies that roll to the render pose.
 *
 * <p>The accumulated rotation is kept per entity (weakly, so cubes that leave the world can be garbage
 * collected) because the render state is rebuilt from scratch every frame.</p>
 */
public final class SphereRoll {

    /**
     * Where the sphere's centre sits at the point {@code SulfurCubeRenderer.scale} returns:
     * {@code LivingEntityRenderer.submit} translates by this much right afterwards and the body model
     * is a cube centred on its own origin, so the rotation has to happen around that point.
     */
    private static final float CENTRE_Y = -1.501F;

    /** Never divide by a radius smaller than this, however tiny the entity is. */
    private static final float MIN_RADIUS = 0.05F;

    private static final Map<Entity, Tracker> TRACKERS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private SphereRoll() {
    }

    /**
     * Advances the entity's roll by the distance it moved since the previous frame.
     *
     * <p>An entity that is not rolling - because it carries no block, or because rolling is turned off -
     * is still followed, so that it picks up where it left off instead of spinning to catch up.</p>
     *
     * @return the accumulated world-space rotation of that entity's sphere, or null if it is not rolling.
     */
    public static Quaternionf update(Entity entity, float partialTick, boolean rolling) {
        double x = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double z = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        Tracker tracker = TRACKERS.computeIfAbsent(entity, key -> new Tracker(x, z));
        Quaternionf roll = tracker.advance(x, z, Math.max(entity.getBbWidth() * 0.5F, MIN_RADIUS), rolling);
        return rolling ? roll : null;
    }

    /** Rotates the pose around the sphere's centre by the given world-space roll. */
    public static void apply(PoseStack poseStack, Quaternionf worldRoll, float bodyRot) {
        poseStack.translate(0.0F, CENTRE_Y, 0.0F);
        poseStack.mulPose(RollMath.toModelSpace(worldRoll, bodyRot));
        poseStack.translate(0.0F, -CENTRE_Y, 0.0F);
    }

    private static final class Tracker {

        private final Quaternionf roll = new Quaternionf();
        private double lastX;
        private double lastZ;

        private Tracker(double x, double z) {
            this.lastX = x;
            this.lastZ = z;
        }

        private Quaternionf advance(double x, double z, double radius, boolean rolling) {
            if (rolling) {
                RollMath.advance(this.roll, x - this.lastX, z - this.lastZ, radius);
            }
            this.lastX = x;
            this.lastZ = z;
            return this.roll;
        }
    }
}
