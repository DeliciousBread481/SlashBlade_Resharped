# SlashBlade 附属 API 移植文档

## Minecraft 1.20.1 Forge → 1.21.1 NeoForge

---

## 1. 概述

本文档记录 SlashBlade:Resharpened 从 **1.20.1 Forge** 迁移至 **1.21.1 NeoForge** 后的 API 变更，面向附属模组开发者及需要适配新版本 API 的开发人员。

核心变更: 拔刀剑状态存储从 Forge Capability 迁移至 Minecraft 原生 Data Components，实体数据从 Capability 迁移至 NeoForge AttachmentType，网络层从 SimpleChannel 迁移至 PayloadRegistrar。

---

## 2. 核心 API 变更

### 2.1 ISlashBladeState — 接口变更

**包路径不变** `mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState`。

#### 移除基接口

```java
// ✗ 旧: 继承 INBTSerializable
public interface ISlashBladeState extends INBTSerializable<CompoundTag> {

// ✓ 新: 独立接口
public interface ISlashBladeState {
```

`serializeNBT()` / `deserializeNBT()` 保留为 default 方法用于兼容 (配方复制、JEI 等)，但不属于接口契约的一部分。日常读写不再依赖 NBT。

#### 新增方法

| 方法 | 说明 |
|------|------|
| `long getLastProcessedComboTick()` | 上次处理 Combo 的游戏刻, 用于防止重复处理 |
| `void setLastProcessedComboTick(long tick)` | 设置处理刻 |
| `default void synchronizeComboSeq(LivingEntity, ResourceLocation)` | 同步 Combo 状态到客户端 |
| `default Map.Entry<Integer, RL> peekCurrentComboStateTicks(LivingEntity)` | 窥探当前 Combo tick (不改变状态) |

#### Combo 状态解析拆分

```java
// 新: 分为非变异版和变异版
peekCurrentComboStateTicks(user)    // 只读, 不更新 lastComboTick
resolvCurrentComboStateTicks(user)  // 读写, 会更新 lastComboTick
```

---

### 2.2 BladeStateAccess — 新的状态访问入口 (核心变更)

**文件:** `capability/slashblade/BladeStateAccess.java`

这是**最重要的迁移 API**。旧的 Forge Capability 访问方式已全部废弃。

```java
// ✗ 旧方式: Forge Capability
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
    state.setKillCount(100);
    int refine = state.getRefine();
});

// ✓ 新方式: BladeStateAccess (推荐)
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
var state = BladeStateAccess.of(stack);
state.ifPresent(s -> {
    s.setKillCount(100);
    int refine = s.getRefine();
});
```

#### API 签名

```java
public class BladeStateAccess {
    /**
     * 获取拔刀剑状态的 Optional 包装。
     * 仅当 stack 非空且物品为 ItemSlashBlade 或其子类时返回有效值。
     * 对于未初始化的堆栈会调用 ensureComponent 自动创建默认 BladeStateData。
     */
    public static Optional<ISlashBladeState> of(ItemStack stack);

    /**
     * 内部类, 实现 ISlashBladeState, 将 ItemStack 的 DataComponent 包装为 ISlashBladeState。
     * 每个 getter 从 stack 读取 DataComponent, 每个 setter 通过 stack.update() 写入。
     */
    private static class ComponentBackedState implements ISlashBladeState { ... }
}
```

#### 注意事项

- `BladeStateAccess.of(stack)` 返回 `Optional<ISlashBladeState>`
- 堆栈为空气或非拔刀剑物品时返回 `Optional.empty()`
- `ItemSlashBlade.BLADESTATE`、`ItemSlashBlade.INPUT_STATE` 等旧 Capability 常**已删除**
- 不再需要 `LazyOptional`, 不再需要 null 检查 `getCapability` 返回值

---

### 2.3 BladeStateData / BladeRuntimeStateData — 不可变数据记录

旧的 `SlashBladeState` (可变 POJO, ~20 个实例字段) 已被两个不可变 `record` 取代:

#### BladeStateData — 持久化状态

**文件:** `capability/slashblade/BladeStateData.java`

```java
public record BladeStateData(
    String translationKey,           // 译名键
    float baseAttackModifier,        // 基础攻击修正 (默认 4.0F)
    int proudSoul,                   // 耀魂值
    int killCount,                   // 击杀数
    int refine,                      // 锻造数
    boolean broken,                  // 是否折断
    boolean sealed,                  // 是否封印
    ResourceLocation slashArtsKey,   // SA 键
    boolean defaultBewitched,        // 默认附魔
    ResourceLocation comboRoot,      // Combo 根节点 (默认 standby)
    CarryType carryType,             // 携带姿态
    int effectColor,                 // 特效颜色 (ARGB)
    boolean effectColorInverse,      // 颜色反转
    Vec3 adjust,                     // 模型调整偏移
    Optional<ResourceLocation> texture,  // 纹理
    Optional<ResourceLocation> model,    // 模型
    List<ResourceLocation> specialEffects  // 特殊效果列表
)
```

#### BladeRuntimeStateData — 运行时状态

**文件:** `capability/slashblade/BladeRuntimeStateData.java`

```java
public record BladeRuntimeStateData(
    ResourceLocation comboSeq,       // 当前 combo 序列
    long lastActionTime,             // 上次动作时间
    long lastProcessedComboTick,     // 上次处理的 combo tick
    int targetEntityId,              // 锁定目标
    boolean onClick,                 // 是否有点击
    float fallDecreaseRate,          // 下落减速
    float attackAmplifier            // 伤害放大 (当前)
)
```

#### 读写模式 (通过 ComponentBackedState)

```java
// 读取: 直接调用 getter (底层读取 DataComponent)
int killCount = state.getKillCount();

// 写入: 通过 setter (底层创建新 record 并 stack.update)
state.setKillCount(killCount + 1);   // 内部: stack.update(BLADE_STATE_DATA, d -> new BladeStateData(..., newKillCount, ...));
state.setRefine(refine + 1);

// 损伤值: 通过 getDamage/setDamage (使用原版 DataComponents)
state.setDamage(5);
int dmg = state.getDamage();
```

---

### 2.4 SlashBladeDataComponents — 数据组件注册

**文件:** `capability/slashblade/SlashBladeDataComponents.java`

```java
public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
    DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, SlashBlade.MODID);

public static final DeferredHolder<..., DataComponentType<BladeStateData>> BLADE_STATE_DATA = ...;
public static final DeferredHolder<..., DataComponentType<BladeRuntimeStateData>> BLADE_RUNTIME_STATE = ...;
```

附属模组一般不需要直接操作 `DeferredRegistry` 或 DataComponentType, 使用 `BladeStateAccess.of()` 即可。

---

## 3. 物品 API 变更

### 3.1 获取拔刀剑状态

```java
// ✗ 旧
stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> { ... });
stack.getCapability(ItemSlashBlade.BLADESTATE).orElseThrow(NullPointerException::new);

// ✓ 新
BladeStateAccess.of(stack).ifPresent(state -> { ... });
BladeStateAccess.of(stack).orElseThrow();
```

### 3.2 损伤值 (Damage)

损伤值现在直接存储在 `DataComponents.DAMAGE` / `DataComponents.MAX_DAMAGE` 中, 与 Minecraft 原版一致。

```java
// 旧: 在 SlashBladeState 字段上存储
state.getDamage()
state.getMaxDamage()
state.setDamage(dmg)
state.setMaxDamage(max)

// 新: ComponentBackedState 方法委托到原版 DataComponents
// 签名不变, 但底层变化:
//   state.getDamage()     → stack.getDamageValue()
//   state.getMaxDamage()  → stack.getMaxDamage()
//   state.setDamage(dmg)  → stack.set(DataComponents.DAMAGE, dmg)
//   state.setMaxDamage(m) → stack.set(DataComponents.MAX_DAMAGE, m)
```

#### `ItemSlashBlade.getDamage(ItemStack)` / `getMaxDamage(ItemStack)` 不再被覆写

旧版覆写了 `getDamage`/`getMaxDamage` 从 Capability 读取; 新版依赖原版 SwordItem 实现, 直接使用 `DataComponents`.

### 3.3 属性修改器 (Attribute Modifiers)

**方法签名更改:**

```java
// ✗ 旧
@Deprecated
public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack)

// ✓ 新
@Override
public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack)
```

**关键常量变更:**

| 含义 | 旧 | 新 |
|------|----|----|
| 攻击伤害 UUID | `ATTACK_DAMAGE_AMPLIFIER` (自定义) | `Item.BASE_ATTACK_DAMAGE_ID` (原版) |
| 操作类型 | `Operation.ADDITION` | `Operation.ADD_VALUE` |
| 槽位 | `EquipmentSlot.MAINHAND` | `EquipmentSlotGroup.MAINHAND` |
| 触及距离属性 | `ForgeMod.ENTITY_REACH` | `Attributes.ENTITY_INTERACTION_RANGE` |
| 返回类型 | `Multimap<Attribute, AttributeModifier>` | `ItemAttributeModifiers` |

**注册物品时的 Properties 写法:**

```java
// 旧
new Item.Properties().defaultDurability(...).rarity(...)

// 新 (不变)
new Item.Properties().durability(...).rarity(...)
```

### 3.4 initCapabilities / getShareTag / readShareTag — 全部移除

```java
// ✗ 以下方法在新版 ItemSlashBlade 中不存在:
@Override public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt)
@Override public CompoundTag getShareTag(ItemStack stack)
@Override public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt)
```

数据同步由 `DataComponentType` 的 `networkSynchronized(StreamCodec)` 自动处理。无需手动同步。

### 3.5 输入端状态 (Input State)

```java
// ✗ 旧: 从 Capability 字段获取
public static final Capability<IInputState> INPUT_STATE = CapabilityManager.get(new CapabilityToken<>() {});
player.getCapability(INPUT_STATE).ifPresent(s -> s.getCommands().add(InputCommand.R_CLICK));

// ✓ 新: 从实体 AttachmentType 获取
import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
player.getData(CapabilityInputState.INPUT_STATE.get()).getCommands().add(InputCommand.R_CLICK);
```

---

## 4. 实体数据 API

### 4.1 从 Capability 迁移至 AttachmentType

三个实体数据附件均已完成迁移:

| 类 | 注册方法 | 访问方式 |
|----|---------|---------|
| `CapabilityInputState` | `AttachmentType.builder(InputState::new)` | `entity.getData(INPUT_STATE.get())` |
| `CapabilityMobEffect` | `AttachmentType.builder(MobEffectState::new)` | `entity.getData(MOB_EFFECT.get())` |
| `CapabilityConcentrationRank` | `AttachmentType.builder(ConcentrationRank::new)` | `entity.getData(CONCENTRATION_RANK.get())` |

### 4.2 旧 API 对照

```java
// ✗ 旧
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;

public static final Capability<IInputState> INPUT_STATE = CapabilityManager.get(new CapabilityToken<>() {});
event.addCapability(key, new InputStateCapabilityProvider());
player.getCapability(CapabilityInputState.INPUT_STATE).ifPresent(s -> { ... });
```

```java
// ✓ 新
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
    DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, SlashBlade.MODID);

public static final Supplier<AttachmentType<IInputState>> INPUT_STATE =
    ATTACHMENT_TYPES.register("input_state",
        () -> AttachmentType.<IInputState>builder(InputState::new).build());

// 无需 AttachCapabilitiesEvent 处理器! 自动附加
player.getData(CapabilityInputState.INPUT_STATE.get());
```

### 4.3 已删除的类

以下 Forge Capability 相关类已删除:

- `NamedBladeStateCapabilityProvider`
- `InputStateCapabilityProvider`
- `MobEffectCapabilityProvider`
- `ConcentrationRankCapabilityProvider`
- `CapabilityAttachHandler`
- `CapabilitySlashBlade`

---

## 5. 事件系统

### 5.1 事件总线

```java
// ✗ 旧
import net.minecraftforge.common.MinecraftForge;
MinecraftForge.EVENT_BUS.register(this);
MinecraftForge.EVENT_BUS.post(event);

// ✓ 新
import net.neoforged.neoforge.common.NeoForge;
NeoForge.EVENT_BUS.register(this);
NeoForge.EVENT_BUS.post(event);
```

### 5.2 可取消事件

```java
// ✗ 旧: @Cancelable 注解
import net.minecraftforge.eventbus.api.Cancelable;
@Cancelable
public static class BreakEvent extends SlashBladeEvent { ... }

// ✓ 新: 实现 ICancellableEvent 接口
import net.neoforged.neoforge.common.ICancellableEvent;
public static class BreakEvent extends SlashBladeEvent implements ICancellableEvent { ... }
```

### 5.3 SlashBladeEvent 子类一览

所有 SlashBlade 自定义事件位于 `event/SlashBladeEvent.java`:

| 事件类 | 继承 | 说明 |
|--------|------|------|
| `UpdateAttackEvent` | SlashBladeEvent | 攻击力更新, double newDamage (可修改) |
| `HitEvent` | SlashBladeEvent (implements ICancellableEvent) | 将要命中实体时 |
| `BreakEvent` | SlashBladeEvent (implements ICancellableEvent) | 将要折断时 |
| `NextComboEvent` | SlashBladeEvent (implements ICancellableEvent) | 下一个 Combo 切换 (修改 combo) |
| `DropEvent` | SlashBladeEvent | 被丢弃/掉落 |
| `BladeMotionEvent` | (独立类) | 动作变更事件, 现持有一个 `actionTime` 参数 |

```java
// 监听 BladeMotionEvent
NeoForge.EVENT_BUS.addListener((BladeMotionEvent event) -> {
    LivingEntity entity = event.getEntity();
    ResourceLocation combo = event.getCombo();
    long actionTime = event.getActionTime();  // 新参数
});
```

### 5.4 EventBusSubscriber 注解

```java
// ✗ 旧 (内部类)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public static class RegistryEvents { ... }

// ✓ 新 (顶层类)
@EventBusSubscriber(modid = SlashBlade.MODID)
public class RegistryEvents { ... }
```

---

## 6. 网络 API

### 6.1 发送消息

```java
// ✗ 旧: SimpleChannel
import mods.flammpfeil.slashblade.network.NetworkManager;
NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);

// ✓ 新: 直接使用 PacketDistributor
import net.neoforged.neoforge.network.PacketDistributor;
PacketDistributor.sendToPlayer((ServerPlayer) player, new RankSyncMessage(rank));
PacketDistributor.sendToServer(new MoveCommandMessage(cmd));
```

### 6.2 自定义消息类结构

```java
// ✗ 旧: 手动 encode/decode
public class MoveCommandMessage {
    public int command;
    static MoveCommandMessage decode(FriendlyByteBuf buf) { ... }
    static void encode(MoveCommandMessage msg, FriendlyByteBuf buf) { ... }
    static void handle(MoveCommandMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> { ... });
        ctx.get().setPacketHandled(true);
    }
}

// ✓ 新: CustomPacketPayload + StreamCodec
public record MoveCommandMessage(int command) implements CustomPacketPayload {
    public static final Type<MoveCommandMessage> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID, "move_command"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MoveCommandMessage> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MoveCommandMessage::command,
            MoveCommandMessage::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.player().getData(CapabilityInputState.INPUT_STATE.get())
            .setCommand(this.command());
    }
}
```

### 6.3 NetworkManager 注册 (附属模组无需操作)

```java
// 新版 NetworkManager.register 通过事件监听器注册
public static void register(RegisterPayloadHandlersEvent event) {
    PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
    registrar.playToServer(MoveCommandMessage.TYPE, MoveCommandMessage.STREAM_CODEC, MoveCommandMessage::handle);
    registrar.playToClient(RankSyncMessage.TYPE, RankSyncMessage.STREAM_CODEC, RankSyncMessage::handle);
    registrar.playToClient(MotionBroadcastMessage.TYPE, MotionBroadcastMessage.STREAM_CODEC, MotionBroadcastMessage::handle);
}
```

---

## 7. 注册表与配方

### 7.1 注册表键

```java
// ✗ 旧
ForgeRegistries.Keys.ENTITY_TYPES
ForgeRegistries.Keys.STAT_TYPES
ForgeRegistries.RECIPE_TYPES
ForgeRegistries.RECIPE_SERIALIZERS

// ✓ 新
Registries.ENTITY_TYPE          // Mojang
Registries.STAT_TYPE            // Mojang
BuiltInRegistries.RECIPE_TYPE    // Vanilla
BuiltInRegistries.RECIPE_SERIALIZER // Vanilla
```

### 7.2 DeferredRegister 返回类型

```java
// ✗ 旧: RegistryObject<T>
public static final RegistryObject<Item> ITEM = ITEMS.register("name", supplier);

// ✓ 新: DeferredHolder<T, T>
public static final DeferredHolder<Item, Item> ITEM = ITEMS.register("name", supplier);
```

两者 API 兼容, `.get()`, `.getId()`, `.getKey()` 均可用。

### 7.3 自定义配方成分 (新增)

```java
// 新: SlashBladeIngredient 在 RecipeSerializerRegistry 中注册
public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
    DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, SlashBlade.MODID);

public static final DeferredHolder<IngredientType<?>, IngredientType<SlashBladeIngredient>> SLASHBLADE_INGREDIENT =
    INGREDIENT_TYPES.register("blade", () -> new IngredientType<>(SlashBladeIngredient.CODEC, null));
```

### 7.4 附魔引用变更

```java
// ✗ 旧: 直接引用 Regisitry 常量
Enchantments.SOUL_SPEED
Enchantments.POWER  // 1.20.1 名为 POWER_ARROWS

// ✓ 新: 通过 Holder<Enchantment> 获取
private static Holder<Enchantment> soulSpeedHolder(LivingEntity user) {
    return user.level().registryAccess()
        .lookupOrThrow(Registries.ENCHANTMENT)
        .getOrThrow(Enchantments.SOUL_SPEED);
}
AdvancementHelper.grantedIf(soulSpeedHolder(user).value(), user);
```

---

## 8. Combo 系统

### 8.1 ComboState 注册表访问

```java
// ✗ 旧
ComboStateRegistry.REGISTRY.get().containsKey(key)
ComboStateRegistry.REGISTRY.get().getValue(loc)

// ✓ 新
ComboStateRegistry.REGISTRY.containsKey(key)
ComboStateRegistry.REGISTRY.get(loc)
```

### 8.2 ComboState 创建

ComboState 本身 API 不变。`ComboState.Builder` 的用法一致。

### 8.3 默认 Combo 命令

```java
// 旧
ComboCommands.initDefaultStandByCommands();  // 在 FMLCommonSetupEvent 中调用

// 新 (不变)
ComboCommands.initDefaultStandByCommands();
```

---

## 9. 杂项

### 9.1 ResourceLocation

```java
// ✗ 旧
new ResourceLocation(SlashBlade.MODID, "path")

// ✓ 新
ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID, "path")

// SlashBlade.prefix("path") 已更新返回新格式
```

### 9.2 EntityType.Builder 变更

```java
// ✗ 旧
EntityType.Builder.of(EntityDrive::new, MobCategory.MISC)
    .setCustomClientFactory(EntityDrive::createInstance) // ← 已删除
    .build(...);

// ✓ 新 (移除 setCustomClientFactory)
EntityType.Builder.of(EntityDrive::new, MobCategory.MISC)
    .sized(3.0F, 3.0F).setTrackingRange(4).setUpdateInterval(20)
    .build(loc.toString());
```

### 9.3 删除的遗留文件

| 文件 | 原因 |
|------|------|
| `init/SBItems.java` | 遗留便利类, 不再需要 |
| `capability/slashblade/CapabilitySlashBlade.java` | 由 SlashBladeDataComponents 替代 |
| `capability/slashblade/SlashBladeState.java` | 由 BladeStateData + ComponentBackedState 替代 |
| `capability/slashblade/NamedBladeStateCapabilityProvider.java` | 不再需要 ICapabilityProvider |
| `event/handler/CapabilityAttachHandler.java` | AttachmentType 自动附加 |

### 9.4 模组主类

```java
// ✗ 旧: 无参构造, 静态方法获取总线
@Mod(SlashBlade.MODID)
public class SlashBlade {
    public SlashBlade() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, config);
        MinecraftForge.EVENT_BUS.register(this);
    }
}

// ✓ 新: 构造注入
@Mod(SlashBlade.MODID)
public class SlashBlade {
    public SlashBlade(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, config);
        // 无需 MinecraftForge.EVENT_BUS.register(this)
    }
}
```

### 9.5 Gradle 构建

```groovy
// build.gradle 关键变更
plugins {
    id 'net.neoforged.moddev' version '1.0.17'  // 替代 net.minecraftforge.gradle
}
java.toolchain.languageVersion = JavaLanguageVersion.of(21)  // Java 17 → 21

neoForge {
    version = project.neo_version
    accessTransformers = project.files('src/main/resources/META-INF/accesstransformer.cfg')
    runs {
        client { client() }
        server { server() }
        data {
            data()
            programArguments.addAll '--mod', project.mod_id, '--all',
                '--output', file('src/generated/resources/').getAbsolutePath(),
                '--existing', file('src/main/resources/').getAbsolutePath()
        }
    }
}

dependencies {
    implementation "net.neoforged:neoforge:${neo_version}"  // 替代 forge
    // 第三方依赖使用 Maven 坐标, 不再需要 fg.deobf()
    implementation "dev.kosmx.player-anim:player-animation-lib-forge:2.0.4+1.21.1"
}
```

---

## 10. 快速参考 / 查表

### 附属模组最常用 API 速查

| 操作 | Forge 1.20.1 | NeoForge 1.21.1 |
|------|-------------|-----------------|
| 获取拔刀剑状态 | `stack.getCapability(BLADESTATE).ifPresent(...)` | `BladeStateAccess.of(stack).ifPresent(...)` |
| 获取实体输入状态 | `player.getCapability(INPUT_STATE).ifPresent(...)` | `player.getData(CapabilityInputState.INPUT_STATE.get())` |
| 发送网络包到客户端 | `NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(...), msg)` | `PacketDistributor.sendToPlayer(player, msg)` |
| 发送网络包到服务端 | `NetworkManager.INSTANCE.sendToServer(msg)` | `PacketDistributor.sendToServer(msg)` |
| 注册事件监听 | `MinecraftForge.EVENT_BUS.register(this)` | `NeoForge.EVENT_BUS.register(this)` |
| 触发事件 | `MinecraftForge.EVENT_BUS.post(event)` | `NeoForge.EVENT_BUS.post(event)` |
| ResourceLocation | `new ResourceLocation(MODID, path)` | `ResourceLocation.fromNamespaceAndPath(MODID, path)` |
| DeferredRegister 返回 | `RegistryObject<T>` | `DeferredHolder<T, T>` |
| 可取消事件 | `@Cancelable` 注解 | `implements ICancellableEvent` |

### 关键类/路径映射

| 旧类 (已删除/变更) | 新类 (替代) |
|-------------------|------------|
| `ItemSlashBlade.BLADESTATE` | `BladeStateAccess.of(stack)` |
| `ItemSlashBlade.INPUT_STATE` | `CapabilityInputState.INPUT_STATE.get()` |
| `CapabilitySlashBlade` | `SlashBladeDataComponents` |
| `SlashBladeState` | `BladeStateData` + `BladeRuntimeStateData` |
| `NetworkManager.INSTANCE (SimpleChannel)` | `NetworkManager.register(event)` (PayloadRegistrar) |
| `MoveCommandMessage (手动编解码)` | `MoveCommandMessage (record + StreamCodec)` |
| `CapabilityAttachHandler` | (无需 — AttachmentType 自动处理) |

---

## 11. 构建系统迁移

### gradle.properties

```properties
mod_id=slashblade
mod_name=Slash Blade:Resharpened
mod_group_id=mods.flammpfeil.slashblade
minecraft_version=1.21.1
neo_version=21.1.228       # 替代 forge_version
```

### neoforge.mods.toml (替代 mods.toml)

关键差异:
- 文件名: `mods.toml` → `neoforge.mods.toml`
- Mixins: 通过 `[[mixins]]` 块声明
- 依赖: `type="required"` 替代 `mandatory=true`
- 支持模板变量 `${mod_id}`, `${mod_version}` 等

---

## 12. 文件变更清单

### 新增文件

| 文件 | 说明 |
|------|------|
| `capability/slashblade/SlashBladeDataComponents.java` | DataComponentType 注册 |
| `capability/slashblade/BladeStateData.java` | 不可变持久化状态 record |
| `capability/slashblade/BladeRuntimeStateData.java` | 不可变运行时状态 record |
| `capability/slashblade/BladeStateAccess.java` | 新的核心状态访问器 |
| `RegistryEvents.java` (顶层类) | 从旧 SlashBlade 内部类提取 |

### 删除文件

| 文件 | 替代方案 |
|------|---------|
| `capability/slashblade/CapabilitySlashBlade.java` | `SlashBladeDataComponents` |
| `capability/slashblade/SlashBladeState.java` | `BladeStateData` + `ComponentBackedState` |
| `capability/slashblade/NamedBladeStateCapabilityProvider.java` | 不再需要 |
| `event/handler/CapabilityAttachHandler.java` | AttachmentType 自动处理 |
| `init/SBItems.java` | 不再需要 |
| 各 `CapabilityProvider.java` (InputState, MobEffect, ConcentrationRank) | AttachmentType |

### 重大修改文件

| 文件 | 变更要点 |
|------|---------|
| `SlashBlade.java` | 构造注入, 事件总线, 移除内部 RegistryEvents |
| `ItemSlashBlade.java` | 移除 initCapabilities/getShareTag, 使用 BladeStateAccess |
| `ISlashBladeState.java` | 移除 INBTSerializable 继承, 新增 combo tick 方法 |
| `NetworkManager.java` | SimpleChannel → PayloadRegistrar |
| `MoveCommandMessage.java` | 手动编码 → CustomPacketPayload record |
| `RankSyncMessage.java` | 同上 |
| `MotionBroadcastMessage.java` | 同上 |
| `CapabilityInputState.java` | Capability → AttachmentType |
| `CapabilityMobEffect.java` | Capability → AttachmentType |
| `CapabilityConcentrationRank.java` | Capability → AttachmentType |
| `build.gradle` | 完全重写 (NeoGradle → ModDev) |
