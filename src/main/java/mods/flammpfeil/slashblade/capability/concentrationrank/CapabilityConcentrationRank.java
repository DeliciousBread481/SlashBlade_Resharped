package mods.flammpfeil.slashblade.capability.concentrationrank;

import mods.flammpfeil.slashblade.SlashBlade;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class CapabilityConcentrationRank {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.ATTACHMENT_TYPES, SlashBlade.MODID);

    public static final Supplier<AttachmentType<IConcentrationRank>> RANK_POINT = ATTACHMENT_TYPES
            .register("concentration", () -> AttachmentType.<IConcentrationRank>builder(ConcentrationRank::new).build());
}
