package mods.flammpfeil.slashblade.event;

import mods.flammpfeil.slashblade.network.MotionBroadcastMessage;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

public class BladeMotionEventBroadcaster {

    private static final class SingletonHolder {
        private static final BladeMotionEventBroadcaster instance = new BladeMotionEventBroadcaster();
    }

    public static BladeMotionEventBroadcaster getInstance() {
        return BladeMotionEventBroadcaster.SingletonHolder.instance;
    }

    private BladeMotionEventBroadcaster() {
    }

    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onBladeMotion(BladeMotionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) {
            return;
        }

        PacketDistributor.sendToPlayersNear(sp.serverLevel(), null, sp.getX(), sp.getY(), sp.getZ(), 20,
                new MotionBroadcastMessage(sp.getUUID(), event.getCombo(), event.getActionTime()));
    }
}
