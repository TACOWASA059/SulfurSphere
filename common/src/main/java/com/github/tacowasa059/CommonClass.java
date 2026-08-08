package com.github.tacowasa059;

import com.github.tacowasa059.config.SulfurSphereConfig;
import com.github.tacowasa059.platform.Services;

// This class is part of the common project meaning it is shared between all supported loaders. The actual feature of
// this mod (warping the Sulfur Cube into a sphere) lives entirely in client-side Mixins that target vanilla classes,
// so it is loader-agnostic and also resides in the common project. See com.github.tacowasa059.mixin.* and
// com.github.tacowasa059.render.* . This entry point only logs that the mod has loaded.
public class CommonClass {

    public static void init() {
        SulfurSphereConfig.load();
        Constants.LOG.info("{} loaded on {} ({} environment).",
                Constants.MOD_NAME,
                Services.PLATFORM.getPlatformName(),
                Services.PLATFORM.getEnvironmentName());
    }
}
