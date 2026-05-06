package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

public class AnvilRarityHandler {
    private static final class SingletonHolder {
        private static final AnvilRarityHandler instance = new AnvilRarityHandler();
    }

    public static AnvilRarityHandler getInstance() {
        return SingletonHolder.instance;
    }

    private AnvilRarityHandler() {
    }

    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onAnvilUpdate(AnvilUpdateEvent event) {
        if (!(event.getOutput().getItem() instanceof ItemSlashBlade)) {
            return;
        }

        ItemSlashBlade.updateRarity(event.getOutput());
    }
}
