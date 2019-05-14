package me.nethersoul.skinoverride;

import javafx.scene.control.Skin;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mod(modid = SkinOverride.MODID, name = SkinOverride.NAME, version = SkinOverride.VERSION)
public class SkinOverride {
    public static final String MODID = "skinoverride";
    public static final String NAME = "Skin Override";
    public static final String VERSION = "1.2";

    private Logger logger;

    private static SkinEventHandler handler = new SkinEventHandler();

    @Config.LangKey("skinoverride.config")
    @Config(modid = MODID)
    public static class SkinConfig {
        @Config.Name("skin_data")
        public static String skinArray[] = {};
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(handler);
        logger = event.getModLog();

        logger.log(Level.INFO, "Forgia Utils: pre load...");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        try {
            if (FMLCommonHandler.instance().getSide().isServer()) return;

            logger.log(Level.INFO, "Adding skin command...");

            if (event.getSide() == Side.CLIENT && ClientCommandHandler.instance != null) {
                //Adding the skin command to the game
                ClientCommandHandler.instance.getCommands().put("skin", new CommandSkinChange());
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, "Failed to add skin command.");
            ex.printStackTrace();
        }

        ConfigManager.sync(MODID, Config.Type.INSTANCE);
    }

    /**
     * Transforms the array to a List<String> and returns it
     *
     * @return skinArray
     */
    public static List<String> getSkins() {
        return Arrays.asList(SkinConfig.skinArray);
    }

    /**
     * Adds a skin + ulr to the database
     *
     * @param username
     * @param url
     */
    public static void addSkin(String username, String url) {
        List<String> skins = new ArrayList<>(getSkins());

        int i;
        boolean found = false;
        for (i = 0; i < skins.size(); i++) {
            String name = skins.get(i).split(":")[0];
            if (name.equals(username)) {
                found = true;
                break;
            }
        }

        String toAdd = username + ":" + url;

        if (found) {
            skins.set(i, toAdd);
        } else {
            skins.add(toAdd);
        }

        SkinConfig.skinArray = skins.toArray(SkinConfig.skinArray);
    }
}
