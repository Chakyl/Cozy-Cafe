package io.github.chakyl.cozycafe.registry;

import com.google.common.base.Suppliers;
import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.blockentities.CafeManagerBlockEntity;
import io.github.chakyl.cozycafe.blockentities.CafeMenuBlockEntity;
import io.github.chakyl.cozycafe.blocks.CafeManagerBlock;
import io.github.chakyl.cozycafe.blocks.CafeMenuBlock;
import io.github.chakyl.cozycafe.gui.CafeManagerMenu;
import io.github.chakyl.cozycafe.gui.MenuSelectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public final class CozyRegistry {

    private static final String MODID = CozyCafe.MODID;

    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    public static void register() {
        BlockRegistry.register();
        BlockEntityRegistry.register();
        MenuRegistry.register();
        ItemRegistry.register();
        CreativeTabReg.register();
    }

    public static final class BlockRegistry {

        private static void register() {
            BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        }

        public static final RegistryObject<Block> CAFE_MANAGER = registerWithItem("cafe_manager", () -> new CafeManagerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).noOcclusion().strength(1.5F, 6.0F)));
        public static final RegistryObject<Block> CAFE_MENU = registerWithItem("cafe_menu", () -> new CafeMenuBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).noOcclusion().strength(1.5F, 6.0F)));


        private static RegistryObject<Block> registerWithItem(final String name, final Supplier<Block> supplier) {
            return registerWithItem(name, supplier, ItemRegistry::registerBlockItem);
        }

        private static RegistryObject<Block> registerWithItem(final String name, final Supplier<Block> blockSupplier, final Function<RegistryObject<Block>, RegistryObject<Item>> itemSupplier) {
            final RegistryObject<Block> block = BLOCKS.register(name, blockSupplier);
            final RegistryObject<Item> item = itemSupplier.apply(block);
            return block;
        }

        private static boolean never(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
            return false;
        }
    }

    public static final class BlockEntityRegistry {
        private static void register() {
            BLOCK_ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        }

        public static final RegistryObject<BlockEntityType<CafeManagerBlockEntity>> CAFE_MANAGER = BLOCK_ENTITY_TYPES.register("cafe_manager", () -> BlockEntityType.Builder.of(CafeManagerBlockEntity::new, BlockRegistry.CAFE_MANAGER.get()).build(null));
        public static final RegistryObject<BlockEntityType<CafeMenuBlockEntity>> CAFE_MENU = BLOCK_ENTITY_TYPES.register("cafe_menu", () -> BlockEntityType.Builder.of(CafeMenuBlockEntity::new, BlockRegistry.CAFE_MENU.get()).build(null));
    }

    public static final class ItemRegistry {

        private static final List<RegistryObject<Item>> ALL_ITEMS = new ArrayList<>();

        private static void register() {
            ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        }
//
//        public static final RegistryObject<Item> GATCHA_CAPSULE = register("gatcha_capsule", () -> new GatchaCapsuleItem(new Item.Properties().rarity(Rarity.RARE).stacksTo(64)));


        /**
         * Creates a registry object for a block item and adds it to the mod creative tab
         *
         * @param block the block
         * @return the registry object
         */
        private static RegistryObject<Item> registerBlockItem(final RegistryObject<Block> block) {
            return register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
        }

        /**
         * Creates a registry object for the given item and adds it to the mod creative tab
         *
         * @param name     the registry name
         * @param supplier the item supplier
         * @return the item registry object
         */
        private static RegistryObject<Item> register(final String name, final Supplier<Item> supplier) {
            final RegistryObject<Item> item = ITEMS.register(name, supplier);
            ALL_ITEMS.add(item);
            return item;
        }
    }

    public static final class MenuRegistry {

        private static void register() {
            MENU_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        }
        public static final RegistryObject<MenuType<CafeManagerMenu>> CAFE_MANAGER = MENU_TYPES.register("cafe_manager", () -> IForgeMenuType.create(CafeManagerMenu::new));
        public static final RegistryObject<MenuType<MenuSelectorMenu>> MENU_SELECTOR = MENU_TYPES.register("menu_selector", () -> IForgeMenuType.create(MenuSelectorMenu::new));
    }
    public static final class CreativeTabReg {

        private static void register() {
            CREATIVE_MODE_TABS.register(FMLJavaModLoadingContext.get().getModEventBus());
        }

        public static final RegistryObject<CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("tab", () ->
                CreativeModeTab.builder()
                        .icon(Suppliers.memoize(() -> new ItemStack(BlockRegistry.CAFE_MANAGER.get())))
                        .title(Component.translatable("itemGroup." + CozyCafe.MODID))
                        .withSearchBar()
                        .displayItems((parameters, output) ->
                                output.acceptAll(ItemRegistry.ALL_ITEMS
                                        .stream()
                                        .map(o -> new ItemStack(o.get()))
                                        .toList())
                        )
                        .build()
        );
    }

}