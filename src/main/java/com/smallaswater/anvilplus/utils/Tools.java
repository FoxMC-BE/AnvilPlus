package com.smallaswater.anvilplus.utils;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.network.protocol.AddEntityPacket;
import com.smallaswater.anvilplus.AnvilPlus;
import com.smallaswater.anvilplus.inventorys.AnvilPlusInventory;
import com.smallaswater.anvilplus.inventorys.BaseHolder;

/**
 * @author SmallasWater
 * Create on 2021/1/4 15:43
 * Package com.smallaswater.anvilplus.utils
 */
public class Tools {

    public static void getAnvilInventory(Player player){
        long id = Entity.entityCount++;
        AddEntityPacket fakeEntity = new AddEntityPacket();
        fakeEntity.entityUniqueId = id;
        fakeEntity.entityRuntimeId = id;
        fakeEntity.type = 96;
        fakeEntity.x = (float)player.x;
        fakeEntity.y = (float)((player.y+3)>256 ? 256 : player.y+3);
        fakeEntity.z = (float)player.z;
        fakeEntity.metadata.putString(4, "§l§eAnvil").putByte(44, 8).putInt(45, InventoryType.HOPPER.getDefaultSize());
        player.dataPacket(fakeEntity);
        BaseHolder inventory = new BaseHolder(player);
        ((AnvilPlusInventory)inventory.getInventory()).id = id;
        ((AnvilPlusInventory)inventory.getInventory()).title = "§l§eAnvil";
        AnvilPlus.inventory.put(player, (AnvilPlusInventory) inventory.getInventory());

        player.addWindow(inventory.getInventory());
    }

}
