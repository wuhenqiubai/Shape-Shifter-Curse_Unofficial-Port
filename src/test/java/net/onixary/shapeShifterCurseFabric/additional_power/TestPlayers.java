package net.onixary.shapeShifterCurseFabric.additional_power;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.fabricmc.fabric.impl.networking.ChannelInfoHolder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.level.GameType;

import java.util.UUID;

public final class TestPlayers {
    private TestPlayers() {
    }

    public static ServerPlayer spawn(GameTestHelper context) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "test-mock-player");
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
        ServerLevel level = context.getLevel();
        MockServerPlayer player = new MockServerPlayer(level, cookie);
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        ((ChannelInfoHolder) connection)
                .fabric_getPendingChannelsNames(ConnectionProtocol.PLAY)
                .add(Identifier.fromNamespaceAndPath("cardinal-components", "entity_sync"));
        new EmbeddedChannel(connection);
        level.getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        return player;
    }

    public static final class MockServerPlayer extends ServerPlayer {
        private GameType gameType = GameType.CREATIVE;

        public MockServerPlayer(ServerLevel level, CommonListenerCookie cookie) {
            super(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
        }

        @Override
        public GameType gameMode() {
            return this.gameType;
        }

        @Override
        public boolean setGameMode(GameType type) {
            this.gameType = type;
            return super.setGameMode(type);
        }
    }
}
