package com.github.tacowasa059;

import com.github.tacowasa059.command.SulfurSphereCommands;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

// Client-only mod: only runs on the physical client. The feature is purely visual (rendering).
public class SulfurSphere implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Bootstrap the common code from the Fabric client entry point.
        CommonClass.init();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(SulfurSphereCommands.<FabricClientCommandSource>build(
                        (source, message) -> source.sendFeedback(message))));
    }
}
