package com.github.tacowasa059.render;

import org.joml.Quaternionf;

/**
 * Implemented on {@code SulfurCubeRenderState} by a Mixin so the roll worked out while extracting the
 * render state can be handed to the renderer, which only ever sees the state and not the entity.
 */
public interface RollHolder {

    /** The world-space roll of this frame, or null when the sphere should not roll. */
    Quaternionf sulfursphere$getRoll();

    void sulfursphere$setRoll(Quaternionf roll);
}
