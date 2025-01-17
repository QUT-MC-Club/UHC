package com.hugman.uhc.command;

import com.hugman.uhc.config.UHCConfig;
import com.hugman.uhc.module.Module;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;

import java.util.Objects;

public class ModulesCommand {
    public static final SimpleCommandExceptionType NO_MODULES_ACTIVATED = new SimpleCommandExceptionType(Text.translatable("command.uhc.modules.no_modules_activated"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("modules")
                        .requires(ModulesCommand::isSourceInUHC)
                        .executes(ModulesCommand::displayModules));
    }

    public static boolean isSourceInUHC(ServerCommandSource source) {
        GameSpace gameSpace = GameSpaceManager.get().byWorld(source.getWorld());
        if (gameSpace != null) {
            return gameSpace.getMetadata().sourceConfig().value().config() instanceof UHCConfig;
        }
        return false;
    }

    private static int displayModules(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        RegistryEntryList<Module> moduleEntries = ((UHCConfig) Objects.requireNonNull(GameSpaceManager.get().byWorld(source.getWorld())).getMetadata().sourceConfig().value().config()).modules();
        if (moduleEntries.size() != 0) {
            ScreenHandlerType<?> type = Registries.SCREEN_HANDLER.get(Identifier.of("generic_9x" + MathHelper.clamp(1, MathHelper.ceil((float) moduleEntries.size() / 9), 6)));
            SimpleGui gui = new SimpleGui(type, source.getPlayer(), false);
            gui.setTitle(Text.translatable("ui.uhc.modules.title"));
            int i = 0;
            for (var moduleEntry : moduleEntries) {
                var module = moduleEntry.value();
                GuiElementBuilder elementBuilder = new GuiElementBuilder(module.icon())
                        .setName(Text.translatable(module.translation()).formatted(Formatting.BOLD).setStyle(Style.EMPTY.withColor(module.color())))
                        .hideDefaultTooltip();
                for (String s : module.getDescriptionLines()) {
                    elementBuilder.addLoreLine(Text.literal("- ").append(Text.translatable(s)).formatted(Formatting.GRAY));
                }
                gui.setSlot(i++, elementBuilder);
            }
            gui.open();
            return Command.SINGLE_SUCCESS;
        } else {
            throw NO_MODULES_ACTIVATED.create();
        }
    }
}
