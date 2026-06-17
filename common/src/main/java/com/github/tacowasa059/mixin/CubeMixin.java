package com.github.tacowasa059.mixin;

import com.github.tacowasa059.render.SulfurCubeParts;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.model.geom.ModelPart$Cube")
public class CubeMixin {

    @Shadow public ModelPart.Polygon[] polygons;
    @Shadow public float minX;
    @Shadow public float minY;
    @Shadow public float minZ;
    @Shadow public float maxX;
    @Shadow public float maxY;
    @Shadow public float maxZ;

    @Inject(method = "compile", at = @At("HEAD"), cancellable = true)
    private void renderSphere(PoseStack.Pose pose,
                              VertexConsumer builder,
                              int light,
                              int overlay,
                              int color,
                              CallbackInfo ci) {

        if (!shouldReplace()) return;

        Matrix4f matrix = pose.pose();

        float cx = (minX + maxX) / 32f;
        float cy = (minY + maxY) / 32f;
        float cz = (minZ + maxZ) / 32f;

        float rx = (maxX - minX) / 32f;
        float ry = (maxY - minY) / 32f;
        float rz = (maxZ - minZ) / 32f;

        int faceSteps = 8;
        for (ModelPart.Polygon polygon : polygons) {
            ModelPart.Vertex[] vertices = polygon.vertices();
            if (vertices.length != 4) {
                continue;
            }

            float[] p00 = toControlVertex(vertices[0]);
            float[] p10 = toControlVertex(vertices[1]);
            float[] p11 = toControlVertex(vertices[2]);
            float[] p01 = toControlVertex(vertices[3]);

            for (int y = 0; y < faceSteps; y++) {
                float t1 = (float) y / faceSteps;
                float t2 = (float) (y + 1) / faceSteps;
                for (int x = 0; x < faceSteps; x++) {
                    float s1 = (float) x / faceSteps;
                    float s2 = (float) (x + 1) / faceSteps;

                    float[] v1 = sampleSphereVertex(p00, p10, p11, p01, s1, t1, cx, cy, cz, rx, ry, rz);
                    float[] v2 = sampleSphereVertex(p00, p10, p11, p01, s1, t2, cx, cy, cz, rx, ry, rz);
                    float[] v3 = sampleSphereVertex(p00, p10, p11, p01, s2, t2, cx, cy, cz, rx, ry, rz);
                    float[] v4 = sampleSphereVertex(p00, p10, p11, p01, s2, t1, cx, cy, cz, rx, ry, rz);

                    // ModelPart compile path is quad-based: emit 4 vertices per patch.
                    add(builder, matrix, v1, light, overlay, color);
                    add(builder, matrix, v2, light, overlay, color);
                    add(builder, matrix, v3, light, overlay, color);
                    add(builder, matrix, v4, light, overlay, color);
                }
            }
        }

        ci.cancel();
    }

    // [x, y, z, u, v]
    @Unique
    private static float[] toControlVertex(ModelPart.Vertex vertex) {
        return new float[] {
                vertex.worldX(),
                vertex.worldY(),
                vertex.worldZ(),
                vertex.u(),
                vertex.v()
        };
    }

    @Unique
    private static float[] sampleSphereVertex(float[] p00, float[] p10, float[] p11, float[] p01,
                                              float s, float t,
                                              float cx, float cy, float cz,
                                              float rx, float ry, float rz) {
        float topX = lerp(p00[0], p10[0], s);
        float topY = lerp(p00[1], p10[1], s);
        float topZ = lerp(p00[2], p10[2], s);
        float bottomX = lerp(p01[0], p11[0], s);
        float bottomY = lerp(p01[1], p11[1], s);
        float bottomZ = lerp(p01[2], p11[2], s);

        float x = lerp(topX, bottomX, t);
        float y = lerp(topY, bottomY, t);
        float z = lerp(topZ, bottomZ, t);

        float nx = safeDiv(x - cx, rx);
        float ny = safeDiv(y - cy, ry);
        float nz = safeDiv(z - cz, rz);

        Vector3f normal = new Vector3f(nx, ny, nz);
        if (normal.lengthSquared() < 1.0e-8f) {
            normal.set(0f, 1f, 0f);
        } else {
            normal.normalize();
        }

        float sx = cx + normal.x() * rx;
        float sy = cy + normal.y() * ry;
        float sz = cz + normal.z() * rz;

        float topU = lerp(p00[3], p10[3], s);
        float topV = lerp(p00[4], p10[4], s);
        float bottomU = lerp(p01[3], p11[3], s);
        float bottomV = lerp(p01[4], p11[4], s);

        return new float[] {
                sx,
                sy,
                sz,
                lerp(topU, bottomU, t),
                lerp(topV, bottomV, t),
                normal.x(),
                normal.y(),
                normal.z()
        };
    }

    @Unique
    private static float safeDiv(float value, float divisor) {
        if (Math.abs(divisor) < 1.0e-6f) {
            return 0f;
        }
        return value / divisor;
    }

    @Unique
    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    @Unique
    private static void add(VertexConsumer builder,
                            Matrix4f matrix,
                            float[] vertex,
                            int light,
                            int overlay,
                            int color) {

        Vector3f pos = matrix.transformPosition(vertex[0], vertex[1], vertex[2], new Vector3f());

        builder.addVertex(
                pos.x(), pos.y(), pos.z(),
                color,
                vertex[3], vertex[4],
                overlay,
                light,
                vertex[5], vertex[6], vertex[7]
        );
    }

    @Unique
    private boolean shouldReplace() {
        // Only warp cubes that belong to the Sulfur Cube body model, leaving all other models untouched.
        return SulfurCubeParts.contains((ModelPart.Cube) (Object) this);
    }
}
