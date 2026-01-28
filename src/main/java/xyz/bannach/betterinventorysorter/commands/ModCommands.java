package xyz.bannach.betterinventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import xyz.bannach.betterinventorysorter.Betterinventorysorter;
import xyz.bannach.betterinventorysorter.Config;
import xyz.bannach.betterinventorysorter.ModAttachments;
import xyz.bannach.betterinventorysorter.network.SyncPreferencePayload;
import xyz.bannach.betterinventorysorter.server.SortHandler;
import xyz.bannach.betterinventorysorter.sorting.ItemSorter;
import xyz.bannach.betterinventorysorter.sorting.SortMethod;
import xyz.bannach.betterinventorysorter.sorting.SortOrder;
import xyz.bannach.betterinventorysorter.sorting.SortPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@EventBusSubscriber(modid = Betterinventorysorter.MODID)
public class ModCommands {

    private static final SuggestionProvider<CommandSourceStack> REGION_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(Stream.of("all", "main", "hotbar"), builder);

    private static final SuggestionProvider<CommandSourceStack> METHOD_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(
                    Arrays.stream(SortMethod.values()).map(SortMethod::getSerializedName),
                    builder
            );

    private static final SuggestionProvider<CommandSourceStack> ORDER_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(
                    Arrays.stream(SortOrder.values()).map(SortOrder::getSerializedName),
                    builder
            );

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("bis")
                .then(Commands.literal("sortinv")
                        .executes(context -> executeSortInv(context, "main"))
                        .then(Commands.argument("region", StringArgumentType.word())
                                .suggests(REGION_SUGGESTIONS)
                                .executes(context -> executeSortInv(context, StringArgumentType.getString(context, "region")))))
                .then(Commands.literal("help")
                        .executes(ModCommands::executeHelp))
                .then(Commands.literal("change")
                        .then(Commands.argument("method", StringArgumentType.word())
                                .suggests(METHOD_SUGGESTIONS)
                                .then(Commands.argument("order", StringArgumentType.word())
                                        .suggests(ORDER_SUGGESTIONS)
                                        .executes(ModCommands::executeChange))))
                .then(Commands.literal("reset")
                        .executes(ModCommands::executeReset))
                .then(Commands.literal("config")
                        .executes(context -> executeConfig(context, null))
                        .then(Commands.argument("key", StringArgumentType.word())
                                .executes(context -> executeConfig(context, StringArgumentType.getString(context, "key")))))
        );
    }

    private static int executeSortInv(CommandContext<CommandSourceStack> context, String region) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be run by a player"));
            return 0;
        }

        if (player.isSpectator()) {
            context.getSource().sendFailure(Component.literal("Cannot sort inventory in spectator mode"));
            return 0;
        }

        AbstractContainerMenu menu = player.containerMenu;
        SortPreference preference = player.getData(ModAttachments.SORT_PREFERENCE);

        String regionKey;
        switch (region.toLowerCase()) {
            case "hotbar" -> {
                sortRegion(menu, SortHandler.REGION_PLAYER_HOTBAR, preference);
                regionKey = "command.betterinventorysorter.sortinv.region.hotbar";
            }
            case "main" -> {
                sortRegion(menu, SortHandler.REGION_PLAYER_MAIN, preference);
                regionKey = "command.betterinventorysorter.sortinv.region.main";
            }
            case "all" -> {
                sortRegion(menu, SortHandler.REGION_PLAYER_MAIN, preference);
                sortRegion(menu, SortHandler.REGION_PLAYER_HOTBAR, preference);
                regionKey = "command.betterinventorysorter.sortinv.region.all";
            }
            default -> {
                context.getSource().sendFailure(Component.literal("Invalid region. Use: all, main, or hotbar"));
                return 0;
            }
        }

        menu.broadcastChanges();
        context.getSource().sendSuccess(() -> Component.translatable(
                "command.betterinventorysorter.sortinv.success",
                Component.translatable(regionKey)
        ), false);

        return 1;
    }

    private static void sortRegion(AbstractContainerMenu menu, int region, SortPreference preference) {
        List<Slot> targetSlots = SortHandler.getTargetSlots(menu, region);
        if (targetSlots.isEmpty()) {
            return;
        }

        List<ItemStack> stacks = new ArrayList<>();
        for (Slot slot : targetSlots) {
            stacks.add(slot.getItem().copy());
        }

        List<ItemStack> sorted = ItemSorter.sort(stacks, preference);

        for (int i = 0; i < targetSlots.size(); i++) {
            targetSlots.get(i).set(sorted.get(i));
        }
    }

    private static int executeHelp(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        SortPreference current = player != null
                ? player.getData(ModAttachments.SORT_PREFERENCE)
                : new SortPreference(Config.defaultSortMethod, Config.defaultSortOrder);

        context.getSource().sendSuccess(() -> Component.translatable("command.betterinventorysorter.help.header"), false);
        context.getSource().sendSuccess(() -> Component.translatable("command.betterinventorysorter.help.sortinv"), false);
        context.getSource().sendSuccess(() -> Component.translatable("command.betterinventorysorter.help.change"), false);
        context.getSource().sendSuccess(() -> Component.translatable("command.betterinventorysorter.help.reset"), false);
        context.getSource().sendSuccess(() -> Component.translatable("command.betterinventorysorter.help.config"), false);
        context.getSource().sendSuccess(() -> Component.translatable(
                "command.betterinventorysorter.help.current",
                Component.translatable(current.method().getTranslationKey()),
                Component.translatable(current.order().getTranslationKey())
        ), false);

        return 1;
    }

    private static int executeChange(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be run by a player"));
            return 0;
        }

        String methodName = StringArgumentType.getString(context, "method");
        String orderName = StringArgumentType.getString(context, "order");

        SortMethod method = parseMethod(methodName);
        if (method == null) {
            context.getSource().sendFailure(Component.translatable("command.betterinventorysorter.change.invalid_method"));
            return 0;
        }

        SortOrder order = parseOrder(orderName);
        if (order == null) {
            context.getSource().sendFailure(Component.translatable("command.betterinventorysorter.change.invalid_order"));
            return 0;
        }

        SortPreference newPreference = new SortPreference(method, order);
        player.setData(ModAttachments.SORT_PREFERENCE, newPreference);

        PacketDistributor.sendToPlayer(player, new SyncPreferencePayload(method, order));

        context.getSource().sendSuccess(() -> Component.translatable(
                "command.betterinventorysorter.change.success",
                Component.translatable(method.getTranslationKey()),
                Component.translatable(order.getTranslationKey())
        ), false);

        return 1;
    }

    private static int executeReset(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be run by a player"));
            return 0;
        }

        SortPreference defaultPreference = new SortPreference(Config.defaultSortMethod, Config.defaultSortOrder);
        player.setData(ModAttachments.SORT_PREFERENCE, defaultPreference);

        PacketDistributor.sendToPlayer(player, new SyncPreferencePayload(defaultPreference.method(), defaultPreference.order()));

        context.getSource().sendSuccess(() -> Component.translatable(
                "command.betterinventorysorter.reset.success",
                Component.translatable(defaultPreference.method().getTranslationKey()),
                Component.translatable(defaultPreference.order().getTranslationKey())
        ), false);

        return 1;
    }

    private static int executeConfig(CommandContext<CommandSourceStack> context, String key) {
        if (key == null) {
            context.getSource().sendSuccess(() -> Component.translatable("command.betterinventorysorter.config.header"), false);
            context.getSource().sendSuccess(() -> Component.translatable(
                    "command.betterinventorysorter.config.method",
                    Component.translatable(Config.defaultSortMethod.getTranslationKey())
            ), false);
            context.getSource().sendSuccess(() -> Component.translatable(
                    "command.betterinventorysorter.config.order",
                    Component.translatable(Config.defaultSortOrder.getTranslationKey())
            ), false);
            context.getSource().sendSuccess(() -> Component.translatable(
                    "command.betterinventorysorter.config.button",
                    String.valueOf(Config.showSortButton)
            ), false);
        } else {
            switch (key.toLowerCase()) {
                case "method" -> context.getSource().sendSuccess(() -> Component.translatable(
                        "command.betterinventorysorter.config.method",
                        Component.translatable(Config.defaultSortMethod.getTranslationKey())
                ), false);
                case "order" -> context.getSource().sendSuccess(() -> Component.translatable(
                        "command.betterinventorysorter.config.order",
                        Component.translatable(Config.defaultSortOrder.getTranslationKey())
                ), false);
                case "button" -> context.getSource().sendSuccess(() -> Component.translatable(
                        "command.betterinventorysorter.config.button",
                        String.valueOf(Config.showSortButton)
                ), false);
                default -> {
                    context.getSource().sendFailure(Component.literal("Unknown config key: " + key + ". Valid keys: method, order, button"));
                    return 0;
                }
            }
        }

        return 1;
    }

    private static SortMethod parseMethod(String name) {
        for (SortMethod method : SortMethod.values()) {
            if (method.getSerializedName().equalsIgnoreCase(name)) {
                return method;
            }
        }
        return null;
    }

    private static SortOrder parseOrder(String name) {
        for (SortOrder order : SortOrder.values()) {
            if (order.getSerializedName().equalsIgnoreCase(name)) {
                return order;
            }
        }
        return null;
    }
}
