package io.github.chakyl.cozycafe;

import com.google.common.base.Suppliers;
import io.github.chakyl.cozycafe.blockentities.CafeManagerBlockEntity;
import io.github.chakyl.cozycafe.blockentities.CafeMenuBlockEntity;
import io.github.chakyl.cozycafe.blockentities.CafeSignBlockEntity;
import io.github.chakyl.cozycafe.blockentities.PlatingStationBlockEntity;
import io.github.chakyl.cozycafe.blocks.CafeManagerBlock;
import io.github.chakyl.cozycafe.blocks.CafeMenuBlock;
import io.github.chakyl.cozycafe.blocks.CafeSignBlock;
import io.github.chakyl.cozycafe.blocks.PlatingStationBlock;
import io.github.chakyl.cozycafe.entities.CustomerEntity;
import io.github.chakyl.cozycafe.gui.CafeManagerMenu;
import io.github.chakyl.cozycafe.gui.CafeStatsMenu;
import io.github.chakyl.cozycafe.gui.CafeStatsScreen;
import io.github.chakyl.cozycafe.gui.MenuSelectorMenu;
import io.github.chakyl.cozycafe.item.CafeSignItem;
import io.github.chakyl.cozycafe.item.DirtyServingPlateItem;
import io.github.chakyl.cozycafe.item.ServingPlateItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.chakyl.cozycafe.CozyCafe.MODID;

@SuppressWarnings("unused")
public final class CozyRegistry {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MODID);
    private static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CozyCafe.MODID);

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
//        ParticleRegistry.register();
        ITEMS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        DATA_COMPONENT_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);


        BlockRegistry.init();
        BlockEntityRegistry.init();
        EntityRegistry.init();
        ItemRegistry.init();
        MenuRegistry.init();
        DataComponentsRegistry.init();
        CreativeTabReg.init();
    }

    public static final class BlockRegistry {
        public static void init() {}

        public static final DeferredHolder<Block, Block> CAFE_MANAGER = registerWithItem("cafe_manager", () ->  new CafeManagerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).noOcclusion().strength(1.5F, 6.0F)));
        public static final DeferredHolder<Block, Block> CAFE_SIGN = registerWithItem("cafe_sign", () ->  new CafeSignBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).noOcclusion().strength(1.5F, 6.0F)), (blockObj) -> ItemRegistry.register("cafe_sign", () -> new CafeSignItem(blockObj.get(), new Item.Properties())));
        public static final DeferredHolder<Block, Block> CAFE_MENU = registerWithItem("cafe_menu", () ->  new CafeMenuBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).noOcclusion().strength(1.5F, 6.0F)));
        public static final DeferredHolder<Block, Block> PLATING_STATION = registerWithItem("plating_station", () ->  new PlatingStationBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).noOcclusion().strength(1.5F, 6.0F)));


        private static DeferredHolder<Block, Block> registerWithItem(final String name, final Supplier<Block> supplier) {
            return registerWithItem(name, supplier, ItemRegistry::registerBlockItem);
        }

        private static DeferredHolder<Block, Block> registerWithItem(final String name, final Supplier<Block> blockSupplier, final Function<DeferredHolder<Block, Block>, DeferredHolder<Item, Item>> itemSupplier) {
            final DeferredHolder<Block, Block> block = BLOCKS.register(name, blockSupplier);
            final DeferredHolder<Item, Item> item = itemSupplier.apply(block);
            return block;
        }

        private static boolean never(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
            return false;
        }
    }

    public static final class BlockEntityRegistry {
        public static void init() {}

        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CafeManagerBlockEntity>> CAFE_MANAGER = BLOCK_ENTITY_TYPES.register("cafe_manager",
                () -> BlockEntityType.Builder.of(CafeManagerBlockEntity::new, BlockRegistry.CAFE_MANAGER.get()).build(null));
        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CafeSignBlockEntity>> CAFE_SIGN = BLOCK_ENTITY_TYPES.register("cafe_sign",
                () -> BlockEntityType.Builder.of(CafeSignBlockEntity::new, BlockRegistry.CAFE_SIGN.get()).build(null));
        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CafeMenuBlockEntity>> CAFE_MENU = BLOCK_ENTITY_TYPES.register("cafe_menu",
                () -> BlockEntityType.Builder.of(CafeMenuBlockEntity::new, BlockRegistry.CAFE_MENU.get()).build(null));
        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlatingStationBlockEntity>> PLATING_STATION = BLOCK_ENTITY_TYPES.register("plating_station",
                () -> BlockEntityType.Builder.of(PlatingStationBlockEntity::new, BlockRegistry.PLATING_STATION.get()).build(null));
    }

    public static final class EntityRegistry {
        public static void init() {}

        public static final DeferredHolder<EntityType<?>, EntityType<CustomerEntity>> CUSTOMER = ENTITY_TYPES.register("customer", () -> EntityType.Builder.of(CustomerEntity::new, MobCategory.CREATURE).sized(0.6F, 1.8F).build("customer"));
    }

    public static final class ParticleRegistry {
        public static void init() {}
        // TODO: 1.1, add particles when a customer is served for payment
//        public static final RegistryObject<SimpleParticleType> PAYMENT = PARTICLE_TYPES.register("payment", () -> new SimpleParticleType(false));
    }

    public static final class ItemRegistry {
        public static void init() {}

        public static final DeferredHolder<Item, Item> SERVING_PLATE = register("serving_plate", () -> new ServingPlateItem(new Item.Properties().stacksTo(8)));
        public static final DeferredHolder<Item, Item> DIRTY_SERVING_PLATE = register("dirty_serving_plate", () -> new DirtyServingPlateItem(new Item.Properties().stacksTo(8)));
        public static final DeferredHolder<Item, Item> CAFE_CATALOG = register("cafe_catalog", () -> new Item(new Item.Properties().stacksTo(1)));

        /**
         * Creates a registry object for a block item and adds it to the mod creative tab
         *
         * @param block the block
         * @return the registry object
         */
        private static DeferredHolder<Item, Item> registerBlockItem(final DeferredHolder<Block, Block> block) {
            return register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
        }

        /**
         * Creates a registry object for the given item and adds it to the mod creative tab
         *
         * @param name     the registry name
         * @param supplier the item supplier
         * @return the item registry object
         */
        private static DeferredHolder<Item, Item> register(final String name, final Supplier<Item> supplier) {
            final DeferredHolder<Item, Item> item = ITEMS.register(name, supplier);
            return item;
        }
    }

    public static final class MenuRegistry {
        public static void init() {}

        public static final DeferredHolder<MenuType<?>, MenuType<CafeManagerMenu>> CAFE_MANAGER = MENU_TYPES.register("cafe_manager", () -> IMenuTypeExtension.create(CafeManagerMenu::new));
        public static final DeferredHolder<MenuType<?>, MenuType<MenuSelectorMenu>>  MENU_SELECTOR = MENU_TYPES.register("menu_selector", () -> IMenuTypeExtension.create(MenuSelectorMenu::new));
        public static final DeferredHolder<MenuType<?>, MenuType<CafeStatsMenu>>  CAFE_STATS = MENU_TYPES.register("cafe_stats", () -> IMenuTypeExtension.create(CafeStatsMenu::new));
    }

    public static final class DataComponentsRegistry {
        public static void init() {}

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> LINKED_MANAGER = DATA_COMPONENT_TYPES.registerComponentType("linked_manager", builder-> builder.persistent(BlockPos.CODEC));
        public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> PLATED_FOOD = DATA_COMPONENT_TYPES.registerComponentType("plated_food", builder -> builder.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC));
        public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> CAFE_DATA = DATA_COMPONENT_TYPES.registerComponentType("cafe_data", builder-> builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG));
    }

    public static final class CreativeTabReg {
        public static void init() {}


        public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("tab", () -> CreativeModeTab.builder()
                .icon(Suppliers.memoize(() -> new ItemStack(BlockRegistry.CAFE_MENU.get())))
                .title(Component.translatable("itemGroup." + CozyCafe.MODID))
                .withSearchBar()
                .displayItems((parameters, output) -> {
                    // It kept having items before blocks no matter what regi order it was and that annoyed me
                    output.accept(BlockRegistry.CAFE_MANAGER.get());
                    output.accept(BlockRegistry.CAFE_SIGN.get());
                    output.accept(BlockRegistry.CAFE_MENU.get());
                    output.accept(BlockRegistry.PLATING_STATION.get());
                    output.accept(ItemRegistry.SERVING_PLATE.get());
                    output.accept(ItemRegistry.DIRTY_SERVING_PLATE.get());
                    output.accept(ItemRegistry.CAFE_CATALOG.get());
                })
                .build()
        );
    }


}