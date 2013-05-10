package fr.ungeek.Upsilon;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

/**
 * User: PunKeel
 * Date: 5/9/13
 * Time: 12:48 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class DailyGift implements Listener {
	Main m;

	public DailyGift(Main m) {
		this.m = m;
	}

	@EventHandler()
	public void onJoin(PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		String today = m.getDate();
		String yesterday = m.getDate(-1);
		final String message;
		if (p.hasMetadata("ups_lastjoin")) {
			List<MetadataValue> ups_lastjoin = p.getMetadata("ups_lastjoin");
			String LastJoin = "";
			if (ups_lastjoin.size() != 0) {
				LastJoin = p.getMetadata("ups_lastjoin").get(0).asString();
			}
			if (!LastJoin.equals(today)) {
				int jours = 1;
				p.removeMetadata("ups_lastjoin", m);
				p.removeMetadata("ups_follow", m);
				p.setMetadata("ups_lastjoin", new FixedMetadataValue(m, today));
				if (LastJoin.equals(yesterday)) {
					// consecutif
					jours = p.getMetadata("ups_follow").get(0).asInt() + 1;
					int gain = ((jours > 5) ? 50 : (jours * 10));
					p.setMetadata("ups_follow", new FixedMetadataValue(m, jours));
					message = (m.getTAG() + "Tu as reçu " + gain + " ƒ pour tes " + jours + " jours de présence à la suite !");
					m.econ.depositPlayer(p.getName(), gain);
				} else {
					// pas consecutif
					p.setMetadata("ups_follow", new FixedMetadataValue(m, jours));
					message = m.getTAG() + "Tu as reçu 10 ƒ pour ton premier jour de présence consécutif!";
					m.econ.depositPlayer(p.getName(), 10);
				}
				if (!message.isEmpty()) {
					Bukkit.getScheduler().scheduleSyncDelayedTask(m, new Runnable() {
						public void run() {
							p.sendMessage(message);
						}
					}, 20);
				}
			}

		} else {
			p.setMetadata("ups_lastjoin", new FixedMetadataValue(m, today));
			p.setMetadata("ups_follow", new FixedMetadataValue(m, 1));
		}
	}
}
