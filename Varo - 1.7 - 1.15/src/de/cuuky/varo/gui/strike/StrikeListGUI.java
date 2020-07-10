package de.cuuky.varo.gui.strike;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import de.cuuky.cfw.item.ItemBuilder;
import de.cuuky.cfw.menu.SuperInventory;
import de.cuuky.cfw.menu.utils.PageAction;
import de.cuuky.varo.Main;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.entity.player.stats.stat.Strike;
import de.cuuky.varo.gui.MainMenu;

public class StrikeListGUI extends SuperInventory {

	private Player target;

	public StrikeListGUI(Player opener, Player target) {
		super("Strikes", opener, 54, false);

		this.target = target;

		this.setModifier = true;
		Main.getCuukyFrameWork().getInventoryManager().registerInventory(this);
		open();
	}

	@Override
	public boolean onBackClick() {
		new MainMenu(opener);
		return true;
	}

	@Override
	public void onClick(InventoryClickEvent event) {}

	@Override
	public void onClose(InventoryCloseEvent event) {}

	@Override
	public void onInventoryAction(PageAction action) {}

	@Override
	public boolean onOpen() {
		ArrayList<Strike> list = VaroPlayer.getPlayer(target).getStats().getStrikes();

		int start = getSize() * (getPage() - 1);
		for (int i = 0; i != getSize(); i++) {
			Strike strike;
			try {
				strike = list.get(start);
			} catch (IndexOutOfBoundsException e) {
				break;
			}

			linkItemTo(i, new ItemBuilder().displayname("§c" + strike.getStrikeNumber()).itemstack(new ItemStack(Material.PAPER)).lore(new String[] { "§7Reason: §c" + strike.getReason(), "§7Striker: §c" + strike.getStriker(), "§7Date: §c" + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(strike.getAcquiredDate()) }).build(), new Runnable() {

				@Override
				public void run() {
					if (!opener.hasPermission("varo.admin"))
						return;

					// TODO: DO OPTION STUFF
				}
			});
			start++;
		}

		return calculatePages(list.size(), getSize()) == page;
	}
}
