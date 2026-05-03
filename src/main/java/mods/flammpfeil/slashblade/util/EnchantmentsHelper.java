package mods.flammpfeil.slashblade.util;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

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
}
