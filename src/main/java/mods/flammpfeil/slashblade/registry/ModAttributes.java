package mods.flammpfeil.slashblade.registry;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, SlashBlade.MODID);

    public static final DeferredHolder<Attribute, Attribute> SLASHBLADE_DAMAGE = ATTRIBUTES.register("slashblade_damage",
            () -> new RangedAttribute("attribute.name.generic.slashblade_damage", 1.0d, 0.0d, 512.0d).setSyncable(true));


    public static Attribute getSlashBladeDamage() {
        return SLASHBLADE_DAMAGE.get();
    }

}
