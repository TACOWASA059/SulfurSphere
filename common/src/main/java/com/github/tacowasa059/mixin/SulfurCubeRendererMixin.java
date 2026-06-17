package com.github.tacowasa059.mixin;

import com.github.tacowasa059.render.SulfurContainedBlockSphere;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.SulfurCubeRenderer;
import net.minecraft.client.renderer.entity.state.SulfurCubeRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SulfurCubeRenderer.class)
public class SulfurCubeRendererMixin {
    @Shadow
    private BlockModelResolver blockModelResolver;

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/monster/cubemob/SulfurCube;Lnet/minecraft/client/renderer/entity/state/SulfurCubeRenderState;F)V",
            at = @At("TAIL")
    )
    private void sulfursphere$warpContainedBlock(SulfurCube sulfurCube, SulfurCubeRenderState state, float partialTick, CallbackInfo ci) {
        if (state == null || state.containedBlock == null || state.containedBlock.isEmpty()) {
            return;
        }

        BlockState blockState = null;
        ItemStack armor = sulfurCube.getBodyArmorItem();
        if (!armor.isEmpty()) {
            blockState = Block.byItem(armor.getItem()).defaultBlockState();
        }

        BlockModelRenderStateAccessor accessor = (BlockModelRenderStateAccessor) (Object) state.containedBlock;
        if (accessor.sulfursphere$getModelParts() == null || accessor.sulfursphere$getModelParts().isEmpty()) {
            if (blockState != null) {
                this.blockModelResolver.update(
                        state.containedBlock,
                        blockState,
                        ItemFrameRenderer.BLOCK_DISPLAY_CONTEXT
                );
            }
        }
        SulfurContainedBlockSphere.apply(state.containedBlock, blockState);
    }
}
