package com.github.tacowasa059.roll;

import org.joml.Quaternionf;

/**
 * The maths behind rolling the Sulfur Cube's sphere along the ground.
 *
 * <p>Deliberately free of any Minecraft type so it can be unit tested on its own.</p>
 */
public final class RollMath {

    /** Squared movement below this (in blocks) counts as standing still. */
    private static final double MIN_DISTANCE_SQUARED = 1.0e-10;

    /**
     * Movement further than this in a single frame is a teleport - or an entity coming back into view
     * after a while - rather than rolling, and would spin the sphere wildly.
     */
    private static final double MAX_DISTANCE = 1.0;

    private RollMath() {
    }

    /**
     * Advances a world-space roll by one frame of horizontal movement.
     *
     * <p>Rolling without slipping turns the sphere by {@code distance / radius} radians around the
     * horizontal axis perpendicular to the direction of travel, which is {@code up × direction}.</p>
     *
     * @param roll   the accumulated world-space rotation, updated in place.
     * @param dx     movement along the X axis since the previous frame, in blocks.
     * @param dz     movement along the Z axis since the previous frame, in blocks.
     * @param radius the radius of the sphere, in blocks.
     * @return true if the roll was advanced.
     */
    public static boolean advance(Quaternionf roll, double dx, double dz, double radius) {
        if (roll == null || !(radius > 0.0) || !Double.isFinite(dx) || !Double.isFinite(dz)) {
            return false;
        }

        double distanceSquared = dx * dx + dz * dz;
        if (distanceSquared < MIN_DISTANCE_SQUARED || distanceSquared > MAX_DISTANCE * MAX_DISTANCE) {
            return false;
        }

        double distance = Math.sqrt(distanceSquared);
        Quaternionf delta = new Quaternionf().fromAxisAngleRad(
                (float) (dz / distance),
                0.0f,
                (float) (-dx / distance),
                (float) (distance / radius)
        );
        roll.premul(delta).normalize();
        return true;
    }

    /**
     * Converts a world-space roll into the entity model's own space.
     *
     * <p>{@code LivingEntityRenderer} rotates the pose by {@code Axis.YP.rotationDegrees(180 - bodyRot)}
     * and then flips it with {@code scale(-1, -1, 1)} - a 180° turn around Z - so model space maps to
     * world space through {@code rotY(180 - bodyRot) * rotZ(180)}. A world rotation {@code R} therefore
     * has to be applied as {@code M⁻¹ R M} once the pose is in model space.</p>
     */
    public static Quaternionf toModelSpace(Quaternionf worldRoll, float bodyRot) {
        Quaternionf modelToWorld = modelToWorld(bodyRot);
        return new Quaternionf(modelToWorld).conjugate().mul(worldRoll).mul(modelToWorld);
    }

    /** The rotation that maps entity model space onto world space. */
    public static Quaternionf modelToWorld(float bodyRot) {
        return new Quaternionf()
                .rotationY((float) Math.toRadians(180.0f - bodyRot))
                .mul(new Quaternionf().rotationZ((float) Math.PI));
    }
}
