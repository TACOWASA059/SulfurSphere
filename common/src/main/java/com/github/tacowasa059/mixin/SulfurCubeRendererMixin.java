package com.github.tacowasa059.mixin;

import com.github.tacowasa059.config.SulfurSphereConfig;
import com.github.tacowasa059.render.RollHolder;
import com.github.tacowasa059.render.SphereRoll;
import com.github.tacowasa059.render.SulfurContainedBlockSphere;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.SulfurCubeRenderer;
import net.minecraft.client.renderer.entity.state.SulfurCubeRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;
import org.joml.Quaternionf;
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
        if (state == null) {
            return;
        }
        if (state.containedBlock == null || state.containedBlock.isEmpty()) {
            // Only a cube carrying a block rolls, so an empty one never keeps a roll around.
            SphereRoll.update(sulfurCube, partialTick, false);
            ((RollHolder) (Object) state).sulfursphere$setRoll(null);
            return;
        }
        ((RollHolder) (Object) state).sulfursphere$setRoll(
                SphereRoll.update(sulfurCube, partialTick, SulfurSphereConfig.roll()));

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

        // Tint the contained block with the biome colour at the cube's position so that grass blocks
        // (and other biome-tinted blocks) are coloured instead of showing their grey grayscale texture.
        if (blockState != null && sulfurCube.level() instanceof BlockAndTintGetter tintGetter) {
            SulfurContainedBlockSphere.applyBiomeTints(
                    state.containedBlock,
                    blockState,
                    tintGetter,
                    sulfurCube.blockPosition()
            );
        }
    }

    /**
     * Rolls the sphere along the ground. Injected at the tail of {@code scale} because the pose is
     * still in the entity model's own space there, and everything drawn afterwards - the outer shell
     * and the block held inside it - picks the rotation up.
     */
    @Inject(
            method = "scale(Lnet/minecraft/client/renderer/entity/state/SulfurCubeRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            at = @At("TAIL")
    )
    private void sulfursphere$rollSphere(SulfurCubeRenderState state, PoseStack poseStack, CallbackInfo ci) {
        Quaternionf roll = ((RollHolder) (Object) state).sulfursphere$getRoll();
        if (roll != null) {
            SphereRoll.apply(poseStack, roll, state.bodyRot);
        }
    }
}
