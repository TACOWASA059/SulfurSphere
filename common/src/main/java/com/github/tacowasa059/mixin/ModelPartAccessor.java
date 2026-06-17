package com.github.tacowasa059.mixin;

import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Exposes the private {@code cubes} list of a {@link ModelPart} so we can identify exactly which
 * {@link ModelPart.Cube} instances belong to the Sulfur Cube model.
 */
@Mixin(ModelPart.class)
public interface ModelPartAccessor {
    @Accessor("cubes")
    List<ModelPart.Cube> sulfursphere$getCubes();
}
