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

/**
 * Registers and handles the /bis command tree.
 *
 * <p>This class provides Brigadier slash commands for inventory sorting operations.
 * All commands are registered under the {@code /bis} prefix (Better Inventory Sorter).</p>
 *
 * <h2>Available Commands</h2>
 * <ul>
 *   <li>{@code /bis sortinv [region]} - Sort player inventory (all, main, or hotbar)</li>
 *   <li>{@code /bis change <method> <order>} - Change sort preferences</li>
 *   <li>{@code /bis reset} - Reset preferences to server defaults</li>
 *   <li>{@code /bis config [key]} - View configuration values</li>
 *   <li>{@code /bis help} - Display help information</li>
 * </ul>
 *
 * <h2>Side: Common</h2>
 * <p>Commands are registered on both sides but execute server-side.</p>
 *
 * @since 1.0.0
 * @see SortHandler
 * @see ItemSorter
 * @see SortPreference
 */
@EventBusSubscriber(modid = Betterinventorysorter.MODID)
public class ModCommands {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ModCommands() {}

    /**
     * Suggestion provider for inventory region arguments.
     * <p>Provides: all, main, hotbar</p>
     */
    private static final SuggestionProvider<CommandSourceStack> REGION_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(Stream.of("all", "main", "hotbar"), builder);

    /**
     * Suggestion provider for sort method arguments.
     * <p>Provides: alphabetical, category, quantity, mod_id</p>
     */
    private static final SuggestionProvider<CommandSourceStack> METHOD_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(
                    Arrays.stream(SortMethod.values()).map(SortMethod::getSerializedName),
                    builder
            );

    /**
     * Suggestion provider for sort order arguments.
     * <p>Provides: ascending, descending</p>
     */
    private static final SuggestionProvider<CommandSourceStack> ORDER_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(
                    Arrays.stream(SortOrder.values()).map(SortOrder::getSerializedName),
                    builder
            );

    /**
     * Registers all mod commands during the command registration event.
     *
     * <p>This method builds and registers the complete {@code /bis} command tree
     * with all subcommands and their argument handlers.</p>
     *
     * @param event the command registration event
     */
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

    /**
     * Executes the /bis sortinv command to sort player inventory.
     *
     * <p>Sorts the specified region of the player's inventory using their current
     * sort preferences. Valid regions are "all", "main", and "hotbar".</p>
     *
     * @param context the command context containing the source
     * @param region the inventory region to sort
     * @return 1 on success, 0 on failure
     */
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

    /**
     * Sorts a specific inventory region using the given preferences.
     *
     * <p>Extracts items from the target slots, sorts them, and writes them back.
     * This is a helper method used by {@link #executeSortInv}.</p>
     *
     * @param menu the container menu to sort within
     * @param region the region code to sort
     * @param preference the sort preferences to apply
     */
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

    /**
     * Executes the /bis help command to display usage information.
     *
     * <p>Shows all available commands and the player's current sort preferences.</p>
     *
     * @param context the command context containing the source
     * @return 1 (always succeeds)
     */
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

    /**
     * Executes the /bis change command to update sort preferences.
     *
     * <p>Sets the player's sort method and order to the specified values and
     * syncs the change to the client.</p>
     *
     * @param context the command context containing the source and arguments
     * @return 1 on success, 0 on failure (invalid arguments or not a player)
     */
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

    /**
     * Executes the /bis reset command to restore default preferences.
     *
     * <p>Resets the player's sort preferences to the server-configured defaults
     * and syncs the change to the client.</p>
     *
     * @param context the command context containing the source
     * @return 1 on success, 0 on failure (not a player)
     */
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

    /**
     * Executes the /bis config command to display configuration values.
     *
     * <p>Shows all config values if no key is specified, or a specific value
     * if a key (method, order, or button) is provided.</p>
     *
     * @param context the command context containing the source
     * @param key the config key to display, or null for all keys
     * @return 1 on success, 0 on failure (invalid key)
     */
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

    /**
     * Parses a sort method from its string name.
     *
     * @param name the serialized name of the sort method (case-insensitive)
     * @return the matching SortMethod, or null if not found
     */
    private static SortMethod parseMethod(String name) {
        for (SortMethod method : SortMethod.values()) {
            if (method.getSerializedName().equalsIgnoreCase(name)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Parses a sort order from its string name.
     *
     * @param name the serialized name of the sort order (case-insensitive)
     * @return the matching SortOrder, or null if not found
     */
    private static SortOrder parseOrder(String name) {
        for (SortOrder order : SortOrder.values()) {
            if (order.getSerializedName().equalsIgnoreCase(name)) {
                return order;
            }
        }
        return null;
    }
}
