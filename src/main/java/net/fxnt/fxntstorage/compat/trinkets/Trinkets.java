package net.fxnt.fxntstorage.compat.trinkets;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.fxnt.fxntstorage.backpacks.main.BackPackItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class Trinkets {

    public static ItemStack getBackPackTrinket(Player player) {
        ItemStack backPack = ItemStack.EMPTY;
        Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent(player);
        if (optional.isPresent()) {
            TrinketComponent component = optional.get();
            if (component.isEquipped(stack -> stack.getItem() instanceof BackPackItem)) {
                backPack = component.getEquipped(stack -> stack.getItem() instanceof BackPackItem).get(0).getB();
                return backPack;
            }
        }
        return backPack;
    }

    public static boolean setBackPackTrinket(Player player, ItemStack itemStack) {

        Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent(player);
        if (optional.isPresent()) {
            TrinketComponent component = optional.get();
            TrinketInventory trinketInventory = component.getInventory().get("chest").get("back");
            if (trinketInventory.isEmpty()) {
                trinketInventory.setItem(0, itemStack);
                return true;
            }
        }
        return false;
    }


}
