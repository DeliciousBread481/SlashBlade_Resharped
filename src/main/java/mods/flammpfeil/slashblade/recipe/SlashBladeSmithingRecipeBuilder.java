package mods.flammpfeil.slashblade.recipe;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.LinkedHashMap;
import java.util.Map;

public class SlashBladeSmithingRecipeBuilder {

    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;
    private final RecipeCategory category;
    private final ResourceLocation result;
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    public SlashBladeSmithingRecipeBuilder(Ingredient template, Ingredient base,
                                            Ingredient addition, RecipeCategory category, ResourceLocation result) {
        this.category = category;
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    public static SlashBladeSmithingRecipeBuilder smithing(Ingredient template, Ingredient base,
                                                             Ingredient addition, RecipeCategory category, ResourceLocation result) {
        return new SlashBladeSmithingRecipeBuilder(template, base, addition,
                category, result);
    }

    public SlashBladeSmithingRecipeBuilder unlocks(String name, Criterion<?> trigger) {
        this.criteria.put(name, trigger);
        this.advancement.addCriterion(name, trigger);
        return this;
    }

    public void save(RecipeOutput output, String name) {
        this.save(output, ResourceLocation.parse(name));
    }

    public void save(RecipeOutput output, ResourceLocation id) {
        this.ensureValid(id);
        Advancement.Builder advancementBuilder = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancementBuilder::addCriterion);

        SlashBladeSmithingRecipe recipe = new SlashBladeSmithingRecipe(
                id, this.result, this.template, this.base, this.addition
        );

        output.accept(id, recipe, advancementBuilder.build(
                id.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private void ensureValid(ResourceLocation id) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }
}
