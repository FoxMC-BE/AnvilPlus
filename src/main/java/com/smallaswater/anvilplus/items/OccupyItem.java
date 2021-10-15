package com.smallaswater.anvilplus.items;

import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import com.smallaswater.anvilplus.AnvilPlus;

/**
 * @author SmallasWater
 * Create on 2021/1/7 11:26
 * Package com.smallaswater.anvilplus.utils
 */
public class OccupyItem extends Item {

    public OccupyItem() {
        super(101);
        CompoundTag tag = new CompoundTag();
        tag.putString("tag_name","OccupyItem");
        this.setCompoundTag(tag);
        this.setCustomName(AnvilPlus.format("&r&cNo items can be placed here"));
    }


}
