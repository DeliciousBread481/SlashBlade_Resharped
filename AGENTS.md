# AGENTS.md

本仓库当前主线为 `Minecraft 1.21.1 NeoForge`。

## 目标

- 默认以维护、修复和功能迭代为主
- 优先最小改动，优先修正行为回归、运行时问题和构建失败
- 不保留迁移阶段文档或 `old_src`

## 规则

- 不要凭记忆判断 NeoForge、Minecraft 原版或第三方模组 API
- 只要 API、事件、注册、数据组件、网络、资源格式或第三方兼容点存在不确定性，就先查证再改
- 不要为了兼容旧迁移阶段结构而保留额外中间层，除非已有明确运行时需求

## 验证

- 小改动优先 `./gradlew compileJava`
- 涉及 datagen 时运行 `./gradlew runData`
- 涉及客户端路径时运行 `./gradlew runClient`
