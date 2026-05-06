# SlashBlade Resharped 优化计划

> 基于 `SlashBlade_Resharped`（Minecraft 1.21.1 NeoForge）完整代码审查生成。
> **不包含 "无条件启用飞行覆盖服务器配置" 项目 —— 该功能为刚需，不可修改。**

---

## 行为不变性承诺

所有优化方案均设计为**不改变当前预期行为**。若某项必须微调行为，会在该项内明确标注理由。

---

## 安全关键修复（必须执行）

### BlandStandEventHandler: ResourceLocation.parse() → tryParse()

| 文件 | 行号 | 问题 |
|------|------|------|
| `event/bladestand/BlandStandEventHandler.java` | 87 | `ResourceLocation.parse(tag.getString("SpecialEffectType"))` |
| `event/bladestand/BlandStandEventHandler.java` | 135 | `ResourceLocation.parse(tag.getString("SpecialAttackType"))` |

**问题**: 玩家构造含非法字符的 CustomData 标签，右键点击刀挂台时触发 uncaught `ResourceLocationException` → 服务端崩溃。

**方案**: `ResourceLocation.tryParse()` + null 提前 return。

### SlashBladeItems: tooltip 中的 ResourceLocation.parse()

| 文件 | 行号 | 问题 |
|------|------|------|
| `registry/SlashBladeItems.java` | 65, 87 | tooltip 计算路径中的 `ResourceLocation.parse()` |

**方案**: 同上 `ResourceLocation.tryParse()` + null 检查。

---

## 渲染性能优化（OBJ 热路径）

### Face.putVertex() — 消除每帧每顶点 JOML 对象分配

**文件**: `Face.java:83-126`  
**现状**: 每帧每顶点 `new Vector4f(4)` 和 `new Vector3f(3)`。典型模型 1500 顶点 × 3 pass × N 实体 = 数千至数万分配/帧。

**方案（行为不变）**:

1. **消除 Vector4f**（行 89）: 内联 `alphaOverride` 逻辑。只有两种实际行为：
   - `alphaNoOverride` = 直接返回参数 alpha
   - `alphaOverrideYZZ` = 顶点 y 为 0 时 alpha=0

   将 lambda 调用替换为直接判断：
   ```java
   int alpha = FastColor.ARGB32.alpha(color);
   if (alphaOverride == alphaOverrideYZZ && vertices[i].y == 0) alpha = 0;
   ```

2. **复用 Vector3f**（行 118-121）: 添加 ThreadLocal 复用实例：
   ```java
   private static final ThreadLocal<Vector3f> NORMAL_TMP = ThreadLocal.withInitial(Vector3f::new);
   ```
   将 `new Vector3f(normal.x, normal.y, normal.z)` 改为 `NORMAL_TMP.get().set(normal.x, normal.y, normal.z)`。

### Face.addFaceForRender() — 预计算纹理 offset 符号

**文件**: `Face.java:65-75`  
**现状**: 每帧重算 `averageU`/`averageV`，仅用于判断 `offsetU`/`offsetV` 正负方向。

**方案（行为不变）**:

加载时在 Face 中预计算 `boolean[] texUSign, texVSign`（各边长=顶点数），渲染时直接索引。因为 `uvOperator` 为单调仿射变换，比较方向的符号不变。

### WavefrontObject — 静态 Matcher 线程安全

**文件**: `WavefrontObject.java:38-39, 365-503`  
**现状**: 8 个静态 Matcher + `reset()`。`BladeModelManager` 异步加载时多线程竞争。

**方案（行为不变）**: 每个 `isValid*Line()` 内改为局部 `pattern.matcher(line).matches()`，移除所有静态 Matcher 字段。

---

## 服务端/客户端优化

### EntitySlashEffect.alreadyHits 列表泄漏

**文件**: `entity/EntitySlashEffect.java:337-348`  
**现状**: `doCycleHit()` 为 false 时列表永不清理。

**方案（行为不变）**: 在 `onRemovedFromWorld()` 中添加 `alreadyHits.clear()`。实体移除后无代码依赖此列表。

### TargetSelector — 冗余 Stream 包装

**文件**: `util/TargetSelector.java:99-114, 122-123, 207-219`  
**现状**: `Stream.of(x).flatMap(s -> s)` 完全冗余。

**方案（行为不变）**: 直接使用内层 stream，`.collect(Collectors.toList())` → `.toList()`。

### LockonCircleRender — 每渲染实体射线追踪移入 tick 缓存

**文件**: `client/renderer/LockonCircleRender.java:146-223`  
**现状**: `RenderLivingEvent.Post` 对每个渲染实体执行全量射线追踪 + 大范围 AABB 查询。

**方案（轻微行为变化 — 更新频率从帧率降至 20Hz tick）**:

`resolveLockOnTargetClient()` 在 `onEntityUpdate()`（ClientTickEvent）中执行一次，结果 UUID 缓存为字段。`onRenderLiving()` 中仅比对 `entity.getUUID().equals(cachedTargetUUID)`。

**行为变化理由**: 锁敌目标更新延迟从 0 变为 ≤50ms。服务端逻辑本身在 20Hz 下运行，肉眼不可察觉差异。相比当前每帧数十次射线追踪的帧率暴跌，利远大于弊。

---

## 代码质量清理

### 消除重复 updateEnchantment()

**文件**: `recipe/SlashBladeShapedRecipe.java:125-153`、`recipe/SlashBladeSmithingRecipe.java:176-203`  
**方案**: 提取到 `util/EnchantmentsHelper.java`（该文件已存在）。

### 移除大段注释代码

| 文件 | 行号 |
|------|------|
| `client/renderer/SlashBladeTEISR.java` | 401-467 |
| `util/TargetSelector.java` | 107-113, 211-218 |
| `entity/EntityAbstractSummonedSword.java` | 107, 261, 317-324, 345, 434, 532, 557, 574-576, 665-667, 773-774 |
| `item/ItemSlashBlade.java` | 420, 568-573 |

### 弃用 API 替换

全程替换 `getCommandSenderWorld()` → `level()`：

| 文件 | 行号 |
|------|------|
| `util/AttackHelper.java` | 114 |
| `entity/EntityDrive.java` | 303 |
| `entity/EntitySlashEffect.java` | 309 |
| `ability/KillCounter.java` | 74 |
| `ability/Untouchable.java` | 47 |

### 小型修复清单

| 项目 | 文件 | 改动 |
|------|------|------|
| `Vec3(0,0,0)` 常量 | `EntityBlisteringSwords.java:164`, `EntityDropEntry.java:20,28,32` | → `Vec3.ZERO` |
| 冗余异常 lambda | 14 处 | `orElseThrow(NullPointerException::new)` → `orElseThrow()` |
| 拼写修正 | `ComboCommands.java:13` | `DEAFULT_STANDBY` → `DEFAULT_STANDBY` |
| 位运算语义 | `WavefrontObject.java:102` | `\|` → `\|\|` |
| 通配符导入 | 8 个文件 | `import java.util.*` / `import java.awt.*` → 显式导入 |

---

## 交接执行提示词（一步到位）

```
在项目 D:\minecraft_121_modding\SlashBlade_Resharped 中执行以下所有优化任务，一次性完成：

【安全修复】
1. BlandStandEventHandler.java 第 87、135 行：ResourceLocation.parse(tag.getString(...)) 改为 ResourceLocation.tryParse(tag.getString(...))，对 null 提前 return。
2. SlashBladeItems.java 第 65、87 行：同上替换。

【渲染性能 — Face.java】
3. putVertex() 消除 new Vector4f 分配：将第 89 行的 alphaOverride.apply(new Vector4f(vertices[i].x, vertices[i].y, vertices[i].z, 1.0F), FastColor.ARGB32.alpha(color)) 替换为内联逻辑：
   - 计算 int alpha = FastColor.ARGB32.alpha(color);
   - 若 static 字段 Face.alphaOverride 引用等于 Face.alphaOverrideYZZ 且 vertices[i].y == 0，则 alpha = 0;
   - 将计算结果直接传给 wr.setColor()。
4. putVertex() 消除 new Vector3f 分配：在 Face 类添加 private static final ThreadLocal<Vector3f> NORMAL_TMP = ThreadLocal.withInitial(Vector3f::new); 将第 114-121 行的 new Vector3f(...) 替换为 NORMAL_TMP.get().set(normal.x, normal.y, normal.z)。
5. addFaceForRender() 预计算纹理 offset 符号：在 Face 类添加 boolean[] texUSign 和 boolean[] texVSign 字段，在 WavefrontObject.parseFace() 各分支末尾（faceNormal 赋值后）调用一个初始化方法计算这两个数组。计算使用原始 textureCoordinates[i].u/v（不加 uvOperator）比较平均值。putVertex 中将第 99-104 行的 textureU > averageU / textureV > averageV 改为 texUSign[i] / texVSign[i]。

【渲染性能 — WavefrontObject.java】
6. 移除所有 8 个 static Matcher 字段声明（第 38-40 行），每个 isValid*Line() 方法改为 return pattern.matcher(line).matches(); 删除 if(matcher!=null) reset() 逻辑。

【服务端修复】
7. EntitySlashEffect.java：在 onRemovedFromWorld()/remove() 末尾添加 alreadyHits.clear()。
8. TargetSelector.java：移除所有 Stream.of(...stream()).flatMap(s->s) 冗余包装，.collect(Collectors.toList()) 改为 .toList()。
9. LockonCircleRender.java：在类中添加 @Nullable private UUID cachedLockOnTarget 字段。onEntityUpdate()（ClientTickEvent.Post handler）中调用 resolveLockOnTargetClient() 后将结果的 UUID 缓存到字段。onRenderLiving() 中删除 resolveLockOnTargetClient() 调用，改为从字段直接比对 entity.getUUID()。

【代码质量】
10. 将 SlashBladeShapedRecipe.java:125-153 和 SlashBladeSmithingRecipe.java:176-203 的两个 updateEnchantment() 提取为 util/EnchantmentsHelper.java 的 static 方法，两个原调用处改为调用该公共方法。
11. 移除所有已列出行号的注释代码块。
12. 全局搜索 getCommandSenderWorld() 替换为 level()（5 处）。
13. 全局搜索 new Vec3(0, 0, 0) 或 new Vec3(0,0,0) 替换为 Vec3.ZERO（4 处）。
14. ComboCommands.java:13: DEAFULT_STANDBY → DEFAULT_STANDBY。
15. WavefrontObject.java:102: | → ||
16. 全局搜索 orElseThrow(NullPointerException::new) 替换为 orElseThrow()（14 处）。

完成后运行 ./gradlew compileJava 验证编译通过。
对于渲染相关改动，额外运行一次 ./gradlew runClient 确认拔刀模型渲染正常。
```

---

## 验证

```
./gradlew compileJava          # 所有 Phase 必须通过
./gradlew runClient             # 渲染优化后验证
./gradlew runData               # 确认 datagen 不受影响
```
