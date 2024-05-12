package net.fxnt.fxntstorage.backpacks.recipes;

import com.google.gson.JsonObject;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpacks.main.BackPackItem;
import net.fxnt.fxntstorage.init.ModBlocks;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class BackPackRecipe extends ShapedRecipe {

        private ItemStack craftingStack;

    public BackPackRecipe(ResourceLocation id, String group, int width, int height, NonNullList<Ingredient> recipeItems, ItemStack result) {
        super(id, group, CraftingBookCategory.MISC, width, height, recipeItems, result);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        ItemStack[] items = inv.getItems().toArray(new ItemStack[0]);
        for (ItemStack itemStack : items) {
            if (itemStack.getItem() instanceof BackPackItem) {
                this.craftingStack = itemStack;
                break;
            }
        }

        return super.matches(inv, level);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return BackPackRecipeSerializer.SHAPED_RECIPE;
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess registryAccess) {

        ItemStack craftedStack = super.getResultItem(registryAccess);
        if (craftingStack == null) return craftedStack;
        if(craftingStack.hasTag()) {

            craftedStack.setTag(craftingStack.getTag());
            CompoundTag entityTag = craftedStack.getOrCreateTagElement("BlockEntityTag");
            if (entityTag.contains("maxStackSize")) {
                FXNTStorage.LOGGER.info("MaxStackSizeFound");
                int newMaxStackSize = entityTag.getInt("maxStackSize");
                if (craftedStack.getItem().equals(ModBlocks.ANDESITE_BACK_PACK_ITEM)) {
                    newMaxStackSize = Util.ANDESITE_BACKPACK_STACK_SIZE;
                } else if (craftedStack.getItem().equals(ModBlocks.COPPER_BACK_PACK_ITEM)) {
                    newMaxStackSize = Util.COPPER_BACKPACK_STACK_SIZE;
                } else if (craftedStack.getItem().equals(ModBlocks.BRASS_BACK_PACK_ITEM)) {
                    newMaxStackSize = Util.BRASS_BACKPACK_STACK_SIZE;
                } else if (craftedStack.getItem().equals(ModBlocks.HARDENED_BACK_PACK_ITEM)) {
                    newMaxStackSize = Util.HARDENED_BACKPACK_STACK_SIZE;
                }
                entityTag.putInt("maxStackSize", newMaxStackSize);
            }
        }
        return craftedStack;
    }

    private static BackPackRecipe fromShaped(ShapedRecipe recipe) {
        return new BackPackRecipe(recipe.getId(), recipe.getGroup(), recipe.getWidth(), recipe.getHeight(),
                recipe.getIngredients(), recipe.getResultItem(null));
    }

    public static class BackPackRecipeSerializer extends ShapedRecipe.Serializer {

        @Override
        public @NotNull BackPackRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return fromShaped(super.fromJson(recipeId, json));
        }

        @Override
        public @NotNull BackPackRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return fromShaped(super.fromNetwork(recipeId, buffer));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ShapedRecipe recipe) {
            super.toNetwork(buffer, recipe);

        }
    }

}
