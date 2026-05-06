package mods.flammpfeil.slashblade.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class EnchantmentsHelper {
    public static boolean hasEnchantmentsMatch(ItemStack stackA, ItemStack stackB) {
        var enchantmentsB = EnchantmentHelper.getEnchantmentsForCrafting(stackB);
        if (enchantmentsB.isEmpty()) {
            return true;
        }
        var enchantmentsA = EnchantmentHelper.getEnchantmentsForCrafting(stackA);
        for (var entry : enchantmentsB.entrySet()) {
            Holder<Enchantment> ench = entry.getKey();
            int requiredLevel = entry.getIntValue();
            if (enchantmentsA.getLevel(ench) < requiredLevel) {
                return false;
            }
        }
        return true;
    }

    public static void updateEnchantment(ItemStack result, ItemStack ingredient, HolderLookup.Provider access) {
        HolderLookup.RegistryLookup<Enchantment> enchantmentLookup = access.lookupOrThrow(Registries.ENCHANTMENT);
        ItemEnchantments.Mutable newItemEnchants = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(result));
        ItemEnchantments oldItemEnchants = ingredient.getAllEnchantments(enchantmentLookup);
        for (var entry : oldItemEnchants.entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            int destLevel = newItemEnchants.getLevel(enchantment);
            int srcLevel = entry.getIntValue();

            srcLevel = Math.max(srcLevel, destLevel);
            srcLevel = Math.min(srcLevel, enchantment.value().getMaxLevel());

            boolean canApplyFlag = result.supportsEnchantment(enchantment);
            if (canApplyFlag) {
                for (var currentEntry : newItemEnchants.toImmutable().entrySet()) {
                    Holder<Enchantment> currentEnchantment = currentEntry.getKey();
                    if (!currentEnchantment.equals(enchantment)
                            && !Enchantment.areCompatible(enchantment, currentEnchantment)) {
                        canApplyFlag = false;
                        break;
                    }
                }
                if (canApplyFlag) {
                    newItemEnchants.set(enchantment, srcLevel);
                }
            }
        }
        EnchantmentHelper.setEnchantments(result, newItemEnchants.toImmutable());
    }
}
