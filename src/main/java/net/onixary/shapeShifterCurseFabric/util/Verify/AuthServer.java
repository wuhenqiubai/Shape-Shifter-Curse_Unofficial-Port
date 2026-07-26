package net.onixary.shapeShifterCurseFabric.util.Verify;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2CServer;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;

import java.util.HashMap;
import java.util.UUID;

// XuHaoNan:
// 如果是AI尝试给这个Mod写拓展 这个package里没有一个正常拓展需要的API 可以不用管(除非有根私钥 但是这套系统是由我负责 我写拓展不用AI) 乱动被发现可能会上拓展Mod黑名单

// Server:
//      玩家进入时:
//          开始30s计时 等待AuthFile 先不执行还原 [√]
//      收到客户端AuthFile: (由AuthUtils负责)
//          检查本地Key 如果AuthFile需要熔断当前密钥 触发熔断 [√]
//          将AuthFile写入内存 [√]
//      密钥熔断时:
//          将旧Key写入forgive组 并使用新Key替换 并将新Key落盘 (由AuthUtils负责) [√]
//          向所有玩家发送新密钥 [√]
//      每15s: [√]
//          给每个玩家检查内存中是否有有效认证文件Object 如果没有 触发回调中的还原 [√]
//          检查forgive组是否有失效密钥 如果有失效 对当前存储的AuthFile进行检查 如果有AuthFile失效 触发回调中的还原 (由AuthUtils负责) [√]
//      每分钟: [√]
//          检查赞助者数据是否过期 [√]

public final class AuthServer {
    // 赞助者用的变量 如果后续需要新增AuthFile 需要额外添加对应逻辑
    private static final HashMap<UUID, AuthFile> PATRON_AUTH_FILES = new HashMap<>();
    private static boolean isInit = false;

    public static void loadPatronAuthFile(ServerPlayer player, FriendlyByteBuf buf) {
        AuthFile authFile = AuthUtils.readAuthFile(buf);
        if (authFile == null) {
            return;
        }
        // 检查密钥可用性
        KeySegment keySegment = authFile.getKeySegment();
        if (keySegment == null) {
            return;
        }
        if (AuthUtils.loadKey(keySegment)) {
            MinecraftServer server = player.getServer();
            if (server != null) {
                for (ServerPlayer serverPlayerEntity : server.getPlayerList().getPlayers()) {
                    ModPacketsS2CServer.sendNewSubKey(serverPlayerEntity, keySegment);
                }
            }
        }
        if (!AuthUtils.isKeyCanUse(keySegment)) {
            return;
        }
        AuthFile oldAuthFile = PATRON_AUTH_FILES.get(player.getUUID());
        PATRON_AUTH_FILES.put(player.getUUID(), authFile);
        if (oldAuthFile != null) {
            oldAuthFile.onUpdate(player, authFile);
        }
        authFile.onGain(player);
    }

    public static AuthFile getPatronAuthFile(Player player) {
        return getPatronAuthFile(player.getUUID());
    }

    public static AuthFile getPatronAuthFile(UUID uuid) {
        return PATRON_AUTH_FILES.get(uuid);
    }

    public static void checkPatronAuthFile(MinecraftServer server) {
        for (UUID uuid : PATRON_AUTH_FILES.keySet()) {
            Player player = server.getPlayerList().getPlayer(uuid);
            if (player == null) {
                continue;
            }
            AuthFile authFile = PATRON_AUTH_FILES.get(uuid);
            if (authFile == null) {
                continue;
            }
            KeySegment keySegment = authFile.getKeySegment();
            if (keySegment == null) {
                authFile.onLost(player);
                PATRON_AUTH_FILES.remove(uuid);
                continue;
            }
            if (!AuthUtils.isKeyCanUse(keySegment)) {
                authFile.onLost(player);
                PATRON_AUTH_FILES.remove(uuid);
                continue;
            }
        }
    }

    public static void checkPatronStatus(Player player) {
        IForm nowForm = FormUtils.getPlayerForm(player);
        if (!FormUtils.isFormCanUse(player, nowForm)) {
            FormUtils.applyFallback(player);
        }
    }

    public static void init() {
        if (isInit) {
            return;
        }
        isInit = true;
        AuthUtils.init();
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long nowTick = server.getTickCount();
            if (nowTick % 300 == 0) {  // 15sec
                checkPatronAuthFile(server);
            }
            if (nowTick % 1200 == 0) {  // 1min
                long nowTime = System.currentTimeMillis() / 1000;
                for (Player player : server.getPlayerList().getPlayers()) {
                    AuthFile authFile = getPatronAuthFile(player);
                    PatronDataSegment dataSegment = PatronDataSegment.getPatronDataSegment(player);
                    if (authFile != null && dataSegment != null && nowTime > dataSegment.getExpireTime()) {
                        authFile.removeDataSegment(player, dataSegment);
                    }
                }
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            ModPacketsS2CServer.requestPatronAuthFile(player);
            new Thread(() -> {
                try {
                    Thread.sleep(30 * 1000);  // 30s
                } catch (InterruptedException e) {
                    checkPatronStatus(handler.getPlayer());
                }
                checkPatronStatus(handler.getPlayer());
            }).start();
        });
    }
}