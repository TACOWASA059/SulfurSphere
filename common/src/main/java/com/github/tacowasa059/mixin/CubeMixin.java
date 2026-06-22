package com.github.tacowasa059.mixin;

import com.github.tacowasa059.render.SulfurCubeParts;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
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

                    // The body sheet is drawn with a no-cull, depth-writing translucent pipeline. Without
                    // culling we emit both the near and far halves of the sphere, and the per-quad sort
                    // across the hundreds of patches then blends them inconsistently - that double layer
                    // is the "bugged" opacity on the outer shell. Skip the patches that face away from the
                    // camera so a single clean translucent layer is drawn.
                    if (isBackFacing(pose, v1, v2, v3, v4)) {
                        continue;
                    }

                    // Quad-based compile path: emit 4 vertices per patch, preserving the original face
                    // winding (s,t): (lo,lo)->(hi,lo)->(hi,hi)->(lo,hi), i.e. v1 -> v4 -> v3 -> v2, the
                    // same winding the contained-block sphere keeps.
                    add(builder, pose, v1, light, overlay, color);
                    add(builder, pose, v4, light, overlay, color);
                    add(builder, pose, v3, light, overlay, color);
                    add(builder, pose, v2, light, overlay, color);
                }
            }
        }

        ci.cancel();
    }

    // Patch faces away from the camera (and can be skipped). The pose is camera-relative - the camera
    // sits at the origin - so a patch faces the viewer when its outward normal points back toward the
    // origin, i.e. dot(normal, position) < 0. This depends only on the camera position, so it is correct
    // regardless of camera orientation. Each vertex array is [x, y, z, u, v, nx, ny, nz].
    @Unique
    private static boolean isBackFacing(PoseStack.Pose pose, float[] a, float[] b, float[] c, float[] d) {
        float mx = (a[0] + b[0] + c[0] + d[0]) * 0.25f;
        float my = (a[1] + b[1] + c[1] + d[1]) * 0.25f;
        float mz = (a[2] + b[2] + c[2] + d[2]) * 0.25f;
        float nx = a[5] + b[5] + c[5] + d[5];
        float ny = a[6] + b[6] + c[6] + d[6];
        float nz = a[7] + b[7] + c[7] + d[7];

        Vector3f posCam = pose.pose().transformPosition(mx, my, mz, new Vector3f());
        Vector3f normCam = pose.transformNormal(nx, ny, nz, new Vector3f());
        return normCam.dot(posCam) > 0.0f;
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
                            PoseStack.Pose pose,
                            float[] vertex,
                            int light,
                            int overlay,
                            int color) {

        Vector3f pos = pose.pose().transformPosition(vertex[0], vertex[1], vertex[2], new Vector3f());
        // Transform the model-space sphere normal into the pose's space (matching vanilla
        // ModelPart$Cube.compile), otherwise the shading is computed against un-rotated normals and the
        // lit side does not follow the world light direction when the cube turns or squishes.
        Vector3f normal = pose.transformNormal(vertex[5], vertex[6], vertex[7], new Vector3f());

        builder.addVertex(
                pos.x(), pos.y(), pos.z(),
                color,
                vertex[3], vertex[4],
                overlay,
                light,
                normal.x(), normal.y(), normal.z()
        );
    }

    @Unique
    private boolean shouldReplace() {
        // Only warp cubes that belong to the Sulfur Cube body model, leaving all other models untouched.
        return SulfurCubeParts.contains((ModelPart.Cube) (Object) this);
    }
}
