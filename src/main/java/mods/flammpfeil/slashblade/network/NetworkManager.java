package mods.flammpfeil.slashblade.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkManager {

    private static final String PROTOCOL_VERSION = "1";

    private NetworkManager() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(MoveCommandMessage.TYPE, MoveCommandMessage.STREAM_CODEC, MoveCommandMessage::handle);
        registrar.playToClient(RankSyncMessage.TYPE, RankSyncMessage.STREAM_CODEC, RankSyncMessage::handle);
        registrar.playToClient(MotionBroadcastMessage.TYPE, MotionBroadcastMessage.STREAM_CODEC,
                MotionBroadcastMessage::handle);
    }

}
