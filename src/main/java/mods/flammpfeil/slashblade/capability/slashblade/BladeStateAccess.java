package mods.flammpfeil.slashblade.capability.slashblade;

import mods.flammpfeil.slashblade.client.renderer.CarryType;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBladeDetune;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.SpecialEffectsRegistry;
import mods.flammpfeil.slashblade.util.NBTHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.UnaryOperator;

public class BladeStateAccess {

    private BladeStateAccess() {}

    public static Optional<ISlashBladeState> of(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        if (!(stack.getItem() instanceof ItemSlashBlade)) return Optional.empty();
        ensureComponent(stack);
        return Optional.of(new ComponentBackedState(stack));
    }

    private static void ensureComponent(ItemStack stack) {
        if (stack.has(SlashBladeDataComponents.BLADE_STATE_DATA.get())) return;
        BladeStateData data = BladeStateData.DEFAULT;
        if (stack.getItem() instanceof ItemSlashBladeDetune detune) {
            data = new BladeStateData(
                "", detune.getBaseAttackModifier(), 0, 0, 0, false, false,
                BladeStateData.DEFAULT_SLASH_ARTS, false, BladeStateData.DEFAULT_COMBO_ROOT,
                CarryType.PSO2, 0xFF3333FF, false, Vec3.ZERO,
                Optional.ofNullable(detune.getTexture()),
                Optional.ofNullable(detune.getModel()),
                java.util.Collections.emptyList()
            );
        }
        stack.set(SlashBladeDataComponents.BLADE_STATE_DATA.get(), data);
    }

    public static Optional<BladeStateData> getData(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        return Optional.ofNullable(stack.get(SlashBladeDataComponents.BLADE_STATE_DATA.get()));
    }

    public static BladeStateData getDataOrDefault(ItemStack stack) {
        return stack.getOrDefault(SlashBladeDataComponents.BLADE_STATE_DATA.get(), BladeStateData.DEFAULT);
    }

    public static void setData(ItemStack stack, BladeStateData data) {
        stack.set(SlashBladeDataComponents.BLADE_STATE_DATA.get(), data);
    }

    public static void updateData(ItemStack stack, UnaryOperator<BladeStateData> updater) {
        stack.update(SlashBladeDataComponents.BLADE_STATE_DATA.get(), BladeStateData.DEFAULT, updater);
    }

    public static BladeRuntimeState getRuntime(ItemStack stack) {
        return BladeRuntimeStateStore.get(stack);
    }

    static class ComponentBackedState implements ISlashBladeState {
        private final ItemStack stack;

        ComponentBackedState(ItemStack stack) {
            this.stack = stack;
        }

        private BladeStateData data() {
            return stack.getOrDefault(SlashBladeDataComponents.BLADE_STATE_DATA.get(), BladeStateData.DEFAULT);
        }

        private BladeRuntimeState runtime() {
            return BladeRuntimeStateStore.get(stack);
        }

        private void update(UnaryOperator<BladeStateData> updater) {
            stack.update(SlashBladeDataComponents.BLADE_STATE_DATA.get(), BladeStateData.DEFAULT, updater);
        }

        // ========== Persistent fields (BladeStateData) ==========

        @Override @Nonnull
        public String getTranslationKey() { return data().translationKey(); }

        @Override
        public void setTranslationKey(String translationKey) {
            update(d -> new BladeStateData(
                Optional.ofNullable(translationKey).orElse(""), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }

        @Override
        public float getBaseAttackModifier() { return data().baseAttackModifier(); }

        @Override
        public void setBaseAttackModifier(float baseAttackModifier) {
            update(d -> new BladeStateData(d.translationKey(), baseAttackModifier,
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }

        @Override
        public int getProudSoulCount() { return data().proudSoul(); }

        @Override
        public void setProudSoulCount(int psCount) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                Math.max(0, psCount), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }

        @Override
        public int getKillCount() { return data().killCount(); }

        @Override
        public void setKillCount(int killCount) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), killCount, d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }

        @Override
        public int getRefine() { return data().refine(); }

        @Override
        public void setRefine(int refine) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), refine, d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }

        @Override
        public boolean isBroken() { return data().broken(); }

        @Override
        public void setBroken(boolean broken) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), broken, d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }

        @Override
        public boolean isSealed() { return data().sealed(); }

        @Override
        public void setSealed(boolean sealed) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), sealed,
                d.slashArtsKey(), d.defaultBewitched(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }

        @Override
        public ResourceLocation getSlashArtsKey() { return data().slashArtsKey(); }

        @Override
        public void setSlashArtsKey(ResourceLocation key) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                key != null ? key : BladeStateData.DEFAULT_SLASH_ARTS, d.defaultBewitched(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }

        @Override
        public boolean isDefaultBewitched() { return data().defaultBewitched(); }

        @Override
        public void setDefaultBewitched(boolean defaultBewitched) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), defaultBewitched, d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }

        @Override @Nonnull
        public CarryType getCarryType() { return data().carryType(); }

        @Override
        public void setCarryType(CarryType carryType) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.comboRoot(),
                carryType != null ? carryType : CarryType.PSO2,
                d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }

        @Override @Nonnull
        public Color getEffectColor() { return new Color(data().effectColor()); }

        @Override
        public void setEffectColor(Color effectColor) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.comboRoot(),
                d.carryType(), effectColor != null ? effectColor.getRGB() : 0xFF3333FF,
                d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }

        @Override
        public boolean isEffectColorInverse() { return data().effectColorInverse(); }

        @Override
        public void setEffectColorInverse(boolean effectColorInverse) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.comboRoot(),
                d.carryType(), d.effectColor(), effectColorInverse, d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }

        @Override @Nonnull
        public Vec3 getAdjust() { return data().adjust(); }

        @Override
        public void setAdjust(Vec3 adjust) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(),
                adjust != null ? adjust : Vec3.ZERO,
                d.texture(), d.model(), d.specialEffects()));
        }

        @Override @Nonnull
        public Optional<ResourceLocation> getTexture() { return data().texture(); }

        @Override
        public void setTexture(ResourceLocation texture) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                Optional.ofNullable(texture), d.model(), d.specialEffects()));
        }

        @Override @Nonnull
        public Optional<ResourceLocation> getModel() { return data().model(); }

        @Override
        public void setModel(ResourceLocation model) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), Optional.ofNullable(model), d.specialEffects()));
        }

        @Override
        public ResourceLocation getComboRoot() {
            ResourceLocation root = data().comboRoot();
            if (root == null || !ComboStateRegistry.REGISTRY.containsKey(root)) {
                return ComboStateRegistry.STANDBY.getId();
            }
            return root;
        }

        @Override
        public void setComboRoot(ResourceLocation resourceLocation) {
            ResourceLocation resolved = ComboStateRegistry.REGISTRY.containsKey(resourceLocation)
                ? resourceLocation : ComboStateRegistry.STANDBY.getId();
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), resolved,
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }

        @Override
        public Collection<ResourceLocation> getSpecialEffects() { return data().specialEffects(); }

        @Override
        public void setSpecialEffects(ListTag list) {
            List<ResourceLocation> result = new ArrayList<>();
            if (list != null) {
                list.forEach(tag -> {
                    ResourceLocation se = ResourceLocation.tryParse(tag.getAsString());
                    if (se != null && SpecialEffectsRegistry.REGISTRY.containsKey(se)) {
                        result.add(se);
                    }
                });
            }
            update(d -> d.withSpecialEffects(result));
        }

        @Override
        public boolean addSpecialEffect(ResourceLocation se) {
            if (!SpecialEffectsRegistry.REGISTRY.containsKey(se)) return false;
            BladeStateData current = data();
            if (current.specialEffects().contains(se)) return false;
            List<ResourceLocation> updated = new ArrayList<>(current.specialEffects());
            updated.add(se);
            update(d -> d.withSpecialEffects(updated));
            return true;
        }

        @Override
        public boolean removeSpecialEffect(ResourceLocation se) {
            BladeStateData current = data();
            if (!current.specialEffects().contains(se)) return false;
            List<ResourceLocation> updated = new ArrayList<>(current.specialEffects());
            updated.remove(se);
            update(d -> d.withSpecialEffects(updated));
            return true;
        }

        @Override
        public boolean hasSpecialEffect(ResourceLocation se) {
            if (!SpecialEffectsRegistry.REGISTRY.containsKey(se)) {
                removeSpecialEffect(se);
                return false;
            }
            return data().specialEffects().contains(se);
        }

        // ========== Vanilla durability (routed to DataComponents) ==========

        @Override
        public int getDamage() { return stack.getDamageValue(); }

        @Override
        public void setDamage(int damage) {
            int maxDamage = stack.getMaxDamage();
            BladeStateData d = data();
            if (d.broken()) {
                if (damage <= 0 && !d.sealed()) {
                    setBroken(false);
                } else if (maxDamage < damage) {
                    damage = Math.min(damage, maxDamage - 1);
                }
            }
            stack.set(DataComponents.DAMAGE, Mth.clamp(damage, 0, stack.getMaxDamage()));
        }

        @Override
        public int getMaxDamage() { return stack.getMaxDamage(); }

        @Override
        public void setMaxDamage(int damage) {
            stack.set(DataComponents.MAX_DAMAGE, Math.max(1, damage));
        }

        // ========== Runtime fields (BladeRuntimeState) ==========

        @Override
        public long getLastActionTime() { return runtime().getLastActionTime(); }

        @Override
        public void setLastActionTime(long lastActionTime) { runtime().setLastActionTime(lastActionTime); }

        @Override
        public boolean onClick() { return runtime().isOnClick(); }

        @Override
        public void setOnClick(boolean onClick) { runtime().setOnClick(onClick); }

        @Override
        public float getFallDecreaseRate() { return runtime().getFallDecreaseRate(); }

        @Override
        public void setFallDecreaseRate(float fallDecreaseRate) { runtime().setFallDecreaseRate(fallDecreaseRate); }

        @Override
        public float getAttackAmplifier() { return runtime().getAttackAmplifier(); }

        @Override
        public void setAttackAmplifier(float attackAmplifier) { runtime().setAttackAmplifier(attackAmplifier); }

        @Override
        public ResourceLocation getComboSeq() {
            ResourceLocation seq = runtime().getComboSeq();
            return seq != null ? seq : ComboStateRegistry.NONE.getId();
        }

        @Override
        public void setComboSeq(ResourceLocation comboSeq) { runtime().setComboSeq(comboSeq); }

        @Override
        public int getTargetEntityId() { return runtime().getTargetEntityId(); }

        @Override
        public void setTargetEntityId(int id) { runtime().setTargetEntityId(id); }

        // ========== Lifecycle ==========

        @Override
        public boolean isEmpty() {
            return !stack.has(SlashBladeDataComponents.BLADE_STATE_DATA.get());
        }

        @Override
        public void setNonEmpty() {
            if (isEmpty()) {
                BladeStateData data = BladeStateData.DEFAULT;
                if (stack.getMaxDamage() <= 0) {
                    stack.set(DataComponents.MAX_DAMAGE, 40);
                }
                stack.set(SlashBladeDataComponents.BLADE_STATE_DATA.get(), data);
            }
        }

        // ========== NBT serialization (compat bridge) ==========

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            BladeStateData d = data();

            tag.putString("translationKey", d.translationKey());
            tag.putFloat("baseAttackModifier", d.baseAttackModifier());
            tag.putInt("proudSoul", d.proudSoul());
            tag.putInt("killCount", d.killCount());
            tag.putInt("RepairCounter", d.refine());
            tag.putBoolean("isBroken", d.broken());
            tag.putBoolean("isSealed", d.sealed());
            tag.putString("SpecialAttackType", d.slashArtsKey().toString());
            tag.putBoolean("isDefaultBewitched", d.defaultBewitched());
            tag.putByte("StandbyRenderType", (byte) d.carryType().ordinal());
            tag.putInt("SummonedSwordColor", d.effectColor());
            tag.putBoolean("SummonedSwordColorInverse", d.effectColorInverse());
            tag.put("adjustXYZ", NBTHelper.newDoubleNBTList(d.adjust()));
            d.texture().ifPresent(loc -> tag.putString("TextureName", loc.toString()));
            d.model().ifPresent(loc -> tag.putString("ModelName", loc.toString()));
            tag.putString("ComboRoot", d.comboRoot().toString());

            if (!d.specialEffects().isEmpty()) {
                ListTag seList = new ListTag();
                d.specialEffects().forEach(se -> seList.add(StringTag.valueOf(se.toString())));
                tag.put("SpecialEffects", seList);
            }

            BladeRuntimeState rt = runtime();
            tag.putLong("lastActionTime", rt.getLastActionTime());
            tag.putInt("TargetEntity", rt.getTargetEntityId());
            tag.putBoolean("_onClick", rt.isOnClick());
            tag.putFloat("fallDecreaseRate", rt.getFallDecreaseRate());
            tag.putFloat("AttackAmplifier", rt.getAttackAmplifier());
            tag.putString("currentCombo", rt.getComboSeq().toString());
            tag.putInt("Damage", stack.getDamageValue());
            tag.putInt("maxDamage", stack.getMaxDamage());

            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag == null) return;
            setNonEmpty();

            BladeStateData d = data();
            String translationKey = tag.contains("translationKey") ? tag.getString("translationKey") : d.translationKey();
            float baseAttack = tag.contains("baseAttackModifier") ? tag.getFloat("baseAttackModifier") : d.baseAttackModifier();
            int proudSoul = tag.contains("proudSoul") ? tag.getInt("proudSoul") : d.proudSoul();
            int killCount = tag.contains("killCount") ? tag.getInt("killCount") : d.killCount();
            int refine = tag.contains("RepairCounter") ? tag.getInt("RepairCounter") : d.refine();
            boolean broken = tag.contains("isBroken") ? tag.getBoolean("isBroken") : d.broken();
            boolean sealed = tag.contains("isSealed") ? tag.getBoolean("isSealed") : d.sealed();
            ResourceLocation slashArts = tag.contains("SpecialAttackType")
                ? ResourceLocation.tryParse(tag.getString("SpecialAttackType")) : d.slashArtsKey();
            boolean defaultBewitched = tag.contains("isDefaultBewitched") ? tag.getBoolean("isDefaultBewitched") : d.defaultBewitched();
            CarryType carryType = d.carryType();
            if (tag.contains("StandbyRenderType")) {
                int ordinal = tag.getByte("StandbyRenderType");
                CarryType[] values = CarryType.values();
                if (ordinal >= 0 && ordinal < values.length) {
                    carryType = values[ordinal];
                }
            }
            int color = tag.contains("SummonedSwordColor") ? tag.getInt("SummonedSwordColor") : d.effectColor();
            boolean colorInverse = tag.contains("SummonedSwordColorInverse") ? tag.getBoolean("SummonedSwordColorInverse") : d.effectColorInverse();
            Vec3 adjust = tag.contains("adjustXYZ") ? NBTHelper.getVector3d(tag, "adjustXYZ") : d.adjust();

            Optional<ResourceLocation> texture = tag.contains("TextureName")
                ? Optional.ofNullable(ResourceLocation.tryParse(tag.getString("TextureName"))) : d.texture();
            Optional<ResourceLocation> model = tag.contains("ModelName")
                ? Optional.ofNullable(ResourceLocation.tryParse(tag.getString("ModelName"))) : d.model();

            ResourceLocation comboRoot = tag.contains("ComboRoot")
                ? ResourceLocation.tryParse(tag.getString("ComboRoot")) : d.comboRoot();
            if (comboRoot == null) comboRoot = BladeStateData.DEFAULT_COMBO_ROOT;

            List<ResourceLocation> effects;
            if (tag.contains("SpecialEffects")) {
                List<ResourceLocation> newEffects = new ArrayList<>();
                ListTag list = tag.getList("SpecialEffects", 8);
                for (int i = 0; i < list.size(); i++) {
                    ResourceLocation se = ResourceLocation.tryParse(list.getString(i));
                    if (se != null) newEffects.add(se);
                }
                effects = newEffects;
            } else {
                effects = new ArrayList<>(d.specialEffects());
            }

            BladeStateData newData = new BladeStateData(translationKey, baseAttack, proudSoul, killCount,
                refine, broken, sealed, slashArts != null ? slashArts : BladeStateData.DEFAULT_SLASH_ARTS,
                defaultBewitched, comboRoot, carryType, color, colorInverse, adjust,
                texture, model, effects);
            stack.set(SlashBladeDataComponents.BLADE_STATE_DATA.get(), newData);

            if (tag.contains("maxDamage")) stack.set(DataComponents.MAX_DAMAGE, Math.max(1, tag.getInt("maxDamage")));
            if (tag.contains("Damage")) stack.setDamageValue(tag.getInt("Damage"));

            BladeRuntimeState rt = runtime();
            if (tag.contains("lastActionTime")) rt.setLastActionTime(tag.getLong("lastActionTime"));
            if (tag.contains("TargetEntity")) rt.setTargetEntityId(tag.getInt("TargetEntity"));
            if (tag.contains("_onClick")) rt.setOnClick(tag.getBoolean("_onClick"));
            if (tag.contains("fallDecreaseRate")) rt.setFallDecreaseRate(tag.getFloat("fallDecreaseRate"));
            if (tag.contains("AttackAmplifier")) rt.setAttackAmplifier(tag.getFloat("AttackAmplifier"));
            if (tag.contains("currentCombo")) rt.setComboSeq(ResourceLocation.tryParse(tag.getString("currentCombo")));
        }
    }
}
