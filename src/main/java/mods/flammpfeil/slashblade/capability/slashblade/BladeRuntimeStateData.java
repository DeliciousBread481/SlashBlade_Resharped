package mods.flammpfeil.slashblade.capability.slashblade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record BladeRuntimeStateData(
    ResourceLocation comboSeq,
    long lastActionTime,
    long lastProcessedComboTick,
    int targetEntityId,
    boolean onClick,
    float fallDecreaseRate,
    float attackAmplifier
) {
    public static final BladeRuntimeStateData DEFAULT = new BladeRuntimeStateData(
        ComboStateRegistry.NONE.getId(),
        0L,
        -1L,
        -1,
        false,
        0.0F,
        0.0F
    );

    public static final Codec<BladeRuntimeStateData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("comboSeq").forGetter(BladeRuntimeStateData::comboSeq),
        Codec.LONG.fieldOf("lastActionTime").forGetter(BladeRuntimeStateData::lastActionTime),
        Codec.LONG.fieldOf("lastProcessedComboTick").forGetter(BladeRuntimeStateData::lastProcessedComboTick),
        Codec.INT.fieldOf("targetEntityId").forGetter(BladeRuntimeStateData::targetEntityId),
        Codec.BOOL.fieldOf("onClick").forGetter(BladeRuntimeStateData::onClick),
        Codec.FLOAT.fieldOf("fallDecreaseRate").forGetter(BladeRuntimeStateData::fallDecreaseRate),
        Codec.FLOAT.fieldOf("attackAmplifier").forGetter(BladeRuntimeStateData::attackAmplifier)
    ).apply(instance, BladeRuntimeStateData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BladeRuntimeStateData> STREAM_CODEC =
        ByteBufCodecs.fromCodecWithRegistries(CODEC);
}
