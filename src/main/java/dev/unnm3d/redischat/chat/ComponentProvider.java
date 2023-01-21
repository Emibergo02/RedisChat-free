package dev.unnm3d.redischat.chat;

import dev.unnm3d.redischat.Config;
import dev.unnm3d.redischat.RedisChat;
import dev.unnm3d.redischat.commands.PlayerListManager;
import lombok.AllArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
public class ComponentProvider {
    private final MiniMessage miniMessage;
    private final RedisChat plugin;
    private final BukkitAudiences bukkitAudiences;

    public ComponentProvider(RedisChat plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.bukkitAudiences = BukkitAudiences.create(plugin);
    }

    public Component parse(String text, TagResolver... tagResolvers) {
        return parse(null, text, tagResolvers);
    }

    public Component parse(String text) {
        return parse(text, StandardTags.defaults());
    }

    public Component parse(CommandSender player, String text, TagResolver... tagResolvers) {
        return miniMessage.deserialize(
                parsePlaceholders(player,
                        parseMentions(
                                text,
                                plugin.config.formats.get(0))
                ), tagResolvers);
    }

    public Component parse(CommandSender player, String text, boolean parsePlaceholders, TagResolver... tagResolvers) {
        if (!parsePlaceholders)
            return miniMessage.deserialize(
                    parseMentions(
                            text,
                            plugin.config.formats.get(0)
                    ), tagResolvers);
        else
            return parse(player, text, tagResolvers);
    }

    public Component parse(CommandSender player, String text) {
        return parse(player, text, StandardTags.defaults());
    }

    public String parsePlaceholders(CommandSender cmdSender, String text) {
        String message =
                cmdSender instanceof OfflinePlayer
                        ? PlaceholderAPI.setPlaceholders((OfflinePlayer) cmdSender, text)
                        : PlaceholderAPI.setPlaceholders(null, text);
        return miniMessage.serialize(LegacyComponentSerializer.legacySection().deserialize(message)).replace("\\", "");
    }

    public String purgeTags(String text) {
        return miniMessage.stripTags(text, TagResolver.standard());
    }

    public TagResolver getCustomTagResolver(CommandSender player, Config.ChatFormat chatFormat) {

        TagResolver.Builder builder = TagResolver.builder();

        return builder.build();
    }

    public String parseMentions(String text, Config.ChatFormat format) {
        String toParse = text;

        for (String playerName : PlayerListManager.getPlayerList()) {
            Pattern p = Pattern.compile("(^" + playerName + "|" + playerName + "$|\\s" + playerName + "\\s)"); //
            Matcher m = p.matcher(text);
            if (m.find()) {
                String replacing = m.group();
                replacing = replacing.replace(playerName, "<aqua>@" + playerName+"</aqua>");
                toParse = toParse.replace(m.group(), replacing);
            }
        }
        return toParse;
    }

    public String sanitize(String message) {
        for (String regex : plugin.config.regex_blacklist) {
            message = message.replaceAll(regex, "***");
        }
        return message;
    }

    public void sendPublicChat(String serializedText) {
        bukkitAudiences.all().sendMessage(MiniMessage.miniMessage().deserialize(serializedText));
    }

    public void sendPrivateChat(String senderName, String receiverName, String text) {
        Player p = Bukkit.getPlayer(receiverName);
        if (p != null)
            if (p.isOnline()) {
                List<Config.ChatFormat> chatFormatList = plugin.config.getChatFormats(p);
                if (chatFormatList.isEmpty()) return;
                Component formatted = parse(null, chatFormatList.get(0).receive_private_format().replace("%receiver%", receiverName).replace("%sender%", senderName));
                Component toBeReplaced = parse(null, text);
                //Put message into format
                formatted = formatted.replaceText(
                        builder -> builder.match("%message%").replacement(toBeReplaced)
                );
                plugin.config.sendMessage(p, formatted);
            }

    }
}

