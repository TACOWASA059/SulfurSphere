package com.github.tacowasa059.mixin;

import com.github.tacowasa059.render.SulfurCubeParts;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.slime.SulfurCubeModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Records the cubes of every freshly-built Sulfur Cube model so {@code CubeMixin} can recognise them.
 */
@Mixin(SulfurCubeModel.class)
public class SulfurCubeModelMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void sulfursphere$collectCubes(ModelPart root, CallbackInfo ci) {
        SulfurCubeParts.register(root);
    }
}
