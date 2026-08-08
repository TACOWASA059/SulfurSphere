package com.github.tacowasa059.command;

import com.github.tacowasa059.Constants;
import com.github.tacowasa059.config.SulfurSphereConfig;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.network.chat.Component;

import java.util.function.BiConsumer;

/**
 * The {@code /sulfursphere} client command.
 *
 * <p>Built generically over the command source so each loader can register it with its own client
 * command source type.</p>
 */
public final class SulfurSphereCommands {

    private static final String ROLL_ARGUMENT = "enabled";

    private SulfurSphereCommands() {
    }

    /**
     * Builds {@code /sulfursphere roll [true|false]} - without an argument it reports the current
     * setting, with one it changes the setting and saves the config.
     *
     * @param feedback how the loader sends a message back to the player.
     */
    public static <S> LiteralArgumentBuilder<S> build(BiConsumer<S, Component> feedback) {
        return LiteralArgumentBuilder.<S>literal(Constants.MOD_ID)
                .then(LiteralArgumentBuilder.<S>literal("roll")
                        .executes(context -> {
                            feedback.accept(context.getSource(), status(SulfurSphereConfig.roll()));
                            return 1;
                        })
                        .then(RequiredArgumentBuilder.<S, Boolean>argument(ROLL_ARGUMENT, BoolArgumentType.bool())
                                .executes(context -> {
                                    boolean enabled = BoolArgumentType.getBool(context, ROLL_ARGUMENT);
                                    SulfurSphereConfig.setRoll(enabled);
                                    feedback.accept(context.getSource(), status(enabled));
                                    return 1;
                                })));
    }

    private static Component status(boolean enabled) {
        return enabled
                ? Component.translatableWithFallback("commands.sulfursphere.roll.on", "Sulfur Cube rolling: ON")
                : Component.translatableWithFallback("commands.sulfursphere.roll.off", "Sulfur Cube rolling: OFF");
    }
}
