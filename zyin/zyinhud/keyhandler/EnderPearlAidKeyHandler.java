package zyin.zyinhud.keyhandler;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import zyin.zyinhud.ZyinHUD;
import zyin.zyinhud.mods.EatingAid;
import zyin.zyinhud.mods.EnderPearlAid;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;

public class EnderPearlAidKeyHandler extends KeyHandler
{
    private Minecraft mc = Minecraft.getMinecraft();
    private EnumSet tickTypes = EnumSet.of(TickType.CLIENT);

    private int eatingTimer = 0;
    private boolean isEating = false;

    public EnderPearlAidKeyHandler(KeyBinding[] keyBindings, boolean[] repeatings)
    {
        super(keyBindings, repeatings);
    }

    @Override
    public String getLabel()
    {
        return "Ender Pearl Aid Key Handler";
    }

    @Override
    public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat)
    {
        if (!tickEnd)
        {
            return;    //this fixes an issue with the method being called twice
        }

        if (mc.currentScreen != null)
        {
            return;    //don't activate if the user is looking at a GUI
        }

        if (EnderPearlAid.Enabled)
            EnderPearlAid.UseEnderPearl();
    }

    @Override
    public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd)
    {
        if (!tickEnd)
        {
            return;    //this fixes an issue with the method being called twice
        }
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return tickTypes;
    }
}