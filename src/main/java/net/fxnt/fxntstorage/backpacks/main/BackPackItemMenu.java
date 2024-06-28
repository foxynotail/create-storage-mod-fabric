package net.fxnt.fxntstorage.backpacks.main;

import net.fxnt.fxntstorage.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class BackPackItemMenu extends BackPackMenu {

    public BackPackItemMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, createInventory(buf.readItem()), buf.readByte());
    }

    public BackPackItemMenu(int containerId, Inventory playerInventory, Container container, byte backPackType) {
        super(ModMenuTypes.BACK_PACK_ITEM_MENU, containerId, playerInventory, container, backPackType);
    }

    private static Container createInventory(ItemStack itemStack) {
        Container container = new BackPackContainer(itemStack);
        return container;
    }
}
