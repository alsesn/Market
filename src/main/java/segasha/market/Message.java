package segasha.market;

import com.google.common.collect.Lists;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Message {
    noPermission, usage, noItem, invalidPrice, minPrice, sellOrderCreated, sellOrderCancelled;

    private List<String> msg;

    @SuppressWarnings("uncheched")
    public static void load(FileConfiguration c) {
        for(Message message : Message.values()){
            Object obj = c.get("messages," + message.name().replace("_", "."));
            if (obj instanceof List) {
                message.msg = (List<String>) obj;
            }
            else {
                message.msg = Lists.newArrayList((String) obj);
            }
        }
    }

    public Sender replace(String from, String to){
        Sender sender = new Sender();
        return sender.replace(from, to);
    }

    public void send (CommandSender player) {
        new Sender().send(player);
    }

    public class Sender {
        private final Map<String, String> placeholders = new HashMap<>();

        public void send(CommandSender player) {
            for (String message : Message.this.msg) {
                sendMessage(player, replacePlaceholders(message));
            }
        }

        public Sender replace(String from, String to) {
            placeholders.put(from, to);
            return this;
        }
        private void sendMessage(CommandSender player, String message) {
            if (message.startsWith("json:")) {
                player.sendMessage(String.valueOf(new TextComponent(ComponentSerializer.parse(message.substring(5)))));
            } else {
                player.sendMessage(message);
            }
        }

        private String replacePlaceholders(String message) {
            message = ChatColor.translateAlternateColorCodes('&', message);
            if (!message.contains("{")) return message;
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace(entry.getKey(), entry.getValue());
            }

            return message;
        }
    }

}
