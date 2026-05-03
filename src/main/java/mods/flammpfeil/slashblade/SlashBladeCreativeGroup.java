package mods.flammpfeil.slashblade;

import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.registry.SlashArtsRegistry;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class SlashBladeCreativeGroup {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, SlashBlade.MODID);

    private static final CreativeModeTab SLASHBLADE = CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.slashblade")).icon(() -> {
                ItemStack stack = new ItemStack(SlashBladeItems.SLASHBLADE.get());
                BladeStateAccess.of(stack).ifPresent(s -> {
                    s.setModel(ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID, "model/named/yamato.obj"));
                    s.setTexture(ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID, "model/named/yamato.png"));
                });
                return stack;
            }).displayItems((features, output) -> {

                output.accept(SlashBladeItems.PROUDSOUL.get());
                output.accept(SlashBladeItems.PROUDSOUL_TINY.get());
                output.accept(SlashBladeItems.PROUDSOUL_INGOT.get());
                output.accept(SlashBladeItems.PROUDSOUL_SPHERE.get());

                output.accept(SlashBladeItems.PROUDSOUL_CRYSTAL.get());
                output.accept(SlashBladeItems.PROUDSOUL_TRAPEZOHEDRON.get());
                fillEnchantmentsSouls(features, output);
                fillSASpheres(output);
                output.accept(SlashBladeItems.BLADESTAND_1.get());
                output.accept(SlashBladeItems.BLADESTAND_1_W.get());
                output.accept(SlashBladeItems.BLADESTAND_2.get());
                output.accept(SlashBladeItems.BLADESTAND_2_W.get());
                output.accept(SlashBladeItems.BLADESTAND_S.get());
                output.accept(SlashBladeItems.BLADESTAND_V.get());

                output.accept(SlashBladeItems.SLASHBLADE_WOOD.get());
                output.accept(SlashBladeItems.SLASHBLADE_BAMBOO.get());
                output.accept(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get());
                output.accept(SlashBladeItems.SLASHBLADE_WHITE.get());
                output.accept(SlashBladeItems.SLASHBLADE.get());
            }).build();

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SLASHBLADE_GROUP = CREATIVE_MODE_TABS.register("slashblade",
            () -> SLASHBLADE);


    private static void fillEnchantmentsSouls(CreativeModeTab.ItemDisplayParameters features, CreativeModeTab.Output output) {
        features.holders().lookupOrThrow(Registries.ENCHANTMENT).listElements()
            .filter(holder -> new ItemStack(SlashBladeItems.SLASHBLADE.get()).supportsEnchantment(holder))
            .forEach(holder -> {
                ItemStack soul = new ItemStack(SlashBladeItems.PROUDSOUL_TINY.get());
                soul.enchant(holder, 1);
                output.accept(soul);
            });
    }

    private static void fillSASpheres(CreativeModeTab.Output output) {
        SlashArtsRegistry.REGISTRY.forEach(slashArts -> {
            ResourceLocation key = SlashArtsRegistry.REGISTRY.getKey(slashArts);
            if (slashArts.equals(SlashArtsRegistry.NONE.get()) || key == null) {
                return;
            }
            ItemStack sphere = new ItemStack(SlashBladeItems.PROUDSOUL_SPHERE.get());
            CompoundTag tag = new CompoundTag();
            tag.putString("SpecialAttackType", key.toString());
            sphere.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            output.accept(sphere);
        });
    }
}
