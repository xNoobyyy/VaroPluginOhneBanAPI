package de.cuuky.varo.command.essentials;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.cuuky.varo.Main;
import de.cuuky.varo.configuration.configurations.config.ConfigSetting;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.gui.report.ReportGUI;
import de.cuuky.varo.gui.report.ReportListGUI;

public class ReportCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
//		VaroPlayer vp = (sender instanceof Player ? VaroPlayer.getPlayer((Player) sender) : null);
		if(!ConfigSetting.REPORTSYSTEM_ENABLED.getValueAsBoolean()) {
			sender.sendMessage(Main.getPrefix() + "§cReports §7wurden in der Config deaktiviert!");
			return false;
		}

		if(!(sender instanceof Player)) {
			sender.sendMessage(Main.getPrefix() + "Only for Player");
			return false;
		}

		Player player = Bukkit.getPlayerExact(sender.getName());

		if(args.length == 0 || args.length > 1) {
			sender.sendMessage(Main.getPrefix() + "§7------ §cReport §7------");
			sender.sendMessage(Main.getPrefix() + "§c/report §7<Player>");
			if(sender.hasPermission("varo.reports"))
				sender.sendMessage(Main.getPrefix() + "§c/report list");
			sender.sendMessage(Main.getPrefix() + "§7-------------------");
			return false;
		}

		if(args[0].equals("list") && player.hasPermission("varo.reports")) {
			new ReportListGUI(player);
			return false;
		}

		VaroPlayer reported = VaroPlayer.getPlayer(args[0]);
		if(reported == null) {
			sender.sendMessage(Main.getPrefix() + "Dieser Spieler existiert nicht!");
			return false;
		}

		new ReportGUI(player, reported);
		return true;
	}
}