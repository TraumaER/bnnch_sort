package xyz.bannach.bnnch_sort;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

/**
 * Main entrypoint for the Bnnch: Sort mod.
 *
 * <p>This class is annotated with {@link Mod} and serves as the primary initialization point for
 * the mod. It registers data attachments, configuration files, and sets up the mod event bus
 * listeners.
 *
 * <h2>Responsibilities</h2>
 *
 * <ul>
 *   <li>Register {@link ModAttachments} for player sort preferences
 *   <li>Register client and server configuration files
 *   <li>Log initialization message on common setup
 * </ul>
 *
 * <h2>Side: Common</h2>
 *
 * <p>This class is loaded on both client and server sides.
 *
 * @see Config
 * @see ModAttachments
 * @since 1.0.0
 */
@Mod(BnnchSort.MODID)
public class BnnchSort {

  /** The mod identifier used throughout the mod for registration and resource locations. */
  public static final String MODID = "bnnch_sort";

  /** Logger instance for this class. */
  private static final Logger LOGGER = LogUtils.getLogger();

  /**
   * Constructs the mod instance and performs all necessary registrations.
   *
   * <p>This constructor is called by NeoForge during mod loading. It registers the common setup
   * listener, attachment types, and configuration specifications.
   *
   * @param modEventBus the mod-specific event bus for registering listeners
   * @param modContainer the container providing access to mod metadata and config registration
   */
  public BnnchSort(IEventBus modEventBus, ModContainer modContainer) {
    modEventBus.addListener(this::commonSetup);
    xyz.bannach.bnnch_sort.ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
    modContainer.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
    modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
  }

  /**
   * Handles the common setup event, called after all mods have been constructed.
   *
   * <p>This method logs an initialization message to confirm the mod has loaded successfully.
   *
   * @param event the common setup event fired during mod loading
   */
  private void commonSetup(final FMLCommonSetupEvent event) {
    LOGGER.info("Bnnch: Sort initialized");
  }
}
