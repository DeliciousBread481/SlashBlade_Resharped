package mods.flammpfeil.slashblade.registry.slashblade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentDefinition {
    public static final Codec<EnchantmentDefinition> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Enchantment.CODEC.fieldOf("id").forGetter(EnchantmentDefinition::getEnchantment),
                    Codec.INT.optionalFieldOf("lvl", 1).forGetter(EnchantmentDefinition::getEnchantmentLevel))
            .apply(instance, EnchantmentDefinition::new));

    private final Holder<Enchantment> id;
    private final int lvl;

    public EnchantmentDefinition(Holder<Enchantment> enchantment, int level) {
        this.id = enchantment;
        this.lvl = level;
    }

    public Holder<Enchantment> getEnchantment() {
        return id;
    }

    public int getEnchantmentLevel() {
        return lvl;
    }
}