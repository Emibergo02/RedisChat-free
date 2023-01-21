package dev.unnm3d.redischat.commands;

import dev.unnm3d.redischat.Permission;
import dev.unnm3d.redischat.RedisChat;
import dev.unnm3d.redischat.redis.ChatPacket;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class BroadcastCommand implements CommandExecutor {
    private final RedisChat plugin;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(Permission.REDIS_CHAT_BROADCAST.getPermission())) return false;
        new BukkitRunnable() {
            @Override
            public void run() {
                String message = MiniMessage.miniMessage().serialize(plugin.getComponentProvider().parse(null, RedisChat.getInstance().config.broadcast_format.replace("%message%", String.join(" ", args))));
                plugin.getRedisDataManager().sendObjectPacket(new ChatPacket(null, message));
            }
        }.runTaskAsynchronously(plugin);

        return false;
    }
}
