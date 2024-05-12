package net.fxnt.fxntstorage.init;

import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpacks.recipes.BackPackRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class ModRecipes {

    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                new ResourceLocation(FXNTStorage.MOD_ID, "backpack_crafting"),
                new BackPackRecipe.BackPackRecipeSerializer());
    }

}
