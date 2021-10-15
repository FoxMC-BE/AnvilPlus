package com.smallaswater.anvilplus.commands;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.*;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.event.player.CraftingTableOpenEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.level.Location;
import cn.nukkit.network.protocol.ContainerOpenPacket;
import cn.nukkit.network.protocol.UpdateBlockPacket;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class CraftCommand extends Command implements Listener {
    BlockCraftingTable craftingTable = new BlockCraftingTable();
    static Long2ObjectOpenHashMap<Location> locations = new Long2ObjectOpenHashMap<>();

    public CraftCommand() {
        super("craft");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] strings) {
        if (sender.hasPermission("craft.use") && sender instanceof Player) {
            Player player = (Player) sender;

            Location location = player.getLocation();
            locations.put(player.getId(), location);

            sendBlock(player, location, BlockID.CRAFTING_TABLE);

            CraftingTableOpenEvent ev = new CraftingTableOpenEvent(player, craftingTable);
            Server.getInstance().getPluginManager().callEvent(ev);

            if (ev.isCancelled()) {
                return true;
            }

            player.craftingType = Player.CRAFTING_BIG;
            player.setCraftingGrid(player.getUIInventory().getBigCraftingGrid());

            if (player.protocol >= 407) {
                ContainerOpenPacket pk = new ContainerOpenPacket();
                pk.windowId = -1;
                pk.type = 1;
                pk.x = (int) location.x;
                pk.y = (int) location.y + 2;
                pk.z = (int) location.z;
                pk.entityId = player.getId();
                player.dataPacket(pk);
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent evt) {
        locations.remove(evt.getPlayer().getId());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent evt) {
        Location location = locations.remove(evt.getPlayer().getId());
        if (location != null) {
            sendBlock(evt.getPlayer(), location, Block.AIR);
        }
    }

    private void sendBlock(Player p, Location location, int block) {
        UpdateBlockPacket updateBlockPacket = new UpdateBlockPacket();
        updateBlockPacket.blockRuntimeId = GlobalBlockPalette.getOrCreateRuntimeId(p.protocol, block, 0);
        updateBlockPacket.flags = UpdateBlockPacket.FLAG_ALL_PRIORITY;
        updateBlockPacket.x = (int) location.x;
        updateBlockPacket.y = (int) location.y + 2;
        updateBlockPacket.z = (int) location.z;
        p.dataPacket(updateBlockPacket);
    }
}
