package mods.flammpfeil.slashblade.capability.slashblade;

import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import net.minecraft.resources.ResourceLocation;

public class BladeRuntimeState {
    private ResourceLocation comboSeq = ComboStateRegistry.NONE.getId();
    private long lastActionTime = 0L;
    private int targetEntityId = -1;
    private boolean onClick = false;
    private float fallDecreaseRate = 0.0F;
    private float attackAmplifier = 0.0F;

    public ResourceLocation getComboSeq() { return comboSeq; }
    public void setComboSeq(ResourceLocation comboSeq) { this.comboSeq = comboSeq; }

    public long getLastActionTime() { return lastActionTime; }
    public void setLastActionTime(long lastActionTime) { this.lastActionTime = lastActionTime; }

    public int getTargetEntityId() { return targetEntityId; }
    public void setTargetEntityId(int targetEntityId) { this.targetEntityId = targetEntityId; }

    public boolean isOnClick() { return onClick; }
    public void setOnClick(boolean onClick) { this.onClick = onClick; }

    public float getFallDecreaseRate() { return fallDecreaseRate; }
    public void setFallDecreaseRate(float fallDecreaseRate) { this.fallDecreaseRate = fallDecreaseRate; }

    public float getAttackAmplifier() { return attackAmplifier; }
    public void setAttackAmplifier(float attackAmplifier) { this.attackAmplifier = attackAmplifier; }
}
