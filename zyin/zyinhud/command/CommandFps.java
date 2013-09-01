package zyin.zyinhud.command;

import zyin.zyinhud.ZyinHUD;
import zyin.zyinhud.mods.Fps;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class CommandFps  extends CommandBase
{
	@Override
	public String getCommandName()
	{
		return "fps";
	}

	@Override
	public String getCommandUsage(ICommandSender iCommandSender)
	{
		return "commands.fps.usage";
	}

	@Override
	public void processCommand(ICommandSender iCommandSender, String[] parameters)
	{
		Fps.ToggleEnabled();
	}
}
