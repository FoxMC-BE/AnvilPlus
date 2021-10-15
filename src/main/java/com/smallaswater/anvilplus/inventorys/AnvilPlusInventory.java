package com.smallaswater.anvilplus.inventorys;




import cn.nukkit.Player;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.inventory.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBookEnchanted;
import cn.nukkit.item.ItemDurable;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.ContainerOpenPacket;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.protocol.RemoveEntityPacket;
import com.smallaswater.anvilplus.AnvilPlus;
import com.smallaswater.anvilplus.craft.BaseCraftItem;
import com.smallaswater.anvilplus.craft.CraftItemManager;
import com.smallaswater.anvilplus.craft.defaults.CraftItem;
import com.smallaswater.anvilplus.events.AnvilSetEchoItemEvent;
import com.smallaswater.anvilplus.events.PlayerAnvilEchoItemEvent;
import com.smallaswater.anvilplus.events.PlayerUseAnvilEvent;
import com.smallaswater.anvilplus.events.PlayerUseCraftItemEvent;
import com.smallaswater.anvilplus.exception.PlayerMoneyErrorException;
import com.smallaswater.anvilplus.items.OccupyItem;

import java.util.*;


/**
 * @author SmallasWater
 * Create on 2021/1/7 11:13
 * Package com.smallaswater.anvilplus.inventorys
 */
public class AnvilPlusInventory extends ContainerInventory implements InventoryHolder{

    private static final int TOOL_ITEM_SLOT = 0;

    private static final int ITEM_SLOT = 2;

    private static final int ECHO_ITEM = 4;

    private Player player;

    public long id;


    public String title;


    AnvilPlusInventory(Player player, BaseHolder holder) {
        super(holder, InventoryType.HOPPER);
        this.player = player;

    }



    private Map<Integer,Item> getOccupyItems(){
        LinkedHashMap<Integer,Item> items = new LinkedHashMap<>();
        items.put(1,new OccupyItem());
        items.put(3,new OccupyItem());
        items.put(4,new OccupyItem());
        return items;
    }


    @Override
    public void onOpen(Player player)
    {
        ContainerOpenPacket pk = new ContainerOpenPacket();
        pk.windowId = player.getWindowId(this);
        pk.type = this.getType().getNetworkType();
        pk.entityId = id;
        player.dataPacket(pk);
        setContents(getOccupyItems());
    }

    @Override
    public void onClose(Player player)
    {
        toClose(player);
        super.onClose(player);
        RemoveEntityPacket pk = new RemoveEntityPacket();
        pk.eid = id;
        player.dataPacket(pk);
        AnvilPlus.inventory.remove(player);
        AnvilPlus.saveAnvilBlock.remove(player);
    }

    private void toClose(Player player){

        Item local = getItem(TOOL_ITEM_SLOT);
        Item second = getItem(ITEM_SLOT);
        Position block = AnvilPlus.saveAnvilBlock.get(player).add(0,0.5);
        if(local != null && local.getId() != 0){
            player.level.dropItem(block,local);
        }
        if(second != null && second.getId() != 0){
            player.level.dropItem(block,second);
        }

    }

    private BaseCraftItem getEchoItem(Player player, Item local, Item second){
        BaseCraftItem craftItem = onEchoItem(player, local, second);
        if(craftItem != null){
            PlayerAnvilEchoItemEvent event = new PlayerAnvilEchoItemEvent(player,local,second,craftItem,this);
            Server.getInstance().getPluginManager().callEvent(event);
            if(event.isCancelled()){
                return null;
            }
            craftItem = event.getCraftItem();
        }
        return craftItem;
    }

    private BaseCraftItem onEchoItem(Player player, Item local, Item second){
        if(local == null || second == null){
            return null;
        }

        BaseCraftItem craft = CraftItemManager.getCraftItem(local, second);

        if(craft != null){

            PlayerUseCraftItemEvent event = new PlayerUseCraftItemEvent(player,local,second,craft);
            Server.getInstance().getPluginManager().callEvent(event);
            if(event.isCancelled()){
                return null;
            }
            craft = event.getCraftItem();
            craft.onEcho(local, second);

            return craft;
        }else{
            craft = new CraftItem(local,second,null);
        }

        return defaultEnchant(craft,second);


    }

    private static LinkedHashMap<Player,Double> exp = new LinkedHashMap<>();
    @Override
    public void onSlotChange(int index, Item before, boolean send) {
        super.onSlotChange(index, before, send);
        Item local = this.getItem(TOOL_ITEM_SLOT);
        Item second = this.getItem(ITEM_SLOT);
        Item echos = this.getItem(ECHO_ITEM);
        BaseCraftItem echoI = getEchoItem(player, local, second);
        if(local.getId() != 0 && second.getId() != 0){
            try {
                if (echoI != null) {
                    Item echo = echoI.getEcho();
                    if (echo != null && echo.getId() != 0) {
                        //Consumption when the player takes out the item
                        if (index == ECHO_ITEM && before != null && before.getId() != 0 && !(before instanceof OccupyItem)) {
                            if (AnvilPlus.saveAnvilBlock.containsKey(player)) {
                                this.setItem(TOOL_ITEM_SLOT, echoI.getLocal());
                                this.setItem(ITEM_SLOT, echoI.getSecond());
                                if (echoI.getLocal().getCount() == 0 || echoI.getSecond().getCount() == 0) {
                                    this.setItem(ECHO_ITEM, new OccupyItem());
                                }
                                if (!AnvilPlus.getLoadMoney().reduceMoney(player, exp.get(player))) {
                                    throw new PlayerMoneyErrorException(player, exp.get(player), echos);
                                }
                                PlayerUseAnvilEvent event = new PlayerUseAnvilEvent(player, echoI, AnvilPlus.saveAnvilBlock.get(player));
                                Server.getInstance().getPluginManager().callEvent(event);

                                player.getLevel().addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_RANDOM_ANVIL_USE);

                            }

                        } else {
                            exp.put(player, echoI.getUseMoney());
                            AnvilSetEchoItemEvent event = new AnvilSetEchoItemEvent(player, local, second, echoI, exp.get(player));
                            Server.getInstance().getPluginManager().callEvent(event);
                            exp.put(player, event.getExp());
                            CompoundTag tag = echo.getNamedTag();
                            if (event.isCancelledItem()) {
                                if (tag == null) {
                                    tag = new CompoundTag();
                                }
                                tag.putString("tag_name", "OccupyItem");
                                echo.setCompoundTag(tag);
                                echo.setCustomName(AnvilPlus.format("&r&cTemporarily undesirable " + event.getCause()));
                            }

                            //防止重复触发onSlotChange
                            this.slots.put(ECHO_ITEM, echo);
                            this.sendSlot(ECHO_ITEM, this.getViewers());
                        }
                    }else{
                        if(!(echos instanceof OccupyItem)) {
                            this.setItem(ECHO_ITEM, new OccupyItem());

                        }
                    }
                }else{
                    if(!(echos instanceof OccupyItem)) {
                        this.setItem(ECHO_ITEM, new OccupyItem());

                    }
                }
            }catch (PlayerMoneyErrorException e){
                AnvilPlus.getInstance().getLogger().info(AnvilPlus.format("&cPlayer "+e.getPlayer().getName()+" Money is abnormal when using an anvil"));
            }
        }else{
            if(!(echos instanceof OccupyItem)) {
                this.setItem(ECHO_ITEM, new OccupyItem());

            }
        }
        this.sendContents(player);


    }

    @Override
    public String getTitle() {
        return title;
    }


    private BaseCraftItem defaultEnchant(BaseCraftItem re,Item second){
        second = second.clone();
        Item result = re.getLocal().clone();
        int countEnchant = 0;

        if (re.getLocal().getId() != 0 && second.getId() != 0) {
            if (re.getLocal().getId() != 0 && second.getId() != 0) {
                boolean isFix = false;
                if(result instanceof ItemDurable){
                    if(result.equals(second,false,false)){
                        if(result.getDamage() > 0){
                            int damage = result.getDamage() - (second.getMaxDurability() - re.getSecond().getDamage());
                            if(damage < 0){
                                damage = 0;
                            }
                            result.setDamage(damage);
                            isFix = true;
                        }
                    }
                }
                if(second instanceof ItemBookEnchanted || second.getId() == result.getId()){
                    ArrayList<Enchantment> enchantments = new ArrayList<>(Arrays.asList(second.getEnchantments()));
                    Enchantment enchantment = null;
                    for(Enchantment enchantment1: enchantments){
                        if(enchantment1.getLevel() > 0 && enchantment1.getId() >= 0){
                            enchantment = enchantment1;
                        }
                        if(enchantment != null){
                            Enchantment localEnchantment = re.getLocal().getEnchantment(enchantment.getId());
                            if (localEnchantment != null) {
                                int startLevel = localEnchantment.getLevel();
                                int level = Math.max(localEnchantment.getLevel(), enchantment.getLevel());
                                if (localEnchantment.getLevel() == enchantment.getLevel()) {
                                    ++level;
                                } else{
                                    if(localEnchantment.getLevel() > enchantment.getLevel()){
                                        if(countEnchant == 0) {
                                            continue;
                                        }
                                    }
                                }
                                enchantment.setLevel(level);
                                if(startLevel == enchantment.getLevel()){
                                    if(countEnchant == 0) {
                                        continue;
                                    }
                                }
                                if(enchantment.canEnchant(result)) {
                                    result.addEnchantment(enchantment);
                                    countEnchant++;
                                }

                            } else {
                                if(enchantment.canEnchant(result)){
                                    result.addEnchantment(enchantment);
                                    countEnchant++;
                                }
                            }
                        }
                    }
                }
                if(countEnchant > 0 || isFix){
                    re.onEcho(re.getLocal(),re.getSecond());
                    re.setEcho(result);
                }

            }

        }

        return re;
    }


    @Override
    public Inventory getInventory() {
        return this;
    }
}
