package mods.flammpfeil.slashblade;

import com.google.common.base.CaseFormat;
import mods.flammpfeil.slashblade.ability.*;
import mods.flammpfeil.slashblade.advancement.SlashBladeItemPredicate;
import mods.flammpfeil.slashblade.capability.concentrationrank.CapabilityConcentrationRank;
import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.capability.mobeffect.CapabilityMobEffect;
import mods.flammpfeil.slashblade.capability.slashblade.CapabilitySlashBlade;
import mods.flammpfeil.slashblade.client.renderer.entity.*;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.entity.*;
import mods.flammpfeil.slashblade.event.BladeMotionEventBroadcaster;
import mods.flammpfeil.slashblade.event.handler.*;
import mods.flammpfeil.slashblade.network.NetworkManager;
import mods.flammpfeil.slashblade.recipe.RecipeSerializerRegistry;
import mods.flammpfeil.slashblade.registry.*;
import mods.flammpfeil.slashblade.registry.combo.ComboCommands;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import mods.flammpfeil.slashblade.util.TargetSelector;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SlashBlade.MODID)
public class SlashBlade {
    public static final String MODID = "slashblade";

    public static ResourceLocation prefix(String path) {
        return new ResourceLocation(SlashBlade.MODID, path);
    }

    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public SlashBlade() {
        // Register the setup method for modloading
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SlashBladeConfig.COMMON_CONFIG);

        modEventBus.addListener(this::setup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        ModAttributes.ATTRIBUTES.register(modEventBus);
        NetworkManager.register();

        SlashBladeItems.ITEMS.register(modEventBus);
        ComboStateRegistry.COMBO_STATE.register(modEventBus);
        SlashArtsRegistry.SLASH_ARTS.register(modEventBus);
        SlashBladeCreativeGroup.CREATIVE_MODE_TABS.register(modEventBus);
        RecipeSerializerRegistry.RECIPE_TYPES.register(modEventBus);
        RecipeSerializerRegistry.RECIPE_SERIALIZER.register(modEventBus);
        SpecialEffectsRegistry.SPECIAL_EFFECT.register(modEventBus);

        ItemPredicate.register(SlashBlade.prefix("slashblade"), SlashBladeItemPredicate::new);
    }

    private void setup(final FMLCommonSetupEvent event) {

        MinecraftForge.EVENT_BUS.addListener(KnockBackHandler::onLivingKnockBack);

        FallHandler.getInstance().register();
        LockOnManager.getInstance().register();
        Guard.getInstance().register();

        MinecraftForge.EVENT_BUS.register(new CapabilityAttachHandler());
        MinecraftForge.EVENT_BUS.register(new StunManager());

        RefineHandler.getInstance().register();
        KillCounter.getInstance().register();
        RankPointHandler.getInstance().register();
        AllowFlightOverrwrite.getInstance().register();
        BlockPickCanceller.getInstance().register();
        BladeMotionEventBroadcaster.getInstance().register();

        MinecraftForge.EVENT_BUS.addListener(TargetSelector::onInputChange);
        SummonedSwordArts.getInstance().register();
        SlayerStyleArts.getInstance().register();
        Untouchable.getInstance().register();
        EnemyStep.getInstance().register();
        KickJump.getInstance().register();
        SuperSlashArts.getInstance().register();

        ComboCommands.initDefaultStandByCommands();


    }

    // You can use EventBusSubscriber to automatically subscribe events on the
    // contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

        public static final ResourceLocation BladeItemEntityLoc = new ResourceLocation(SlashBlade.MODID,
                classToString(BladeItemEntity.class));
        public static EntityType<BladeItemEntity> BladeItem;

        public static final ResourceLocation BladeStandEntityLoc = new ResourceLocation(SlashBlade.MODID,
                classToString(BladeStandEntity.class));
        public static EntityType<BladeStandEntity> BladeStand;

        public static final ResourceLocation SummonedSwordLoc = new ResourceLocation(SlashBlade.MODID,
                classToString(EntityAbstractSummonedSword.class));
        public static EntityType<EntityAbstractSummonedSword> SummonedSword;
        public static final ResourceLocation SpiralSwordsLoc = new ResourceLocation(SlashBlade.MODID,
                classToString(EntitySpiralSwords.class));
        public static EntityType<EntitySpiralSwords> SpiralSwords;

        public static final ResourceLocation StormSwordsLoc = new ResourceLocation(SlashBlade.MODID,
                classToString(EntityStormSwords.class));
        public static EntityType<EntityStormSwords> StormSwords;
        public static final ResourceLocation BlisteringSwordsLoc = new ResourceLocation(SlashBlade.MODID,
                classToString(EntityBlisteringSwords.class));
        public static EntityType<EntityBlisteringSwords> BlisteringSwords;
        public static final ResourceLocation HeavyRainSwordsLoc = new ResourceLocation(SlashBlade.MODID,
                classToString(EntityHeavyRainSwords.class));
        public static EntityType<EntityHeavyRainSwords> HeavyRainSwords;

        public static final ResourceLocation JudgementCutLoc = new ResourceLocation(SlashBlade.MODID,
                classToString(EntityJudgementCut.class));
        public static EntityType<EntityJudgementCut> JudgementCut;

        public static final ResourceLocation SlashEffectLoc = new ResourceLocation(SlashBlade.MODID,
                classToString(EntitySlashEffect.class));
        public static EntityType<EntitySlashEffect> SlashEffect;

        public static final ResourceLocation DriveLoc = new ResourceLocation(SlashBlade.MODID,
                classToString(EntityDrive.class));
        public static EntityType<EntityDrive> Drive;


        @SubscribeEvent
        public static void register(RegisterEvent event) {

            event.register(ForgeRegistries.Keys.ENTITY_TYPES, helper -> {
                {
                    EntityType<EntityAbstractSummonedSword> entity = SummonedSword = EntityType.Builder
                            .of(EntityAbstractSummonedSword::new, MobCategory.MISC).sized(0.5F, 0.5F)
                            .setTrackingRange(4).setUpdateInterval(20)
                            .setCustomClientFactory(EntityAbstractSummonedSword::createInstance)
                            .build(SummonedSwordLoc.toString());
                    helper.register(SummonedSwordLoc, entity);
                }

                {
                    EntityType<EntityStormSwords> entity = StormSwords = EntityType.Builder
                            .of(EntityStormSwords::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(4)
                            .setUpdateInterval(20).setCustomClientFactory(EntityStormSwords::createInstance)
                            .build(StormSwordsLoc.toString());
                    helper.register(StormSwordsLoc, entity);
                }

                {
                    EntityType<EntitySpiralSwords> entity = SpiralSwords = EntityType.Builder
                            .of(EntitySpiralSwords::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(4)
                            .setUpdateInterval(20).setCustomClientFactory(EntitySpiralSwords::createInstance)
                            .build(SpiralSwordsLoc.toString());
                    helper.register(SpiralSwordsLoc, entity);
                }

                {
                    EntityType<EntityBlisteringSwords> entity = BlisteringSwords = EntityType.Builder
                            .of(EntityBlisteringSwords::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(4)
                            .setUpdateInterval(20).setCustomClientFactory(EntityBlisteringSwords::createInstance)
                            .build(BlisteringSwordsLoc.toString());
                    helper.register(BlisteringSwordsLoc, entity);
                }

                {
                    EntityType<EntityHeavyRainSwords> entity = HeavyRainSwords = EntityType.Builder
                            .of(EntityHeavyRainSwords::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(4)
                            .setUpdateInterval(20).setCustomClientFactory(EntityHeavyRainSwords::createInstance)
                            .build(HeavyRainSwordsLoc.toString());
                    helper.register(HeavyRainSwordsLoc, entity);
                }

                {
                    EntityType<EntityJudgementCut> entity = JudgementCut = EntityType.Builder
                            .of(EntityJudgementCut::new, MobCategory.MISC).sized(2.5F, 2.5F).setTrackingRange(4)
                            .setUpdateInterval(20).setCustomClientFactory(EntityJudgementCut::createInstance)
                            .build(JudgementCutLoc.toString());
                    helper.register(JudgementCutLoc, entity);
                }

                {
                    EntityType<BladeItemEntity> entity = BladeItem = EntityType.Builder
                            .of(BladeItemEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).setTrackingRange(4)
                            .setUpdateInterval(20).setCustomClientFactory(BladeItemEntity::createInstanceFromPacket)
                            .build(BladeItemEntityLoc.toString());
                    helper.register(BladeItemEntityLoc, entity);
                }

                {
                    EntityType<BladeStandEntity> entity = BladeStand = EntityType.Builder
                            .of(BladeStandEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(10)
                            .setUpdateInterval(20).setShouldReceiveVelocityUpdates(false)
                            .setCustomClientFactory(BladeStandEntity::createInstance)
                            .build(BladeStandEntityLoc.toString());
                    helper.register(BladeStandEntityLoc, entity);
                }

                {
                    EntityType<EntitySlashEffect> entity = SlashEffect = EntityType.Builder
                            .of(EntitySlashEffect::new, MobCategory.MISC).sized(3.0F, 3.0F).setTrackingRange(4)
                            .setUpdateInterval(20).setCustomClientFactory(EntitySlashEffect::createInstance)
                            .build(SlashEffectLoc.toString());
                    helper.register(SlashEffectLoc, entity);
                }

                {
                    EntityType<EntityDrive> entity = Drive = EntityType.Builder.of(EntityDrive::new, MobCategory.MISC)
                            .sized(3.0F, 3.0F).setTrackingRange(4).setUpdateInterval(20)
                            .setCustomClientFactory(EntityDrive::createInstance).build(DriveLoc.toString());
                    helper.register(DriveLoc, entity);
                }

            });

            event.register(ForgeRegistries.Keys.STAT_TYPES, helper -> SWORD_SUMMONED = registerCustomStat("sword_summoned"));

        }

        private static String classToString(Class<? extends Entity> entityClass) {
            return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entityClass.getSimpleName())
                    .replace("entity_", "");
        }

        @SubscribeEvent
        public static void onRegisterRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(RegistryEvents.SummonedSword, SummonedSwordRenderer::new);
            event.registerEntityRenderer(RegistryEvents.StormSwords, SummonedSwordRenderer::new);
            event.registerEntityRenderer(RegistryEvents.SpiralSwords, SummonedSwordRenderer::new);
            event.registerEntityRenderer(RegistryEvents.BlisteringSwords, SummonedSwordRenderer::new);
            event.registerEntityRenderer(RegistryEvents.HeavyRainSwords, SummonedSwordRenderer::new);
            event.registerEntityRenderer(RegistryEvents.JudgementCut, JudgementCutRenderer::new);
            event.registerEntityRenderer(RegistryEvents.BladeItem, BladeItemEntityRenderer::new);
            event.registerEntityRenderer(RegistryEvents.BladeStand, BladeStandEntityRenderer::new);
            event.registerEntityRenderer(RegistryEvents.SlashEffect, SlashEffectRenderer::new);
            event.registerEntityRenderer(RegistryEvents.Drive, DriveRenderer::new);

        }

        @SubscribeEvent
        public static void onRegisterCapability(final RegisterCapabilitiesEvent event) {
            CapabilitySlashBlade.register(event);
            CapabilityMobEffect.register(event);
            CapabilityInputState.register(event);
            CapabilityConcentrationRank.register(event);
        }

        @SubscribeEvent
        public static void onEntityAttributeModificationEvent(final EntityAttributeModificationEvent event) {
            event.add(EntityType.PLAYER, ModAttributes.SLASHBLADE_DAMAGE.get());
        }

        public static ResourceLocation SWORD_SUMMONED;

        private static ResourceLocation registerCustomStat(String name) {
            ResourceLocation resourcelocation = new ResourceLocation(MODID, name);
            Registry.register(BuiltInRegistries.CUSTOM_STAT, name, resourcelocation);
            Stats.CUSTOM.get(resourcelocation, StatFormatter.DEFAULT);
            return resourcelocation;
        }

        /*
          /scoreboard objectives add stat minecraft.custom:slashblade.sword_summoned
          /scoreboard objectives setdisplay sidebar stat
         */
    }

    public static Registry<SlashBladeDefinition> getSlashBladeDefinitionRegistry(Level level) {
        if (level.isClientSide()) {
            return BladeModelManager.getClientSlashBladeRegistry();
        }
        return level.registryAccess().registryOrThrow(SlashBladeDefinition.REGISTRY_KEY);
    }

    public static HolderLookup.RegistryLookup<SlashBladeDefinition> getSlashBladeDefinitionRegistry(HolderLookup.Provider access) {
        return access.lookupOrThrow(SlashBladeDefinition.REGISTRY_KEY);
    }
}
