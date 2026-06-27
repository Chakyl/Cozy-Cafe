package io.github.chakyl.cozycafe;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

public class CozyConfig {

    public final ForgeConfigSpec.IntValue menuSizePerStar;
    public final ForgeConfigSpec.IntValue customerSpawnInterval;
    public final ForgeConfigSpec.DoubleValue groupCustomerChance;
    public final ForgeConfigSpec.IntValue customerWaitTime;
    public final ForgeConfigSpec.IntValue customerOrderTime;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> customerUsernames;

    public CozyConfig(final ForgeConfigSpec.Builder builder) {
        menuSizePerStar = builder
                .comment("How many menu items are required to open your cafe, per star + 1. Has a very large impact on difficulty!")
                .defineInRange("menu_size_per_star", 3, 1, 256);

        customerSpawnInterval = builder
                .comment("The minimum seconds it can take for a customer to spawn at max reputation. Every star of reputation adds 20% of this value. For example, if its set to 20 seconds, these are the times per star: 400 | 320 | 240 | 160 | 80 | 20,")
                .defineInRange("customer_spawn_interval", 40, 4, 4096);

        groupCustomerChance = builder
                .comment("Chance an additional customer is added to the currently spawning group of customers. Repeats until a roll is failed or 4 customers are spawned,")
                .defineInRange("group_customer_chance", 0.5D, 0D, 1D);

        customerWaitTime = builder
                .comment("How long a customer will wait for their order, in ticks.")
                .defineInRange("customer_wait_time", 1000, 20, Integer.MAX_VALUE);

        customerOrderTime = builder
                .comment("How long it takes a customer to put in their order, in ticks.")
                .defineInRange("customer_order_time", 200, 20, Integer.MAX_VALUE);

        customerUsernames = builder
                .comment("List of Minecraft usernames whose skins will be used for spawning customers.")
                .defineList("customer_usernames", Arrays.asList("Chakyl", "Nitbe", "MHF_Steve", "MHF_Alex", "MHF_Herobrine", "MHF_Chicken", "MHF_Cow", "MHF_Pig", "MHF_Sheep", "MHF_Squid", "MHF_Villager", "MHF_Ocelot", "MHF_Blaze", "MHF_CaveSpider", "MHF_Creeper", "MHF_Enderman", "MHF_Ghast", "MHF_Golem", "MHF_LavaSlime", "MHF_PigZombie", "MHF_Skeleton", "MHF_Slime", "MHF_Spider", "MHF_Witch", "MHF_Zombie", "MHF_Cake", "MHF_Chest", "MHF_CoconutB", "MHF_CoconutG", "MHF_Melon", "MHF_OakLog", "MHF_Present1", "MHF_Present2", "MHF_Pumpkin", "MHF_TNT", "MHF_TNT2", "MHF_ArrowUp", "MHF_ArrowDown", "MHF_ArrowLeft", "MHF_ArrowRight", "MHF_Exclamation", "MHF_Question"), obj -> obj instanceof String);

    }
}