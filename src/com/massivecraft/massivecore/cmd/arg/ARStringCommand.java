package com.massivecraft.massivecore.cmd.arg;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;

import com.massivecraft.massivecore.MassiveCoreEngineCommandRegistration;
import com.massivecraft.massivecore.MassiveException;
import com.massivecraft.massivecore.collections.MassiveList;
import com.massivecraft.massivecore.util.Txt;

// We operate without the leading slash as much as possible.
// We return the command without leading slash.
// If the player supplies a leading slash we assume they mean some WorldEdit double slash first.
// Then we test without if nothing found.
public class ARStringCommand extends ARAbstract<String>
{
	// -------------------------------------------- //
	// INSTANCE & CONSTRUCT
	// -------------------------------------------- //
	
	private static ARStringCommand i = new ARStringCommand();
	public static ARStringCommand get() { return i; }
	
	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //

	@Override
	public String getTypeName()
	{
		return "command";
	}
	
	@Override
	public String read(String arg, CommandSender sender) throws MassiveException
	{
		// Require base command (something at all).
		if (arg.isEmpty()) throw new MassiveException().addMsg("<b>You must at the very least supply a base command.");
		List<String> args = argAsArgs(arg);
		
		// Smart management of first slash ...
		String alias = args.get(0);
		
		// ... if there is such a command just return ...
		Command command = getCommand(alias);
		if (command != null) return arg;
		
		// ... otherwise if starting with slash return it and hope for the best ...
		if (alias.startsWith("/")) return arg.substring(1);
		
		// ... otherwise it's slashless and we return it as is.
		return arg;
	}
	
	@Override
	public Collection<String> getTabList(CommandSender sender, String arg)
	{
		// Split the arg into a list of args #inception-the-movie!
		List<String> args = argAsArgs(arg);
		
		// Tab completion of base commands
		if (args.size() <= 1) return getKnownCommands().keySet();
		
		// Get command alias and subargs
		String alias = args.get(0);
		
		// Attempt using the tab completion of that command.
		Command command = getCommandSmart(alias);
		if (command == null) return Collections.emptySet();
		List<String> subcompletions = command.tabComplete(sender, alias, args.subList(1, args.size()).toArray(new String[0]));
		
		String prefix = Txt.implode(args.subList(0, args.size() - 1), " ") + " ";
		List<String> ret = new MassiveList<String>();
		
		for (String subcompletion : subcompletions)
		{
			String completion = prefix + subcompletion;
			ret.add(completion);
		}
		
		return ret;
	}
	
	// -------------------------------------------- //
	// UTIL
	// -------------------------------------------- //
	
	public static List<String> argAsArgs(String arg)
	{
		String[] args = StringUtils.split(arg, ' ');
		return new MassiveList<String>(Arrays.asList(args));
	}
	
	public static Map<String, Command> getKnownCommands()
	{
		SimpleCommandMap simpleCommandMap = MassiveCoreEngineCommandRegistration.getSimpleCommandMap();
		Map<String, Command> knownCommands = MassiveCoreEngineCommandRegistration.getSimpleCommandMapDotKnownCommands(simpleCommandMap);
		return knownCommands;
	}
	
	public static Command getCommand(String name)
	{
		return getKnownCommands().get(name);
	}
	
	public static Command getCommandSmart(String name)
	{
		Command ret = getCommand(name);
		if (ret != null) return ret;
		
		if ( ! name.startsWith("/")) return null;
		name = name.substring(1);
		return getCommand(name);
	}

}
