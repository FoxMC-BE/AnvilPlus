package com.smallaswater.anvilplus.items;

import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import com.smallaswater.anvilplus.AnvilPlus;

/**
 * @author SmallasWater
 * Create on 2021/1/24 16:22
 * Package com.smallaswater.anvilplus.items
 */
public class AnvilNameTagItem extends Item {

    private static final String NAME = "&c&lnotSet";

    public static final String ITEM_TAG = "anvilItem";

    public static final String NAME_TAG = "anvilName";

    public AnvilNameTagItem() {
        super(421);
        this.setCustomName(AnvilPlus.format("&r&e<Right click/click on the ground to rename>"));
        this.setLore(AnvilPlus.format("\n&r&7Items can be renamed\n&r------------\n&r&Current binding name:\n\n&r"+ NAME));
        CompoundTag tag = this.getNamedTag();
        tag.putString(ITEM_TAG,this.getClass().getSimpleName());
        this.setNamedTag(tag);
    }

    public static AnvilNameTagItem getInstance(Item item){
        AnvilNameTagItem item1 = new AnvilNameTagItem();
        if(item.getNamedTag().contains(ITEM_TAG)){
            if(item.getNamedTag().contains(NAME_TAG)){
                item1.setName(item.getNamedTag().getString(NAME_TAG));
            }
            return item1;
        }
        return null;
    }

    public void setName(String name) {
        CompoundTag tag = this.getNamedTag();
        tag.putString(NAME_TAG,name);
        tag.putString(ITEM_TAG,this.getClass().getSimpleName());
        this.setNamedTag(tag);
        this.setLore(AnvilPlus.format("\n&r&7Items can be renamed\n&r------------\n&r&eCurrent binding name:\n\n&r"+ name));
    }
}
