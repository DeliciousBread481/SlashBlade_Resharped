package mods.flammpfeil.slashblade.recipe;

import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class SlashBladeShapedRecipe extends ShapedRecipe {

    public static final RecipeSerializer<SlashBladeShapedRecipe> SERIALIZER =
            new SlashBladeShapedRecipeSerializer();

    private final ShapedRecipePattern pattern;
    private final ItemStack resultStack;
    private final ResourceLocation outputBlade;

    public SlashBladeShapedRecipe(String group, CraftingBookCategory category,
                                  ShapedRecipePattern pattern, ItemStack result,
                                  Optional<ResourceLocation> outputBlade) {
        super(group, category, pattern, result);
        this.pattern = pattern;
        this.resultStack = result;
        this.outputBlade = outputBlade.orElse(null);
    }

    ShapedRecipePattern getPattern() {
        return pattern;
    }

    ItemStack getResultStack() {
        return resultStack;
    }

    private static ItemStack getResultBlade(ResourceLocation outputBlade) {
        Item bladeItem = BuiltInRegistries.ITEM.containsKey(outputBlade) ? 
        		BuiltInRegistries.ITEM.get(outputBlade)
                : SlashBladeItems.SLASHBLADE.get();

        return Objects.requireNonNullElseGet(bladeItem, SlashBladeItems.SLASHBLADE).getDefaultInstance();
    }

    @Nullable
    public ResourceLocation getOutputBlade() {
        return outputBlade;
    }

    private ResourceKey<SlashBladeDefinition> getOutputBladeKey() {
        return ResourceKey.create(SlashBladeDefinition.REGISTRY_KEY,
                Objects.requireNonNull(outputBlade, "outputBlade must not be null for getOutputBladeKey"));
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider access) {
        ResourceLocation blade = getOutputBlade();
        if (blade == null) {
            return super.getResultItem(access);
        }
        ItemStack result = SlashBladeShapedRecipe.getResultBlade(blade);

        if (!blade.equals(BuiltInRegistries.ITEM.getKey(result.getItem()))) {
            result = access.lookupOrThrow(SlashBladeDefinition.REGISTRY_KEY).getOrThrow(getOutputBladeKey())
                    .value()
                    .getBlade(access);
        }

        return result;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, @NotNull HolderLookup.Provider access) {
        var result = this.getResultItem(access);
        if (!(result.getItem() instanceof ItemSlashBlade)) {
            result = new ItemStack(SlashBladeItems.SLASHBLADE.get());
        }

        var resultState = BladeStateAccess.of(result).orElseThrow(NullPointerException::new);
        boolean sumRefine = SlashBladeConfig.DO_CRAFTING_SUM_REFINE.get();
        int proudSoul = resultState.getProudSoulCount();
        int killCount = resultState.getKillCount();
        int refine = resultState.getRefine();
        for (var stack : input.items()) {
            if (!(stack.getItem() instanceof ItemSlashBlade)) {
                continue;
            }
            var ingredientState = BladeStateAccess.of(stack).orElseThrow(NullPointerException::new);

            proudSoul += ingredientState.getProudSoulCount();
            killCount += ingredientState.getKillCount();
            if (sumRefine) {
                refine += ingredientState.getRefine();
            } else {
                refine = Math.max(refine, ingredientState.getRefine());
            }
            updateEnchantment(result, stack, access);
        }
        resultState.setProudSoulCount(proudSoul);
        resultState.setKillCount(killCount);
        resultState.setRefine(refine);

        return result;
    }

    private void updateEnchantment(ItemStack result, ItemStack ingredient, HolderLookup.Provider access) {
        HolderLookup.RegistryLookup<Enchantment> enchantmentLookup = access.lookupOrThrow(Registries.ENCHANTMENT);
        ItemEnchantments.Mutable newItemEnchants = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(result));
        ItemEnchantments oldItemEnchants = ingredient.getAllEnchantments(enchantmentLookup);
        for (var entry : oldItemEnchants.entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            int destLevel = newItemEnchants.getLevel(enchantment);
            int srcLevel = entry.getIntValue();

            srcLevel = Math.max(srcLevel, destLevel);
            srcLevel = Math.min(srcLevel, enchantment.value().getMaxLevel());

            boolean canApplyFlag = result.supportsEnchantment(enchantment);
            if (canApplyFlag) {
                for (var currentEntry : newItemEnchants.toImmutable().entrySet()) {
                    Holder<Enchantment> currentEnchantment = currentEntry.getKey();
                    if (!currentEnchantment.equals(enchantment)
                            && !Enchantment.areCompatible(enchantment, currentEnchantment)) {
                        canApplyFlag = false;
                        break;
                    }
                }
                if (canApplyFlag) {
                    newItemEnchants.set(enchantment, srcLevel);
                }
            }
        }
        EnchantmentHelper.setEnchantments(result, newItemEnchants.toImmutable());
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

}
