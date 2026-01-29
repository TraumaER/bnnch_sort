package xyz.bannach.bnnch_sort;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import xyz.bannach.bnnch_sort.sorting.SortPreference;

import java.util.function.Supplier;

/**
 * Registry for player data attachments used by the mod.
 *
 * <p>This class registers NeoForge data attachments that persist player-specific data.
 * Attachments are automatically saved with player data and can be configured to persist
 * across death.</p>
 *
 * <h2>Registered Attachments</h2>
 * <ul>
 *   <li>{@link #SORT_PREFERENCE} - Stores the player's sort method and order preferences</li>
 * </ul>
 *
 * <h2>Side: Common</h2>
 * <p>Attachments are registered on both sides but primarily used server-side for persistence.</p>
 *
 * @since 1.0.0
 * @see SortPreference
 * @see BnnchSort
 */
public class ModAttachments {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ModAttachments() {}

    /**
     * Deferred register for attachment types.
     * <p>Registered to the mod event bus in {@link BnnchSort}.</p>
     */
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, BnnchSort.MODID);

    /**
     * Player attachment for storing sort preferences.
     *
     * <p>This attachment stores a {@link SortPreference} containing the player's chosen
     * sort method and order. The attachment has the following properties:</p>
     * <ul>
     *   <li>Default value: Created from {@link Config#defaultSortMethod} and {@link Config#defaultSortOrder}</li>
     *   <li>Serialization: Uses {@link SortPreference#CODEC} for NBT persistence</li>
     *   <li>Death behavior: Preserved across player death (copyOnDeath)</li>
     * </ul>
     */
    public static final Supplier<AttachmentType<SortPreference>> SORT_PREFERENCE =
            ATTACHMENT_TYPES.register("sort_preference", () ->
                    AttachmentType.builder(() -> new SortPreference(Config.defaultSortMethod, Config.defaultSortOrder))
                            .serialize(SortPreference.CODEC)
                            .copyOnDeath()
                            .build()
            );
}
