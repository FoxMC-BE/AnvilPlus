package com.smallaswater.anvilplus.commands;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAnvil;
import cn.nukkit.block.BlockCraftingTable;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.player.CraftingTableOpenEvent;
import com.smallaswater.anvilplus.AnvilPlus;
import com.smallaswater.anvilplus.events.AnvilBreakEvent;
import com.smallaswater.anvilplus.utils.Tools;

public class AnvilCommand extends Command{

    public AnvilCommand() {
        super("anvil");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] strings) {
        if (sender.hasPermission("anvil.use")) {
            Player player = (Player) sender;

            Tools.getAnvilInventory(player);
            BlockAnvil anvil = new BlockAnvil();
            AnvilPlus.saveAnvilBlock.put(player,anvil);
            return true;
        }
        return false;
    }
}
