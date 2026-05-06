package mods.flammpfeil.slashblade.item;

import com.mojang.serialization.Codec;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;

public enum SwordType {
    NONE, EDGEFRAGMENT, BROKEN, ENCHANTED, BEWITCHED, FIERCEREDGE, NOSCABBARD, SEALED, UNBREAKABLE, SOULEATER;

    public static final Codec<SwordType> CODEC = Codec.STRING.xmap(string -> SwordType.valueOf(string.toUpperCase()),
            instance -> instance.name().toLowerCase());

    public static EnumSet<SwordType> from(ItemStack itemStackIn) {
        EnumSet<SwordType> types = EnumSet.noneOf(SwordType.class);

        var state = BladeStateAccess.of(itemStackIn);

        state.ifPresent(s -> {
            if (s.isBroken()) {
                types.add(BROKEN);
            }

            if (s.isSealed()) {
                types.add(SEALED);
            }

            if (!s.isSealed() && itemStackIn.isEnchanted()
                    && (itemStackIn.has(DataComponents.CUSTOM_NAME) || s.isDefaultBewitched())) {
                types.add(BEWITCHED);
            }

            if (s.getKillCount() >= 1000) {
                types.add(FIERCEREDGE);
            }

            if (s.getProudSoulCount() >= 10000) {
                types.add(SOULEATER);
            }

        });
        if (state.isEmpty()) {
            types.add(NOSCABBARD);
            types.add(EDGEFRAGMENT);
        }

        if (itemStackIn.isEnchanted() && !state.map(ISlashBladeState::isSealed).orElse(false)) {
            types.add(ENCHANTED);
        }

        if (itemStackIn.getItem() instanceof ItemSlashBladeDetune) {
            types.remove(SwordType.ENCHANTED);
            types.remove(SwordType.BEWITCHED);
        }

        if (itemStackIn.has(DataComponents.UNBREAKABLE)) {
            types.remove(SwordType.BROKEN);
        }
        return types;
    }
}
