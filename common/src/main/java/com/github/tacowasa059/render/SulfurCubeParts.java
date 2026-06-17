package com.github.tacowasa059.render;

import com.github.tacowasa059.mixin.ModelPartAccessor;
import net.minecraft.client.model.geom.ModelPart;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Registry of the {@link ModelPart.Cube} instances that make up the Sulfur Cube body model.
 *
 * <p>Each time a {@code SulfurCubeModel} is baked its cubes are brand-new instances, so we record them
 * here (by identity, with weak keys so old models from previous resource reloads can be garbage
 * collected). {@code CubeMixin} then warps a cube into a sphere only when it is one of these, leaving
 * every other model in the game untouched.</p>
 */
public final class SulfurCubeParts {
    private static final Set<ModelPart.Cube> CUBES =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private SulfurCubeParts() {
    }

    public static void register(ModelPart root) {
        if (root == null) {
            return;
        }
        addCubes(root);
        for (ModelPart part : root.getAllParts()) {
            addCubes(part);
        }
    }

    private static void addCubes(ModelPart part) {
        List<ModelPart.Cube> cubes = ((ModelPartAccessor) (Object) part).sulfursphere$getCubes();
        if (cubes != null && !cubes.isEmpty()) {
            CUBES.addAll(cubes);
        }
    }

    public static boolean contains(ModelPart.Cube cube) {
        return CUBES.contains(cube);
    }
}
