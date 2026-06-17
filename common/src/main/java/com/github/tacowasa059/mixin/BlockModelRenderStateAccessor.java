package com.github.tacowasa059.mixin;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(BlockModelRenderState.class)
public interface BlockModelRenderStateAccessor {
    @Accessor("modelParts")
    List<BlockStateModelPart> sulfursphere$getModelParts();

    @Accessor("modelParts")
    void sulfursphere$setModelParts(List<BlockStateModelPart> modelParts);

    @Accessor("specialRenderer")
    SpecialModelRenderer<?> sulfursphere$getSpecialRenderer();

    @Accessor("specialRenderer")
    void sulfursphere$setSpecialRenderer(SpecialModelRenderer<?> specialRenderer);

    @Accessor("specialRendererTransformation")
    void sulfursphere$setSpecialRendererTransformation(Matrix4fc transformation);
}
