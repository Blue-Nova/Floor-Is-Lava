package com.bluenova.floorislava.config;

import com.bluenova.floorislava.FloorIsLava;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MainConfig {

    private final static MainConfig instance = new MainConfig();

    private File file;
    private YamlConfiguration config;

    private int plotMargin;
    private int plotSize;
    private int plotAmount;

    private MainConfig() {
    }
    public void load() {
        file = new File(FloorIsLava.getInstance().getDataFolder(), "config.yml");

        if (!file.exists())
            FloorIsLava.getInstance().saveResource("config.yml", false);

        config = new YamlConfiguration();
        config.options().parseComments(true);

        try {
            config.load(file);

        } catch (Exception e) {
            e.printStackTrace();
        }

        plotMargin = config.getInt("Plot.Margin");
        plotSize = config.getInt("Plot.Size");
        plotAmount = config.getInt("Plot.Amount");
    }

    public void save(){
        try {
            config.save(file);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void set(String path, Object value){
        config.set(path,value);
        save();
    }

    public static MainConfig getInstance(){
        return instance;
    }

    public int getPlotMargin(){
        return plotMargin;
    }
    public int getPlotSize(){
        return plotSize;
    }
    public int getPlotAmount(){
        return plotAmount;
    }
}
