package com.github.tacowasa059;

import com.github.tacowasa059.command.SulfurSphereCommands;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

// Client-only mod: only constructed on the physical client. The feature is purely visual (rendering).
@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class SulfurSphere {

    public SulfurSphere(IEventBus eventBus) {
        // Bootstrap the common code from the NeoForge client entry point.
        CommonClass.init();

        NeoForge.EVENT_BUS.addListener(SulfurSphere::registerClientCommands);
    }

    private static void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(SulfurSphereCommands.<CommandSourceStack>build(
                (source, message) -> source.sendSuccess(() -> message, false)));
    }
}
