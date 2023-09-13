package segasha.market.utils;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


import javax.annotation.Nullable;

import java.util.List;
import java.util.Objects;

public abstract class CommandHandler implements CommandExecutor, TabExecutor {

    @Getter
    private final CommandInfo commandInfo;

    public CommandHandler(){
        commandInfo = getClass().getDeclaredAnnotation(CommandInfo.class);
        Objects.requireNonNull(commandInfo, "Annotation is null, fatal error!");
    }
    public List<String> complete(CommandSender sender, String[] args){
        return null;
    }
    @Override
    public boolean onCommand (@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args){
        if (!commandInfo.permission().isEmpty()) {
            if (!sender.hasPermission(commandInfo.permission())){
                sender.sendMessage("no-perms");
                return false;
            }
        }

        if (commandInfo.requiresPlayer()) {
            if (!(sender instanceof Player)){
                sender.sendMessage("This command for only player");
                return false;
            }
        }
        execute(sender, command, label, args);
        return true;
    }

    public void register() {
        Objects.requireNonNull(Bukkit.getPluginCommand(commandInfo.name())).setExecutor(this);
    }
    protected abstract boolean execute (CommandSender sender, Command command, String label, String[] args);
    protected abstract boolean help (CommandSender sender, String label);

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alies, @NotNull String[] args) {
        return filter(complete(sender, args), args);
    }
    private List<String> filter(List<String> list, String[] args) {
        return list;
    }
}