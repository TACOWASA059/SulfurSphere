package com.github.tacowasa059.render;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class SphereBlockStateModelPart implements BlockStateModelPart {
    private static final int SUBDIV = 10;

    private final BlockStateModelPart delegate;
    private final Bounds bounds;
    private final Map<Direction, List<BakedQuad>> byFaceCache = new EnumMap<>(Direction.class);
    private List<BakedQuad> nullFaceCache;

    public SphereBlockStateModelPart(BlockStateModelPart delegate, Bounds bounds) {
        this.delegate = delegate;
        this.bounds = bounds;
    }

    @Override
    public List<BakedQuad> getQuads(Direction direction) {
        if (!bounds.valid()) {
            return delegate.getQuads(direction);
        }
        if (direction == null) {
            if (this.nullFaceCache == null) {
                this.nullFaceCache = warp(delegate.getQuads(null), bounds);
            }
            return this.nullFaceCache;
        }
        return byFaceCache.computeIfAbsent(direction, face -> warp(delegate.getQuads(face), bounds));
    }

    @Override
    public boolean useAmbientOcclusion() {
        return delegate.useAmbientOcclusion();
    }

    @Override
    public Material.Baked particleMaterial() {
        return delegate.particleMaterial();
    }

    @Override
    public int materialFlags() {
        return delegate.materialFlags();
    }

    private static List<BakedQuad> warp(List<BakedQuad> source, Bounds bounds) {
        List<BakedQuad> out = new ArrayList<>(source.size() * SUBDIV * SUBDIV);
        for (BakedQuad quad : source) {
            subdivideAndWarp(quad, out, bounds);
        }
        return out;
    }

    private static void subdivideAndWarp(BakedQuad quad, List<BakedQuad> out, Bounds bounds) {
        Vector3fc p00 = quad.position0();
        Vector3fc p10 = quad.position1();
        Vector3fc p11 = quad.position2();
        Vector3fc p01 = quad.position3();

        float u00 = unpackU(quad.packedUV0());
        float v00 = unpackV(quad.packedUV0());
        float u10 = unpackU(quad.packedUV1());
        float v10 = unpackV(quad.packedUV1());
        float u11 = unpackU(quad.packedUV2());
        float v11 = unpackV(quad.packedUV2());
        float u01 = unpackU(quad.packedUV3());
        float v01 = unpackV(quad.packedUV3());

        for (int y = 0; y < SUBDIV; y++) {
            float t0 = (float) y / SUBDIV;
            float t1 = (float) (y + 1) / SUBDIV;
            for (int x = 0; x < SUBDIV; x++) {
                float s0 = (float) x / SUBDIV;
                float s1 = (float) (x + 1) / SUBDIV;

                Sample a = sample(p00, p10, p11, p01, u00, v00, u10, v10, u11, v11, u01, v01, s0, t0);
                Sample b = sample(p00, p10, p11, p01, u00, v00, u10, v10, u11, v11, u01, v01, s0, t1);
                Sample c = sample(p00, p10, p11, p01, u00, v00, u10, v10, u11, v11, u01, v01, s1, t1);
                Sample d = sample(p00, p10, p11, p01, u00, v00, u10, v10, u11, v11, u01, v01, s1, t0);

                // Keep the original 0->1->2->3 winding (00->10->11->01) to preserve face culling.
                out.add(new BakedQuad(
                        project(a.position, bounds.cx, bounds.cy, bounds.cz, bounds.rx, bounds.ry, bounds.rz),
                        project(d.position, bounds.cx, bounds.cy, bounds.cz, bounds.rx, bounds.ry, bounds.rz),
                        project(c.position, bounds.cx, bounds.cy, bounds.cz, bounds.rx, bounds.ry, bounds.rz),
                        project(b.position, bounds.cx, bounds.cy, bounds.cz, bounds.rx, bounds.ry, bounds.rz),
                        packUV(a.u, a.v),
                        packUV(d.u, d.v),
                        packUV(c.u, c.v),
                        packUV(b.u, b.v),
                        quad.direction(),
                        quad.materialInfo()
                ));
            }
        }
    }

    private static Sample sample(
            Vector3fc p00, Vector3fc p10, Vector3fc p11, Vector3fc p01,
            float u00, float v00, float u10, float v10, float u11, float v11, float u01, float v01,
            float s, float t
    ) {
        Vector3f top = new Vector3f(
                lerp(p00.x(), p10.x(), s),
                lerp(p00.y(), p10.y(), s),
                lerp(p00.z(), p10.z(), s)
        );
        Vector3f bottom = new Vector3f(
                lerp(p01.x(), p11.x(), s),
                lerp(p01.y(), p11.y(), s),
                lerp(p01.z(), p11.z(), s)
        );
        Vector3f pos = new Vector3f(
                lerp(top.x(), bottom.x(), t),
                lerp(top.y(), bottom.y(), t),
                lerp(top.z(), bottom.z(), t)
        );

        float topU = lerp(u00, u10, s);
        float topV = lerp(v00, v10, s);
        float bottomU = lerp(u01, u11, s);
        float bottomV = lerp(v01, v11, s);

        return new Sample(
                pos,
                lerp(topU, bottomU, t),
                lerp(topV, bottomV, t)
        );
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static Vector3fc project(Vector3fc in, float cx, float cy, float cz, float rx, float ry, float rz) {
        float nx = (in.x() - cx) / rx;
        float ny = (in.y() - cy) / ry;
        float nz = (in.z() - cz) / rz;

        Vector3f n = new Vector3f(nx, ny, nz);
        if (n.lengthSquared() < 1.0e-8f) {
            n.set(0f, 1f, 0f);
        } else {
            n.normalize();
        }

        // Keep visible spherical silhouette.
        float r = Math.max(rx, Math.max(ry, rz)) * 1.08f;
        return new Vector3f(
                cx + n.x() * r,
                cy + n.y() * r,
                cz + n.z() * r
        );
    }

    private static float unpackU(long packedUv) {
        return Float.intBitsToFloat((int) packedUv);
    }

    private static float unpackV(long packedUv) {
        return Float.intBitsToFloat((int) (packedUv >>> 32));
    }

    private static long packUV(float u, float v) {
        long lo = Integer.toUnsignedLong(Float.floatToRawIntBits(u));
        long hi = Integer.toUnsignedLong(Float.floatToRawIntBits(v)) << 32;
        return hi | lo;
    }

    private record Sample(Vector3fc position, float u, float v) {
    }

    public static Bounds computeBounds(List<BakedQuad> quads) {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;

        for (BakedQuad quad : quads) {
            Vector3fc[] vs = new Vector3fc[]{quad.position0(), quad.position1(), quad.position2(), quad.position3()};
            for (Vector3fc v : vs) {
                minX = Math.min(minX, v.x());
                minY = Math.min(minY, v.y());
                minZ = Math.min(minZ, v.z());
                maxX = Math.max(maxX, v.x());
                maxY = Math.max(maxY, v.y());
                maxZ = Math.max(maxZ, v.z());
            }
        }

        float rx = (maxX - minX) * 0.5f;
        float ry = (maxY - minY) * 0.5f;
        float rz = (maxZ - minZ) * 0.5f;
        return new Bounds(
                (minX + maxX) * 0.5f,
                (minY + maxY) * 0.5f,
                (minZ + maxZ) * 0.5f,
                Math.max(rx, 1.0e-6f),
                Math.max(ry, 1.0e-6f),
                Math.max(rz, 1.0e-6f)
        );
    }

    public record Bounds(float cx, float cy, float cz, float rx, float ry, float rz) {
        boolean valid() {
            return Float.isFinite(cx) && Float.isFinite(cy) && Float.isFinite(cz);
        }
    }
}
