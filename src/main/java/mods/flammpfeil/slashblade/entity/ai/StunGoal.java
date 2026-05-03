package mods.flammpfeil.slashblade.entity.ai;

import mods.flammpfeil.slashblade.capability.mobeffect.CapabilityMobEffect;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class StunGoal extends Goal {
    private final PathfinderMob entity;

    public StunGoal(PathfinderMob creature) {
        this.entity = creature;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK, Flag.TARGET));
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean canUse() {

        var mobEffect = this.entity.getData(CapabilityMobEffect.MOB_EFFECT.get());
        return mobEffect != null && mobEffect.isStun(this.entity.level().getGameTime());
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by
     * another one
     */
    @Override
    public void stop() {
        var mobEffect = this.entity.getData(CapabilityMobEffect.MOB_EFFECT.get());
        if (mobEffect != null) {
            mobEffect.clearStunTimeOut();
        }
    }
}
