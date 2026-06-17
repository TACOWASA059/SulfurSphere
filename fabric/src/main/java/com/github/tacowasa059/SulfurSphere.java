package com.github.tacowasa059;

import net.fabricmc.api.ClientModInitializer;

// Client-only mod: only runs on the physical client. The feature is purely visual (rendering).
public class SulfurSphere implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Bootstrap the common code from the Fabric client entry point.
        CommonClass.init();
    }
}
