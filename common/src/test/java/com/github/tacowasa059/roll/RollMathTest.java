package com.github.tacowasa059.roll;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RollMathTest {

    private static final float EPSILON = 1.0e-4f;

    @Test
    void advanceTurnsByDistanceOverRadius() {
        Quaternionf roll = new Quaternionf();

        assertTrue(RollMath.advance(roll, 0.5, 0.0, 0.25));

        // Half a block of travel on a 0.25 block radius sphere is 2 radians.
        assertEquals(2.0f, roll.angle(), EPSILON);
    }

    @Test
    void advanceRollsForwardsNotBackwards() {
        Quaternionf roll = new Quaternionf();
        float radius = 0.5f;

        // A quarter turn's worth of travel towards +X.
        RollMath.advance(roll, radius * Math.PI / 2.0, 0.0, radius);

        // The point that was on top of the sphere has to end up on the leading (+X) side.
        Vector3f top = roll.transform(new Vector3f(0.0f, radius, 0.0f));
        assertEquals(radius, top.x(), EPSILON);
        assertEquals(0.0f, top.y(), EPSILON);
        assertEquals(0.0f, top.z(), EPSILON);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 0, 1, 0",   // east: the top moves east
            "-1, 0, -1, 0", // west
            "0, 1, 0, 1",   // south
            "0, -1, 0, -1", // north
    })
    void advanceRollsTowardsTheDirectionOfTravel(double dx, double dz, float expectedX, float expectedZ) {
        Quaternionf roll = new Quaternionf();
        float radius = 0.5f;
        double quarterTurn = radius * Math.PI / 2.0;

        RollMath.advance(roll, dx * quarterTurn, dz * quarterTurn, radius);

        Vector3f top = roll.transform(new Vector3f(0.0f, radius, 0.0f));
        assertEquals(expectedX * radius, top.x(), EPSILON);
        assertEquals(expectedZ * radius, top.z(), EPSILON);
    }

    @Test
    void advanceAccumulatesAcrossFrames() {
        Quaternionf stepped = new Quaternionf();
        for (int i = 0; i < 10; i++) {
            RollMath.advance(stepped, 0.05, 0.0, 0.5);
        }

        Quaternionf single = new Quaternionf();
        RollMath.advance(single, 0.5, 0.0, 0.5);

        assertEquals(single.angle(), stepped.angle(), EPSILON);
    }

    @Test
    void advanceIgnoresStandingStill() {
        Quaternionf roll = new Quaternionf();

        assertFalse(RollMath.advance(roll, 0.0, 0.0, 0.5));
        assertEquals(0.0f, roll.angle(), EPSILON);
    }

    @ParameterizedTest
    @CsvSource({
            "128, 64",  // teleport
            "1.5, 0",   // a jump larger than anything one frame of movement can cover
            "0, -1.5",
    })
    void advanceIgnoresJumps(double dx, double dz) {
        Quaternionf roll = new Quaternionf();

        assertFalse(RollMath.advance(roll, dx, dz, 0.5));
        assertEquals(0.0f, roll.angle(), EPSILON);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, Double.NaN})
    void advanceIgnoresUnusableRadius(double radius) {
        Quaternionf roll = new Quaternionf();

        assertFalse(RollMath.advance(roll, 1.0, 0.0, radius));
        assertEquals(0.0f, roll.angle(), EPSILON);
    }

    @ParameterizedTest
    @CsvSource({
            "NaN, 0",
            "0, NaN",
            "Infinity, 0",
            "0, -Infinity",
    })
    void advanceIgnoresNonFiniteMovement(double dx, double dz) {
        Quaternionf roll = new Quaternionf();

        assertFalse(RollMath.advance(roll, dx, dz, 0.5));
        assertEquals(0.0f, roll.angle(), EPSILON);
    }

    @Test
    void advanceHandlesNullRoll() {
        assertFalse(RollMath.advance(null, 1.0, 0.0, 0.5));
    }

    @ParameterizedTest
    @ValueSource(floats = {0.0f, 45.0f, 90.0f, 180.0f, -137.5f})
    void toModelSpaceRotatesTheSameWayInTheWorld(float bodyRot) {
        Quaternionf worldRoll = new Quaternionf().fromAxisAngleRad(0.3f, 0.5f, 0.8f, 0.7f).normalize();
        Quaternionf modelRoll = RollMath.toModelSpace(worldRoll, bodyRot);
        Quaternionf modelToWorld = RollMath.modelToWorld(bodyRot);

        // Rolling in model space and then mapping to the world has to match rolling in the world.
        Vector3f modelPoint = new Vector3f(0.3f, -0.7f, 0.2f);
        Vector3f viaModel = modelToWorld.transform(modelRoll.transform(new Vector3f(modelPoint)));
        Vector3f viaWorld = worldRoll.transform(modelToWorld.transform(new Vector3f(modelPoint)));

        assertEquals(viaWorld.x(), viaModel.x(), EPSILON);
        assertEquals(viaWorld.y(), viaModel.y(), EPSILON);
        assertEquals(viaWorld.z(), viaModel.z(), EPSILON);
    }

    @Test
    void modelToWorldMatchesTheVanillaPoseSetup() {
        // With no body rotation, model space is the world flipped on X... and Y and Z: the renderer
        // turns 180 degrees around Y and then scales by (-1, -1, 1).
        Quaternionf modelToWorld = RollMath.modelToWorld(0.0f);

        Vector3f right = modelToWorld.transform(new Vector3f(1.0f, 0.0f, 0.0f));
        assertEquals(1.0f, right.x(), EPSILON);

        Vector3f down = modelToWorld.transform(new Vector3f(0.0f, 1.0f, 0.0f));
        assertEquals(-1.0f, down.y(), EPSILON);

        Vector3f back = modelToWorld.transform(new Vector3f(0.0f, 0.0f, 1.0f));
        assertEquals(-1.0f, back.z(), EPSILON);
    }
}
