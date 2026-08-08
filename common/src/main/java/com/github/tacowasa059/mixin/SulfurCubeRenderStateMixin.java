package com.github.tacowasa059.mixin;

import com.github.tacowasa059.render.RollHolder;
import net.minecraft.client.renderer.entity.state.SulfurCubeRenderState;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Carries the sphere's roll from {@code extractRenderState} through to the renderer.
 */
@Mixin(SulfurCubeRenderState.class)
public class SulfurCubeRenderStateMixin implements RollHolder {

    @Unique
    private Quaternionf sulfursphere$roll;

    @Override
    public Quaternionf sulfursphere$getRoll() {
        return this.sulfursphere$roll;
    }

    @Override
    public void sulfursphere$setRoll(Quaternionf roll) {
        this.sulfursphere$roll = roll;
    }
}
