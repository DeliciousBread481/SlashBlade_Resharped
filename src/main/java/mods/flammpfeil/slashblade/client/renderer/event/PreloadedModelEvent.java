package mods.flammpfeil.slashblade.client.renderer.event;

import mods.flammpfeil.slashblade.SlashBlade;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

// 预加载模型的事件
@EventBusSubscriber(modid = SlashBlade.MODID, value = Dist.CLIENT)
public class PreloadedModelEvent {

    @SubscribeEvent
    public static void registerResourceLoaders(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ModelResourceLoader());
    }
}
