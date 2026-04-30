package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.event.SlashBladeRegistryEvent;
import mods.flammpfeil.slashblade.item.SwordType;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class SlashBladeEventHandler {

    @SubscribeEvent
    public static void onLivingOnFire(LivingAttackEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();

        ItemStack stack = victim.getMainHandItem();
        if (stack.getEnchantmentLevel(Enchantments.FIRE_PROTECTION) <= 0) {
            return;
        }
        if (!source.is(DamageTypeTags.IS_FIRE)) {
            return;
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLoadingBlade(SlashBladeRegistryEvent.Pre event) {
        if (!ForgeRegistries.ITEMS.containsKey(event.getSlashBladeDefinition().getItemName())) {
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
