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
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.network.protocol.ContainerOpenPacket;
import cn.nukkit.network.protocol.UpdateBlockPacket;
import com.smallaswater.anvilplus.AnvilPlus;

public class CraftCommand extends Command implements Listener {
    BlockCraftingTable craftingTable = new BlockCraftingTable();
    Level world;
    Location location;

    public CraftCommand() {
        super("craft");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] strings) {
        if (sender.hasPermission("craft.use")) {
            Player player = (Player) sender;

            world = player.getLevel();
            location = player.getLocation();

            sendBlock(player, location, BlockID.CRAFTING_TABLE);

            CraftingTableOpenEvent ev = new CraftingTableOpenEvent(player, craftingTable);

            AnvilPlus.getInstance().getServer().getPluginManager().callEvent(ev);
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
    public void onClose2(CraftingTableOpenEvent evt) {
        System.out.println("Fired the event2");
        if (evt.isCancelled() && world != null) {
            System.out.println("Crafting closed");
            sendBlock(evt.getPlayer(), location, BlockID.AIR);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent evt) {
        System.out.println("Crafting closed0");
        sendBlock(evt.getPlayer(), location, BlockID.AIR);
        if (craftingTable.isValid()) {
            System.out.println("Crafting closed1");
            sendBlock(evt.getPlayer(), location, BlockID.AIR);
        }
        if (evt.getInventory().getType() == InventoryType.CRAFTING) {
            System.out.println("Crafting closed2");
            sendBlock(evt.getPlayer(), location, BlockID.AIR);
        }
        if (evt.getInventory().getType() == InventoryType.CRAFTING && world != null) {
            System.out.println("Crafting closed3");
            sendBlock(evt.getPlayer(), location, BlockID.AIR);
        }
    }

    private void sendBlock(Player p, Location loc, int block) {
        if (loc == null) {
            System.out.println("sendBlock: loc == null");
            return;
        }
        UpdateBlockPacket updateBlockPacket = new UpdateBlockPacket();
        updateBlockPacket.blockRuntimeId = GlobalBlockPalette.getOrCreateRuntimeId(p.protocol, block, 0);
        updateBlockPacket.flags = UpdateBlockPacket.FLAG_ALL_PRIORITY;
        updateBlockPacket.x = (int) loc.x;
        updateBlockPacket.y = (int) loc.y + 2;
        updateBlockPacket.z = (int) loc.z;
        p.dataPacket(updateBlockPacket);
    }
}
