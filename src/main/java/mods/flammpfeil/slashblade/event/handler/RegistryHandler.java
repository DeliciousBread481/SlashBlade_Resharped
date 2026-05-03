package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.event.drop.EntityDropEntry;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.SlashArtsRegistry;
import mods.flammpfeil.slashblade.registry.SpecialEffectsRegistry;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

@EventBusSubscriber(modid = SlashBlade.MODID)
public class RegistryHandler {

    @SubscribeEvent
    public static void onDatapackRegister(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(SlashBladeDefinition.REGISTRY_KEY, SlashBladeDefinition.CODEC,
                SlashBladeDefinition.CODEC);

        event.dataPackRegistry(EntityDropEntry.REGISTRY_KEY, EntityDropEntry.CODEC, EntityDropEntry.CODEC);
    }

    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event) {
        event.register(ComboStateRegistry.REGISTRY);
        event.register(SlashArtsRegistry.REGISTRY);
        event.register(SpecialEffectsRegistry.REGISTRY);
    }
}