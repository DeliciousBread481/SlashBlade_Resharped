package mods.flammpfeil.slashblade.capability.mobeffect;

import mods.flammpfeil.slashblade.SlashBlade;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class CapabilityMobEffect {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.ATTACHMENT_TYPES, SlashBlade.MODID);

    public static final Supplier<AttachmentType<IMobEffectState>> MOB_EFFECT = ATTACHMENT_TYPES
            .register("mob_effect", () -> AttachmentType.<IMobEffectState>builder(MobEffectState::new).build());
}
