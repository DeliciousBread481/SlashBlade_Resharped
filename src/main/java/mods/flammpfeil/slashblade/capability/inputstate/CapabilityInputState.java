package mods.flammpfeil.slashblade.capability.inputstate;

import mods.flammpfeil.slashblade.SlashBlade;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class CapabilityInputState {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.ATTACHMENT_TYPES, SlashBlade.MODID);

    public static final Supplier<AttachmentType<IInputState>> INPUT_STATE = ATTACHMENT_TYPES
            .register("input_state", () -> AttachmentType.<IInputState>builder(InputState::new).build());
}
