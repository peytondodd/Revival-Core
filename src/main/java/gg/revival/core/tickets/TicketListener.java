package gg.revival.core.tickets;

import gg.revival.core.Revival;
import gg.revival.core.tools.Permissions;
import lombok.Getter;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;

public class TicketListener implements Listener {

    @Getter private Revival revival;

    public TicketListener(Revival revival) {
        this.revival = revival;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if(!player.hasPermission(Permissions.TICKETS_VIEW)) return;

        if(!revival.getCfg().TICKETS_ENABLED) return;

        if(revival.getTicketManager().getLoadedTickets().isEmpty()) return;

        new FancyMessage(
                revival.getTicketManager().getLoadedTickets().size() + " open tickets")
                .color(ChatColor.AQUA).then(" [Click here to view]")
                .color(ChatColor.GREEN)
                .command("/tickets").send(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player)event.getWhoClicked();

        if(!revival.getCfg().TICKETS_ENABLED) return;
        if(!player.hasPermission(Permissions.TICKETS_VIEW)) return;
        if(event.getClickedInventory() == null || event.getClickedInventory().getName() == null || !event.getClickedInventory().getName().equals(ChatColor.BLACK + "Tickets")) return;
        if(event.getCurrentItem() == null || !event.getCurrentItem().getType().equals(Material.SKULL_ITEM)) return;

        ItemStack icon = event.getCurrentItem();
        SkullMeta meta = (SkullMeta)icon.getItemMeta();
        String owner = meta.getOwner();

        if(event.getClick().equals(ClickType.LEFT)) {
            if(owner != null && Bukkit.getPlayer(owner) != null) {
                player.teleport(Bukkit.getPlayer(owner));
                player.sendMessage(ChatColor.GREEN + "You have been brought to " + Bukkit.getPlayer(owner).getName());
            }
        }

        if(event.getClick().equals(ClickType.RIGHT)) {
            List<String> lore = meta.getLore();
            UUID ticketId = null;

            for(String lines : lore) {
                if(lines.contains(ChatColor.GOLD + "ID: "))
                    ticketId = UUID.fromString(ChatColor.stripColor(lines.replace("ID: ", "")));
            }

            if(ticketId != null) {
                Ticket ticket = revival.getTicketManager().getTicketByUUID(ticketId);
                revival.getTicketManager().closeTicket(ticket, player.getUniqueId());

                revival.getTicketGui().show(player, event.getClickedInventory(), revival.getTicketManager().getLoadedTickets());

                player.sendMessage(ChatColor.GREEN + "Ticket closed");
            }
        }

        event.setCancelled(true);
    }

}
