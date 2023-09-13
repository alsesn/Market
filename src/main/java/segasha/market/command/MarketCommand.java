package segasha.market.command;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import segasha.market.Market;
import segasha.market.Message;
import segasha.market.utils.CommandHandler;
import segasha.market.utils.CommandInfo;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@CommandInfo(name = "market", requiresPlayer = true)
public class MarketCommand extends CommandHandler {
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0){
            ConfigurationSection section = Market.getData().getConfig().getConfigurationSection("sellOrders" + sender.getName());
            sender.sendMessage(Market.getInstance().getConfig().getString("messages.usage"));
            if (section == null) {
                Message.usage.send(sender);
                return true;
            }

            for (String key : section.getKeys(false)) {
                ItemStack item = Market.getData().getConfig().getItemStack("sellOrders" + sender.getName() + "." + key + ".item");
                if (item == null) return true;
                String name = getItemName(item);
                double price = Market.getData().getConfig().getDouble("sellOrders." + sender.getName() + "." + key + "." + ".price");

            }
        }

        if (args[0].equalsIgnoreCase("cancel")) {
            if (!(sender instanceof  Player)) return true;

            Player player = (Player) sender;
            if (args.length < 2) {
                sender.sendMessage("Cancel order: /market cancel <id>");
                return true;
            }

            String id = args[1];

            ItemStack item = Market.getData().getConfig().getItemStack("sellOrders." + sender.getName() + "." + id + "." + ".item");

            if (item == null) {
                sender.sendMessage("No order with id" + id);
                return true;
            }

            String itemName = getItemName(item);
            double price = Market.getData().getConfig().getDouble("sellOrders." + sender.getName() + "." + id + "." + ".price");


            Market.getData().getConfig().set("sellOrders." + sender.getName() + "." + id, null);
            Market.getData().save();


            HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
            if (!overflow.isEmpty()) {
                for (ItemStack i : overflow.values()){
                    player.getLocation().getWorld().dropItem(player.getLocation(), i);
                }
            }

            Message.sellOrderCancelled.replace("{item}", itemName).replace({"{price}", String.valueOf(price)).send(sender);

            return true;
        }

        if (args[0].equalsIgnoreCase("sell")) {
            if (args.length < 2) {
                sender.sendMessage(Market.getInstance().getConfig().getString("messages.usage"));
                return true;
            }

            if (!(sender instanceof Player)) return true;

            Player player = (Player) sender;
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR) {
                sender.sendMessage(ChatColor.RED + Market.getInstance().getConfig().getString("messages.noItem"));
                return true;
            }

            double price;

            try {
                price = Double.parseDouble(args[1]);
                if (price <= 0) throw new IllegalAccessException();
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + Market.getInstance().getConfig().getString("messages.invalidPrice"));
                return true;
            }

            double minPrice = Market.getInstance().getConfig().getDouble("minPrice");

            if (price < minPrice) {
                player.sendMessage(ChatColor.RED + Market.getInstance().getConfig().getString("messages.minPrice"));
                return true;
            }

            String itemName = getItemName(item);
            String uuid = UUID.randomUUID().toString();

            Market.getData().getConfig().set("sellOrders." + player.getName() + "." + uuid + ".item", item);
            Market.getData().getConfig().set("sellOrders." + player.getName() + "." + uuid + ".price", price);
            Market.getData().getConfig().set("sellOrders." + player.getName() + "." + uuid + ".time", System.currentTimeMillis());
            Market.getData().save();

            item.setAmount(item.getAmount() - 1 );
            player.getInventory().setItemInMainHand(item);

            String orderCreated = Market.getInstance().getConfig().getString("messages.sellOrderCreated", "Created sell order: {item}, ${price}");
            orderCreated = orderCreated.replace("{item}", itemName);
            orderCreated = orderCreated.replace("{price}", String.valueOf(price));
            player.sendMessage(orderCreated);
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("market.reload")){
                sender.sendMessage(ChatColor.RED + "You don't have permission");
                return true;
            }

            Market.getInstance().reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Market reloaded");

            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown command " + args[0]);

        return true;
    }

    private String getItemName(ItemStack item){
        return item.getType().name().toLowerCase();
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args){
        if (args.length == 1 ) return Lists.newArrayList("reload");
        return Lists.newArrayList();
    }

    @Override
    protected boolean help(CommandSender sender, String label) {
        return false;
    }
}
