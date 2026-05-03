package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.event.SlashBladeRegistryEvent;
import mods.flammpfeil.slashblade.item.SwordType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = SlashBlade.MODID)
public class SlashBladeEventHandler {

    @SubscribeEvent
    public static void onLivingOnFire(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        var enchantmentLookup = victim.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var fireProtection = enchantmentLookup.getOrThrow(Enchantments.FIRE_PROTECTION);

        ItemStack stack = victim.getMainHandItem();
        if (stack.getEnchantmentLevel(fireProtection) <= 0) {
            return;
        }
        if (!source.is(DamageTypeTags.IS_FIRE)) {
            return;
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLoadingBlade(SlashBladeRegistryEvent.Pre event) {
        if (!BuiltInRegistries.ITEM.containsKey(event.getSlashBladeDefinition().getItemName())) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void onChargeBlade(SlashBladeEvent.ChargeActionEvent event) {
    	var state = event.getSlashBladeState();
        var swordType = SwordType.from(event.getEntityLiving().getMainHandItem());
        if (state.isBroken() || state.isSealed() || !(swordType.contains(SwordType.ENCHANTED))) {
            event.setCanceled(true);
        }
    }

}
