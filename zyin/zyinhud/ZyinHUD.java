/*
 * IDEAS
 * =====
 * //Ctrl + click item to move it into the crafting area (cant figure out how to get right click hook)
 * dont render other player's armor (or just helmet?) in multiplayer
 */

package zyin.zyinhud;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;

import org.lwjgl.input.Keyboard;

import zyin.zyinhud.keyhandler.DistanceMeasurerKeyHandler;
import zyin.zyinhud.keyhandler.EatingAidKeyHandler;
import zyin.zyinhud.keyhandler.EnderPearlAidKeyHandler;
import zyin.zyinhud.keyhandler.HorseInfoKeyHandler;
import zyin.zyinhud.keyhandler.PlayerLocatorKeyHandler;
import zyin.zyinhud.keyhandler.SafeOverlayKeyHandler;
import zyin.zyinhud.keyhandler.WeaponSwapperKeyHandler;
import zyin.zyinhud.tickhandler.HUDTickHandler;
import zyin.zyinhud.tickhandler.RenderTickHandler;
import zyin.zyinhud.util.Localization;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "ZyinHUD", name = "Zyin's HUD", version = "0.8.0")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class ZyinHUD
{
    /**
     * Comma seperated values of languages to load by setting the default value in the config file.
     * Recreate the config file or the variable "SupportedLanguages" to see these values updated.
     */
    private static final String DefaultSupportedLanguages = "en_US"; //"en_US, zh_CN"
    
    
    public static final String CATEGORY_LANGUAGE = "language";
    public static final String CATEGORY_INFOLINE = "info line";
    public static final String CATEGORY_COORDINATES = "coordinates";
    public static final String CATEGORY_COMPASS = "compass";
    public static final String CATEGORY_DURABILITYINFO = "durability info";
    public static final String CATEGORY_SAFEOVERLAY = "safe overlay";
    public static final String CATEGORY_POTIONTIMERS = "potion timers";
    public static final String CATEGORY_PLAYERLOCATOR = "player locator";
    public static final String CATEGORY_EATINGAID = "eating aid";
    public static final String CATEGORY_WEAPONSWAP = "weapon swap";
    public static final String CATEGORY_FPS = "fps";
    public static final String CATEGORY_HORSEINFO = "horse info";
    public static final String CATEGORY_ENDERPEARLAID = "ender pearl aid";

    //Configurable values - language
    public static String SupportedLanguages;
    
    //Configurable values - info line
    public static boolean ShowInfoLine;

    //Configurable values - coordinates
    public static boolean ShowCoordinates;
    public static boolean UseYCoordinateColors;

    //Configurable values - compass
    public static boolean ShowCompass;

    //Configurable values - durability info
    public static boolean ShowDurabilityInfo;
    public static boolean ShowArmorDurability;
    public static boolean ShowItemDurability;
    public static int DurabilityUpdateFrequency;
    public static double DurabilityDisplayThresholdForArmor;
    public static double DurabilityDisplayThresholdForItem;
    public static int DurabilityLocationHorizontal;
    public static int DurabilityLocationVertical;

    //Configurable values - safe overlay
    public static int SafeOverlayDrawDistance;
    public static double SafeOverlayTransparency;
    public static boolean SafeOverlayDisplayInNether;
    public static boolean SafeOverlaySeeThroughWalls;

    //Configurable values - potion timers
    public static boolean ShowPotionTimers;

    //Configurable values - player locator
    public static boolean ShowDistanceToPlayers;
    public static int PlayerLocatorMinViewDistance;

    //Configurable values - eating aid
    public static boolean EnableEatingAid;
    public static boolean DontEatGoldenFood;

    //Configurable values - weapon swap
    public static boolean EnableWeaponSwap;
    public static boolean ScanHotbarForWeaponsFromLeftToRight;

    //Configurable values - fps
    public static boolean ShowFPS;

    //Configurable values - horse info
    public static boolean ShowHorseStatsOnF3Menu;
    public static int HorseInfoMaxViewDistance;

    //Configurable values - ender pearl aid
    public static boolean EnableEnderPearlAid;

    public static int DistanceMeasurerMode = 0;	//0=off, 1=simple, 2=complex
    public static int SafeOverlayMode = 0;		//0=off, 1=on
    public static int PlayerLocatorMode = 0;	//0=off, 1=on
    public static int HorseInfoMode = 0;		//0=off, 1=on
    
    
    
    Minecraft mc = Minecraft.getMinecraft();
    public static Configuration config = null;

    @Instance("ZyinHUD")
    public static ZyinHUD instance;

    @SidedProxy(clientSide = "zyin.zyinhud.ClientProxy", serverSide = "zyin.zyinhud.CommonProxy")
    public static CommonProxy proxy;

    public ZyinHUD()
    {
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        LoadConfigSettings(event.getSuggestedConfigurationFile());
        
        Localization.loadLanguages("/lang/zyinhud/", GetSupportedLanguages());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.registerRenderers();
        
        //needed for @ForgeSubscribe method subscriptions
        //MinecraftForge.EVENT_BUS.register(SafeOverlay.instance);
        MinecraftForge.EVENT_BUS.register(RenderTickHandler.instance);
        
        LoadTickHandlers();
        LoadKeyHandlers();
        
        
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event)
    {
        config.save();
    }

    private void LoadTickHandlers()
    {
        //Tick Handlers (for drawing on the HUD) are defined here
        TickRegistry.registerTickHandler(new HUDTickHandler(), Side.CLIENT);
    }

    private void LoadKeyHandlers()
    {
        //Key Bind Handlers (for hotkeys) are defined here
        boolean[] repeatFalse = {false};
        //boolean[] repeatTrue = {true};
        
        KeyBinding[] key_K = {new KeyBinding("Distance Measurer Toggle", 	Keyboard.KEY_K)};
        KeyBindingRegistry.registerKeyBinding(new DistanceMeasurerKeyHandler(key_K, repeatFalse));
        
        KeyBinding[] key_L = {new KeyBinding("Safe Overlay Toggle", 		Keyboard.KEY_L)};
        KeyBindingRegistry.registerKeyBinding(new SafeOverlayKeyHandler(key_L, repeatFalse));
        
        KeyBinding[] key_P = {new KeyBinding("Player Locator Toggle", 		Keyboard.KEY_P)};
        KeyBindingRegistry.registerKeyBinding(new PlayerLocatorKeyHandler(key_P, repeatFalse));
        
        KeyBinding[] key_G = {new KeyBinding("Eating Aid Hotkey", 			Keyboard.KEY_G)};
        KeyBindingRegistry.registerKeyBinding(new EatingAidKeyHandler(key_G, repeatFalse));
        
        KeyBinding[] key_F = {new KeyBinding("Weapon Swap Hotkey", 			Keyboard.KEY_F)};
        KeyBindingRegistry.registerKeyBinding(new WeaponSwapperKeyHandler(key_F, repeatFalse));
        
        KeyBinding[] key_O = {new KeyBinding("Horse Info Hotkey", 			Keyboard.KEY_O)};
        KeyBindingRegistry.registerKeyBinding(new HorseInfoKeyHandler(key_O, repeatFalse));
        
        KeyBinding[] key_C = {new KeyBinding("Ender Pearl Aid Hotkey", 		Keyboard.KEY_C)};
        KeyBindingRegistry.registerKeyBinding(new EnderPearlAidKeyHandler(key_C, repeatFalse));
    }
    
    private String[] GetSupportedLanguages()
    {
    	return SupportedLanguages.replace(" ","").split(",");
    }
    

    private void LoadConfigSettings(File configFile)
    {
        //load configuration settings
        config = new Configuration(configFile);
        config.load();	//config.save() is done in serverStopping() event
        Property p;
        

        //CATEGORY_LANGUAGE
        p = config.get(CATEGORY_LANGUAGE, "SupportedLanguages", DefaultSupportedLanguages);
        p.comment = "Languages must be added here in order to get loaded, in addition to adding a .properties file at /lang/zyinhud.";
        SupportedLanguages = p.getString();
        
        
        //CATEGORY_INFOLINE
        p = config.get(CATEGORY_INFOLINE, "ShowInfoLine", true);
        p.comment = "Enable/Disable the entire info line in the top left part of the screen. This includes coordinates, compass, mod status, etc.";
        ShowInfoLine = p.getBoolean(true);
        
        
        //CATEGORY_COORDINATES
        p = config.get(CATEGORY_COORDINATES, "ShowCoordinates", true);
        p.comment = "Enable/Disable showing your coordinates.";
        ShowCoordinates = p.getBoolean(true);
        
        p = config.get(CATEGORY_COORDINATES, "UseYCoordinateColors", true);
        p.comment = "Color code the Y (height) coordinate based on what ores can spawn at that level.";
        UseYCoordinateColors = p.getBoolean(true);
        
        
        //CATEGORY_COMPASS
        p = config.get(CATEGORY_COMPASS, "ShowCompass", true);
        p.comment = "Enable/Disable showing the compass.";
        ShowCompass = p.getBoolean(true);
        
        
        //CATEGORY_DURABILITYINFO
        p = config.get(CATEGORY_DURABILITYINFO, "ShowDurabilityInfo", true);
        p.comment = "Enable/Disable durability info.";
        ShowDurabilityInfo = p.getBoolean(true);
        
        p = config.get(CATEGORY_DURABILITYINFO, "ShowArmorDurability", true);
        p.comment = "Enable/Disable showing breaking armor.";
        ShowArmorDurability = p.getBoolean(true);
        
        p = config.get(CATEGORY_DURABILITYINFO, "ShowItemDurability", true);
        p.comment = "Enable/Disable showing breaking items.";
        ShowItemDurability = p.getBoolean(true);
        
        p = config.get(CATEGORY_DURABILITYINFO, "DurabilityDisplayThresholdForArmor", 0.9);
        p.comment = "Display when armor gets damaged more than this fraction of its durability.";
        DurabilityDisplayThresholdForArmor = p.getDouble(0.9);
        
        p = config.get(CATEGORY_DURABILITYINFO, "DurabilityDisplayThresholdForItem", 0.9);
        p.comment = "Display when an item gets damaged more than this fraction of its durability.";
        DurabilityDisplayThresholdForItem = p.getDouble(0.9);
        
        p = config.get(CATEGORY_DURABILITYINFO, "DurabilityUpdateFrequency", 50);
        p.comment = "Update the HUD every XX game ticks (~100 = 1 second)";
        DurabilityUpdateFrequency = p.getInt();
        
        p = config.get(CATEGORY_DURABILITYINFO, "DurabilityLocationHorizontal", 20);
        p.comment = "The horizontal position of the durability icons. 0 is left, 400 is far right.";
        DurabilityLocationHorizontal = p.getInt();
        
        p = config.get(CATEGORY_DURABILITYINFO, "DurabilityLocationVertical", 20);
        p.comment = "The vertical position of the durability icons. 0 is top, 200 is very bottom.";
        DurabilityLocationVertical = p.getInt();
        
        
        //CATEGORY_SAFEOVERLAY
        p = config.get(CATEGORY_SAFEOVERLAY, "SafeOverlayDrawDistance", 20);
        p.comment = "How far away unsafe spots should be rendered around the player measured in blocks. This can be changed in game with - + L and + + L.";
        SafeOverlayDrawDistance = p.getInt(20);
        
        p = config.get(CATEGORY_SAFEOVERLAY, "SafeOverlayTransparency", 0.3);
        p.comment = "The transparency of the unsafe marks. Must be between greater than 0.1 and less than or equal to 1.";
        SafeOverlayTransparency = p.getDouble(0.3);
        
        p = config.get(CATEGORY_SAFEOVERLAY, "SafeOverlayDisplayInNether", false);
        p.comment = "Enable/Disable showing unsafe areas in the Nether.";
        SafeOverlayDisplayInNether = p.getBoolean(false);
        
        p = config.get(CATEGORY_SAFEOVERLAY, "SafeOverlaySeeThroughWalls", false);
        p.comment = "Enable/Disable showing unsafe areas through walls. Toggle in game with Ctrl + L.";
        SafeOverlaySeeThroughWalls = p.getBoolean(false);
        
        
        //CATEGORY_POTIONTIMERS
        p = config.get(CATEGORY_POTIONTIMERS, "ShowPotionTimers", true);
        p.comment = "Enable/Disable showing the time remaining on potions.";
        ShowPotionTimers = p.getBoolean(true);
        
        
        //CATEGORY_PLAYERLOCATOR
        p = config.get(CATEGORY_PLAYERLOCATOR, "ShowDistanceToPlayers", false);
        p.comment = "Show how far away you are from the other players next to their name.";
        ShowDistanceToPlayers = p.getBoolean(false);
        
        p = config.get(CATEGORY_PLAYERLOCATOR, "PlayerLocatorMinViewDistance", 10);
        p.comment = "Stop showing player names when they are this close (distance measured in blocks).";
        PlayerLocatorMinViewDistance = p.getInt(10);
        
        
        //CATEGORY_EATINGAID
        p = config.get(CATEGORY_EATINGAID, "EnableEatingAid", true);
        p.comment = "Enables pressing a hotkey (default=G) to eat food even if it is  in your inventory and not your hotbar.";
        EnableEatingAid = p.getBoolean(true);
        
        p = config.get(CATEGORY_EATINGAID, "DontEatGoldenFood", true);
        p.comment = "Enable/Disable using golden apples and golden carrots as food.";
        DontEatGoldenFood = p.getBoolean(true);
        
        
        //CATEGORY_WEAPONSWAP
        p = config.get(CATEGORY_WEAPONSWAP, "EnableWeaponSwap", true);
        p.comment = "Enables pressing a hotkey (default=F) to swap between your sword and bow.";
        EnableWeaponSwap = p.getBoolean(true);
        
        p = config.get(CATEGORY_WEAPONSWAP, "ScanHotbarForWeaponsFromLeftToRight", true);
        p.comment = "Set to false to scan the hotbar for swords and bows from right to left. Only matters if you have multiple swords/bows in your hotbar.";
        ScanHotbarForWeaponsFromLeftToRight = p.getBoolean(true);
        
        
        //CATEGORY_FPS
        p = config.get(CATEGORY_FPS, "ShowFPS", false);
        p.comment = "Enable/Disable showing your FPS at the end of the Info Line.";
        ShowFPS = p.getBoolean(false);
        
        
        //CATEGORY_HORSEINFO
        p = config.get(CATEGORY_HORSEINFO, "ShowHorseStatsOnF3Menu", true);
        p.comment = "Enable/Disable showing the stats of the horse you're riding on the F3 screen.";
        ShowHorseStatsOnF3Menu = p.getBoolean(true);
        
        p = config.get(CATEGORY_HORSEINFO, "HorseInfoMaxViewDistance", 8);
        p.comment = "How far away horse stats will be rendered on the screen (distance measured in blocks).";
        HorseInfoMaxViewDistance = p.getInt(8);
        
        
        //CATEGORY_ENDERPEARLAID
        p = config.get(CATEGORY_ENDERPEARLAID, "EnableEnderPearlAid", true);
        p.comment = "Enables pressing a hotkey (default=C) to use an enderpearl even if it is  in your inventory and not your hotbar.";
        EnableEnderPearlAid = p.getBoolean(true);
        
        
        
    }
}
