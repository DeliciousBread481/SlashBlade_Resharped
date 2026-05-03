package mods.flammpfeil.slashblade.ability;

import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.event.handler.InputCommandEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.InputCommand;
import mods.flammpfeil.slashblade.util.RayTraceHelper;
import mods.flammpfeil.slashblade.util.TargetSelector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LockOnManager {
    private static final class SingletonHolder {
        private static final LockOnManager instance = new LockOnManager();
    }

    public static LockOnManager getInstance() {
        return SingletonHolder.instance;
    }

    private LockOnManager() {
    }

    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onInputChange(InputCommandEvent event) {


        ServerPlayer player = event.getEntity();
        // set target
        ItemStack stack = event.getEntity().getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }
        if (!(stack.getItem() instanceof ItemSlashBlade)) {
            return;
        }

        Entity targetEntity;

        if (event.getOld().contains(InputCommand.SNEAK) == event.getCurrent().contains(InputCommand.SNEAK)) {
            return;
        }

        if ((event.getOld().contains(InputCommand.SNEAK) && !event.getCurrent().contains(InputCommand.SNEAK))) {
            // remove target
            targetEntity = null;
        } else {
            // search target

            Optional<HitResult> result = RayTraceHelper.rayTrace(player.level(), player, player.getEyePosition(1.0f),
                    player.getLookAngle(), 40, 40, (e) -> true);
            Optional<Entity> foundEntity = result.filter(r -> r.getType() == HitResult.Type.ENTITY).filter(r -> {
                EntityHitResult er = (EntityHitResult) r;
                Entity target = er.getEntity();

                if (target instanceof PartEntity) {
                    target = ((PartEntity<?>) target).getParent();
                }

                boolean isMatch = false;

                if (target instanceof LivingEntity) {
                    isMatch = TargetSelector.lockon.test(player, (LivingEntity) target);
                }

                return isMatch;
            }).map(r -> ((EntityHitResult) r).getEntity());

            if (foundEntity.isEmpty()) {
                List<LivingEntity> entities = player.level().getNearbyEntities(LivingEntity.class,
                        TargetSelector.lockon, player, player.getBoundingBox().inflate(12.0D, 6.0D, 12.0D));

                foundEntity = entities.stream().map(s -> (Entity) s)
                        .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)));
            }

            targetEntity = foundEntity.map(e -> (e instanceof PartEntity) ? ((PartEntity<?>) e).getParent() : e)
                    .orElse(null);

        }

        BladeStateAccess.of(stack).ifPresent(s -> s.setTargetEntityId(targetEntity));

    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onEntityUpdate(ClientTickEvent.Pre event) {
        final Minecraft mcinstance = Minecraft.getInstance();
        if (mcinstance.player == null) {
            return;
        }

        LocalPlayer player = mcinstance.player;

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }

        BladeStateAccess.of(stack).ifPresent(s -> {

            Entity target = s.getTargetEntity(player.level());

            if (target == null) {
                return;
            }
            if (!target.isAlive()) {
                return;
            }

            if (!player.level().isClientSide()) {
                return;
            }
            var input = player.getData(CapabilityInputState.INPUT_STATE.get());
            if (input == null || !input.getCommands().contains(InputCommand.SNEAK)) {
                return;
            }

            float partialTicks = mcinstance.getTimer().getGameTimeDeltaTicks();

            float oldYawHead = player.yHeadRot;
            float oldYawOffset = player.yBodyRot;
            float oldPitch = player.getXRot();
            float oldYaw = player.getYRot();

            float prevYawHead = player.yHeadRotO;
            float prevYawOffset = player.yBodyRotO;
            float prevYaw = player.yRotO;
            float prevPitch = player.xRotO;

            player.lookAt(EntityAnchorArgument.Anchor.EYES, target.position().add(0, target.getEyeHeight() / 2.0, 0));

            float step = 0.125f * partialTicks;

            step *= (float) Math.min(1.0f, Math.abs(Mth.wrapDegrees(oldYaw - player.yHeadRot) * 0.5));

            player.setXRot(Mth.rotLerp(step, oldPitch, player.getXRot()));
            player.setYRot(Mth.rotLerp(step, oldYaw, player.getYRot()));
            player.setYHeadRot(Mth.rotLerp(step, oldYawHead, player.getYHeadRot()));

            player.yBodyRot = oldYawOffset;

            player.yBodyRotO = prevYawOffset;
            player.yHeadRotO = prevYawHead;
            player.yRotO = prevYaw;
            player.xRotO = prevPitch;
        });
    }

}
