package com.github.tacowasa059;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

// Client-only mod: only constructed on the physical client. The feature is purely visual (rendering).
@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class SulfurSphere {

    public SulfurSphere(IEventBus eventBus) {
        // Bootstrap the common code from the NeoForge client entry point.
        CommonClass.init();
    }
}
