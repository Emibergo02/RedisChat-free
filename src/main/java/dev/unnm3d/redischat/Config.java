package dev.unnm3d.redischat;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Configuration
public final class Config {

    private static final BukkitAudiences audiences = BukkitAudiences.create(RedisChat.getInstance());

    @Comment({"Redis uri", "Example: redis://user:password@localhost:6379"})
    public Redis redis = new Redis("redis://user:password@localhost:6379");
    @Comment({"The format of the chat", "Permission format is overridden on descending order", "(if a player has default and vip, if default is the first element, vip will be ignored)"})
    public List<ChatFormat> formats = List.of(new ChatFormat("redischat.default",
            "<click:suggest_command:/msg %player_name%><hover:show_text:'" +
                    "<reset>Information | <white>%player_displayname%<br>" +
                    "<gold><bold>➧</bold> Money<reset>: <white>%vault_eco_balance% <gold>✵<br>" +
                    "<br><reset><underlined>Click to send a message" +
                    "'><white>%player_displayname% </click> <dark_gray>» <reset>%message%",
            "<dark_aqua>MSG <white>(<reset>You <white>to <green>%receiver%<white>)<reset>: <white>%message%",
            "<dark_aqua>MSG <white>(<green>%sender% <white>to <reset>You<white>)<reset>: <white>%message%"
    ));
    public List<String> regex_blacklist = List.of("discord.gg/.*");
    public String broadcast_format = "<red>Announce <dark_gray>» <white>%message%";
    public String clear_chat_message = "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>";
    public String player_not_online = "<red>The player %player% is not online</red>";
    public String cannot_message_yourself = "<red>You cannot message yourself</red>";
    public String missing_arguments = "<red>Missing arguments</red>";
    public String no_reply_found = "<red>You do not have any message to reply</red>";
    public String reply_not_online = "<red>%player% is not online</red>";
    public String rate_limited = "<red>You've been rate limited</red>";
    public String ignoring_list = "<aqua>Player ignored</aqua><br><green>%list%</green>";
    public String ignoring_player = "<green>Toggled ignoring of %player%</green>";
    public String spychat_format = "<red>%sender% said to %receiver% : %message%</red>";
    public int rate_limit = 3;
    public int rate_limit_time_seconds = 5;
    public boolean debug = false;

    public record Redis(
            String redisUri) {
    }

    public record ChatFormat(
            String permission,
            String format,
            String private_format,
            String receive_private_format) {
    }

    public @NotNull List<ChatFormat> getChatFormats(CommandSender p) {
        List<Config.ChatFormat> chatFormatList = formats.stream().filter(format -> p.hasPermission(format.permission())).toList();
        if (chatFormatList.isEmpty()) {
            Bukkit.getLogger().info("No format found for " + p.getName());
            return List.of();
        }
        return chatFormatList;
    }

    public void sendMessage(CommandSender p, String message) {
        audiences.sender(p).sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    public void sendMessage(CommandSender p, Component component) {
        audiences.sender(p).sendMessage(component);
    }
}