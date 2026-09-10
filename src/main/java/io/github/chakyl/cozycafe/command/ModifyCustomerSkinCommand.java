package io.github.chakyl.cozycafe.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.client.SkinCache;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ModifyCustomerSkinCommand {

    private static int addUsername(CommandSourceStack commandSourceStack, String username) {
        List<String> customers = new ArrayList<>(CozyCafe.CONFIG.customerUsernames.get());
        if (!customers.contains(username)) {
            customers.add(username);
            CozyCafe.CONFIG.customerUsernames.set(customers);
            CozyCafe.CONFIG.customerUsernames.save();
            commandSourceStack.sendSuccess(() -> Component.translatable("command.cozycafe.add_user.success", username), true);
            SkinCache.preloadSkins();
            return 1;
        } else {
            commandSourceStack.sendFailure(Component.translatable("command.cozycafe.add_user.fail", username));
        }
        return -1;
    }

    private static int removeUsername(CommandSourceStack commandSourceStack, String username) {
        List<String> customers = new ArrayList<>(CozyCafe.CONFIG.customerUsernames.get());
        if (customers.contains(username)) {
            customers.remove(username);
            CozyCafe.CONFIG.customerUsernames.set(customers);
            CozyCafe.CONFIG.customerUsernames.save();
            commandSourceStack.sendSuccess(() -> Component.translatable("command.cozycafe.remove_user.success", username), true);
            return 1;
        } else {
            commandSourceStack.sendFailure(Component.translatable("command.cozycafe.remove_user.fail", username));
        }
        return -1;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cozycafe")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("addcustomerskin")
                        .then(Commands.argument("mc_username", StringArgumentType.string())
                                .executes(context -> addUsername(context.getSource(), StringArgumentType.getString(context, "mc_username")))
                        )
                ).then(Commands.literal("removecustomerskin")
                        .then(Commands.argument("mc_username", StringArgumentType.string())
                                .executes(context -> removeUsername(context.getSource(), StringArgumentType.getString(context, "mc_username")))
                        )
                )
        );
    }
}