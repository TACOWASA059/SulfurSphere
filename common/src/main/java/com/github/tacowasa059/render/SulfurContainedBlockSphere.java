package com.github.tacowasa059.render;

import com.mojang.blaze3d.platform.Transparency;
import com.github.tacowasa059.mixin.BlockModelRenderStateAccessor;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class SulfurContainedBlockSphere {
    private SulfurContainedBlockSphere() {
    }

    public static void apply(BlockModelRenderState containedBlock) {
        apply(containedBlock, null);
    }

    public static void apply(BlockModelRenderState containedBlock, BlockState sourceBlockState) {
        if (containedBlock == null || containedBlock.isEmpty()) {
            return;
        }

        BlockModelRenderStateAccessor accessor = (BlockModelRenderStateAccessor) (Object) containedBlock;
        accessor.sulfursphere$setSpecialRenderer(null);
        accessor.sulfursphere$setSpecialRendererTransformation(null);

        List<BlockStateModelPart> original = accessor.sulfursphere$getModelParts();
        if (original == null || original.isEmpty()) {
            if (sourceBlockState != null && rebuildModelPartsFromBlockState(containedBlock, sourceBlockState)) {
                original = accessor.sulfursphere$getModelParts();
            } else {
                SphereGeneratedModelPart fallback = createFallbackSphere();
                if (fallback != null) {
                    accessor.sulfursphere$setModelParts(List.of(fallback));
                }
                return;
            }
        }

        List<BlockStateModelPart> wrapped = new ArrayList<>(original.size());
        for (BlockStateModelPart part : original) {
            wrapped.add(new SphereBlockStateModelPart(part, computeBounds(part)));
        }
        accessor.sulfursphere$setModelParts(wrapped);
    }

    /**
     * Re-computes the contained block's tint layers using the biome at the Sulfur Cube's position.
     *
     * <p>The contained block is set up with {@code BlockDisplayContext}, so its tints come from the
     * biome-independent {@code color(state)} (e.g. {@code GrassColor.getDefaultColor()}); a grass
     * block carried inside the cube therefore renders with the flat default colour. Worse, the
     * rebuild path above clears the tint layers entirely, which leaves the grey grayscale grass
     * texture untinted. Recomputing the layers here with {@code colorInWorld} restores a proper,
     * biome-correct colour for grass, leaves, water and the like.</p>
     */
    public static void applyBiomeTints(BlockModelRenderState containedBlock, BlockState state, BlockAndTintGetter level, BlockPos pos) {
        if (containedBlock == null || state == null || level == null || pos == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }

        List<BlockTintSource> sources = minecraft.getBlockColors().getTintSources(state);
        if (sources.isEmpty()) {
            return;
        }

        IntList tintLayers = containedBlock.tintLayers();
        tintLayers.clear();
        for (BlockTintSource source : sources) {
            tintLayers.add(source.colorInWorld(state, level, pos));
        }
    }

    private static SphereBlockStateModelPart.Bounds computeBounds(BlockStateModelPart part) {
        List<BakedQuad> allQuads = new ArrayList<>();
        allQuads.addAll(part.getQuads(null));
        for (Direction direction : Direction.values()) {
            allQuads.addAll(part.getQuads(direction));
        }
        return SphereBlockStateModelPart.computeBounds(allQuads);
    }

    private static boolean rebuildModelPartsFromBlockState(BlockModelRenderState containedBlock, BlockState sourceBlockState) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return false;
        }
        BlockStateModelSet modelSet = minecraft.getModelManager().getBlockStateModelSet();
        if (modelSet == null) {
            return false;
        }
        BlockStateModel stateModel = modelSet.get(sourceBlockState);
        if (stateModel == null) {
            return false;
        }

        containedBlock.clear();
        List<BlockStateModelPart> parts = containedBlock.setupModel(new Matrix4f(), stateModel.hasMaterialFlag(1));
        stateModel.collectParts(containedBlock.scratchRandomSource(42L), parts);
        return parts != null && !parts.isEmpty();
    }

    private static SphereGeneratedModelPart createFallbackSphere() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return null;
        }

        AbstractTexture texture = minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        if (!(texture instanceof TextureAtlas atlas)) {
            return null;
        }

        TextureAtlasSprite sprite = atlas.missingSprite();
        Material.Baked particle = new Material.Baked(sprite, false);
        BakedQuad.MaterialInfo materialInfo = BakedQuad.MaterialInfo.of(
                particle,
                Transparency.TRANSLUCENT,
                -1,
                true,
                0
        );
        return new SphereGeneratedModelPart(true, particle, materialInfo.flags(), materialInfo);
    }
}
