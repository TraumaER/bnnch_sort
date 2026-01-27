package xyz.bannach.betterinventorysorter;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(Betterinventorysorter.MODID)
public class Betterinventorysorter {
    public static final String MODID = "betterinventorysorter";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Betterinventorysorter(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Better Inventory Sorter initialized");
    }
}
