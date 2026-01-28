package xyz.bannach.betterinventorysorter;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import xyz.bannach.betterinventorysorter.sorting.SortPreference;

import java.util.function.Supplier;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Betterinventorysorter.MODID);

    public static final Supplier<AttachmentType<SortPreference>> SORT_PREFERENCE =
            ATTACHMENT_TYPES.register("sort_preference", () ->
                    AttachmentType.builder(() -> new SortPreference(Config.defaultSortMethod, Config.defaultSortOrder))
                            .serialize(SortPreference.CODEC)
                            .copyOnDeath()
                            .build()
            );
}
