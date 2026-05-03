package mods.flammpfeil.slashblade.registry;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.slasharts.SlashArts;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class SlashArtsRegistry {
    public static final DeferredRegister<SlashArts> SLASH_ARTS = DeferredRegister.create(SlashArts.REGISTRY_KEY,
            SlashBlade.MODID);

    public static final Registry<SlashArts> REGISTRY = new RegistryBuilder<>(SlashArts.REGISTRY_KEY).create();
    public static final DeferredHolder<SlashArts, SlashArts> NONE = SLASH_ARTS.register("none",
            () -> new SlashArts((e) -> ComboStateRegistry.NONE.getId()));

    public static final DeferredHolder<SlashArts, SlashArts> JUDGEMENT_CUT = SLASH_ARTS.register("judgement_cut",
            () -> new SlashArts((e) -> e.onGround() ? ComboStateRegistry.JUDGEMENT_CUT.getId()
                    : ComboStateRegistry.JUDGEMENT_CUT_SLASH_AIR.getId()).setComboStateJust((e) -> ComboStateRegistry.JUDGEMENT_CUT_SLASH_JUST.getId()).setComboStateSuper(e -> ComboStateRegistry.JUDGEMENT_CUT_END.getId()));

    public static final DeferredHolder<SlashArts, SlashArts> SAKURA_END = SLASH_ARTS.register("sakura_end",
            () -> new SlashArts((e) -> e.onGround() ? ComboStateRegistry.SAKURA_END_LEFT.getId()
                    : ComboStateRegistry.SAKURA_END_LEFT_AIR.getId()));

    public static final DeferredHolder<SlashArts, SlashArts> VOID_SLASH = SLASH_ARTS.register("void_slash",
            () -> new SlashArts((e) -> ComboStateRegistry.VOID_SLASH.getId()));

    public static final DeferredHolder<SlashArts, SlashArts> CIRCLE_SLASH = SLASH_ARTS.register("circle_slash",
            () -> new SlashArts((e) -> ComboStateRegistry.CIRCLE_SLASH.getId()));

    public static final DeferredHolder<SlashArts, SlashArts> DRIVE_VERTICAL = SLASH_ARTS.register("drive_vertical",
            () -> new SlashArts((e) -> ComboStateRegistry.DRIVE_VERTICAL.getId()));

    public static final DeferredHolder<SlashArts, SlashArts> DRIVE_HORIZONTAL = SLASH_ARTS.register("drive_horizontal",
            () -> new SlashArts((e) -> ComboStateRegistry.DRIVE_HORIZONTAL.getId()));

    public static final DeferredHolder<SlashArts, SlashArts> WAVE_EDGE = SLASH_ARTS.register("wave_edge",
            () -> new SlashArts((e) -> ComboStateRegistry.WAVE_EDGE_VERTICAL.getId()));

    public static final DeferredHolder<SlashArts, SlashArts> PIERCING = SLASH_ARTS.register("piercing",
            () -> new SlashArts((e) -> ComboStateRegistry.PIERCING.getId())
                    .setComboStateJust((e) -> ComboStateRegistry.PIERCING_JUST.getId())
    );
}
