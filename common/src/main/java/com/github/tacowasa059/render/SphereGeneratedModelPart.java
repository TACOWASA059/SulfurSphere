package com.github.tacowasa059.render;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class SphereGeneratedModelPart implements BlockStateModelPart {
    private static final int STACKS = 12;
    private static final int SLICES = 24;
    private static final float RADIUS = 0.54f;

    private final boolean ambientOcclusion;
    private final Material.Baked particle;
    private final int materialFlags;
    private final List<BakedQuad> allQuads;
    private final Map<Direction, List<BakedQuad>> byFace;

    public SphereGeneratedModelPart(boolean ambientOcclusion, Material.Baked particle, int materialFlags, MaterialInfo materialInfo) {
        this.ambientOcclusion = ambientOcclusion;
        this.particle = particle;
        this.materialFlags = materialFlags;
        this.byFace = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            this.byFace.put(direction, new ArrayList<>());
        }
        this.allQuads = buildSphere(materialInfo, this.byFace);
    }

    @Override
    public List<BakedQuad> getQuads(Direction direction) {
        if (direction == null) {
            return this.allQuads;
        }
        return this.byFace.get(direction);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.ambientOcclusion;
    }

    @Override
    public Material.Baked particleMaterial() {
        return this.particle;
    }

    @Override
    public int materialFlags() {
        return this.materialFlags;
    }

    private static List<BakedQuad> buildSphere(MaterialInfo materialInfo, Map<Direction, List<BakedQuad>> byFace) {
        List<BakedQuad> out = new ArrayList<>(STACKS * SLICES);
        for (int i = 0; i < STACKS; i++) {
            float v0 = (float) i / STACKS;
            float v1 = (float) (i + 1) / STACKS;
            float t0 = (float) (Math.PI * v0);
            float t1 = (float) (Math.PI * v1);

            for (int j = 0; j < SLICES; j++) {
                float u0 = (float) j / SLICES;
                float u1 = (float) (j + 1) / SLICES;
                float p0 = (float) (Math.PI * 2.0 * u0);
                float p1 = (float) (Math.PI * 2.0 * u1);

                Vector3f a = spherePoint(t0, p0);
                Vector3f b = spherePoint(t1, p0);
                Vector3f c = spherePoint(t1, p1);
                Vector3f d = spherePoint(t0, p1);

                Direction direction = dominantDirection(a, b, c);
                BakedQuad quad = new BakedQuad(
                        a, b, c, d,
                        packUV(u0, v0),
                        packUV(u0, v1),
                        packUV(u1, v1),
                        packUV(u1, v0),
                        direction,
                        materialInfo
                );
                out.add(quad);
                byFace.get(direction).add(quad);
            }
        }
        return out;
    }

    private static Direction dominantDirection(Vector3f a, Vector3f b, Vector3f c) {
        Vector3f ab = new Vector3f(b).sub(a);
        Vector3f ac = new Vector3f(c).sub(a);
        Vector3f n = ab.cross(ac);
        float ax = Math.abs(n.x());
        float ay = Math.abs(n.y());
        float az = Math.abs(n.z());
        if (ax >= ay && ax >= az) {
            return n.x() >= 0f ? Direction.EAST : Direction.WEST;
        }
        if (ay >= ax && ay >= az) {
            return n.y() >= 0f ? Direction.UP : Direction.DOWN;
        }
        return n.z() >= 0f ? Direction.SOUTH : Direction.NORTH;
    }

    private static Vector3f spherePoint(float theta, float phi) {
        float x = (float) (Math.sin(theta) * Math.cos(phi));
        float y = (float) Math.cos(theta);
        float z = (float) (Math.sin(theta) * Math.sin(phi));
        return new Vector3f(
                0.5f + x * RADIUS,
                0.5f + y * RADIUS,
                0.5f + z * RADIUS
        );
    }

    private static long packUV(float u, float v) {
        long lo = Integer.toUnsignedLong(Float.floatToRawIntBits(u));
        long hi = Integer.toUnsignedLong(Float.floatToRawIntBits(v)) << 32;
        return hi | lo;
    }
}
