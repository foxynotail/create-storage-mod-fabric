package net.fxnt.fxntstorage;

import com.simibubi.create.Create;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import io.github.fabricators_of_create.porting_lib.util.EnvExecutor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fxnt.fxntstorage.backpacks.upgrades.BackPackOnBackUpgradeHandler;
import net.fxnt.fxntstorage.config.Config;
import net.fxnt.fxntstorage.init.*;
import net.fxnt.fxntstorage.network.BackPackPackets;
import net.fxnt.fxntstorage.network.InventoryPackets;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FXNTStorage implements ModInitializer {
    public static final String MOD_ID = "fxntstorage";
    public static final String NAME = "FXNT Storage";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {

        ForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.COMMON, Config.COMMON_CONFIG);
        Config.loadConfig(Config.COMMON_CONFIG, FabricLoader.getInstance().getConfigDir().resolve("fxntstorage-common.toml"));

        ModGroups.register();
        ModItems.register();
        ModBlocks.register();
        ModRecipes.register();
        InventoryPackets.register();
        BackPackPackets.register();
        ModMenuTypes.register();

        LOGGER.info("Create addon mod [{}] is loading alongside Create [{}]!", NAME, Create.VERSION);
        LOGGER.info(EnvExecutor.unsafeRunForDist(
                () -> () -> "{} is accessing Porting Lib from the client!",
                () -> () -> "{} is accessinsg Porting Lib from the server!"
        ), NAME);

        // Click Event
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (player.getInventory().getSelected().is(ItemTags.TOOLS)) {
                new BackPackOnBackUpgradeHandler(player).fromAttackBlockEvent(player, world, hand, pos);
            }
            return InteractionResult.PASS;
        });
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof LivingEntity livingEntity) {
                new BackPackOnBackUpgradeHandler(player).fromAttackEntityEvent(player, world, hand, livingEntity);
            }
            return InteractionResult.PASS;
        });

    }
}
