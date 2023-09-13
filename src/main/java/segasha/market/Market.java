package segasha.market;

import org.bukkit.plugin.java.JavaPlugin;
import segasha.market.command.MarketCommand;

public final class Market extends JavaPlugin {


    private static Market instance;
    private Storage data;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        data = new Storage("items.yml");

        registerCommand();
    }

    public static Market getInstance() {
        return instance;
    }

    public static Storage getData() {
        return instance.data;
    }

    private void registerCommand(){
        new MarketCommand().register();
    }
}
