package mods.flammpfeil.slashblade.capability.slashblade;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class SlashBladeDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, SlashBlade.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BladeStateData>> BLADE_STATE_DATA =
        register("blade_state",
            b -> b.persistent(BladeStateData.CODEC)
                  .networkSynchronized(BladeStateData.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BladeRuntimeStateData>> BLADE_RUNTIME_STATE =
        register("blade_runtime_state",
            b -> b.persistent(BladeRuntimeStateData.CODEC)
                  .networkSynchronized(BladeRuntimeStateData.STREAM_CODEC));

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(
            String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return COMPONENTS.register(name,
            () -> builder.apply(DataComponentType.builder()).build());
    }
}
