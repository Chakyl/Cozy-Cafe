package io.github.chakyl.cozycafe;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

public class CozyConfig {

    public final ForgeConfigSpec.IntValue menuSizePerStar;
    public final ForgeConfigSpec.IntValue customerSpawnInterval;
    public final ForgeConfigSpec.DoubleValue groupCustomerChance;
    public final ForgeConfigSpec.DoubleValue dessertChance;
    public final ForgeConfigSpec.IntValue customerWaitTime;
    public final ForgeConfigSpec.IntValue customerOrderTime;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> customerUsernames;

    public final ForgeConfigSpec.ConfigValue<String> currency_1;
    public final ForgeConfigSpec.ConfigValue<String> currency_8;
    public final ForgeConfigSpec.ConfigValue<String> currency_16;
    public final ForgeConfigSpec.ConfigValue<String> currency_64;
    public final ForgeConfigSpec.ConfigValue<String> currency_512;
    public final ForgeConfigSpec.ConfigValue<String> currency_4096;

    public final ForgeConfigSpec.BooleanValue numismaticsUtilsPayment;
    public final ForgeConfigSpec.BooleanValue dailyLimit;
    public final ForgeConfigSpec.BooleanValue platingRequired;
    public final ForgeConfigSpec.ConfigValue<String> quality_bonus_stage;
    public final ForgeConfigSpec.ConfigValue<String> pickle_bonus_stage;

    public CozyConfig(final ForgeConfigSpec.Builder builder) {
        menuSizePerStar = builder
                .comment("How many menu items are required to open your cafe, per star + 1. Has a very large impact on difficulty!")
                .defineInRange("menu_size_per_star", 3, 0, 5);

        // Customers and wait times

        customerSpawnInterval = builder
                .comment("The minimum seconds it can take for a customer to spawn at max reputation. Every star of reputation adds 20% of this value. For example, if its set to 20 seconds, these are the times per star: 400 | 320 | 240 | 160 | 80 | 20,")
                .defineInRange("customer_spawn_interval", 40, 4, 4096);

        groupCustomerChance = builder
                .comment("Chance an additional customer is added to the currently spawning group of customers. Repeats until a roll is failed or 4 customers are spawned/")
                .defineInRange("group_customer_chance", 0.5D, 0D, 1D);

        dessertChance = builder
                .comment("Chance a customer orders a dessert, if the menu has any.")
                .defineInRange("dessert_chance", 0.40D, 0D, 1D);

        customerWaitTime = builder
                .comment("How long a customer will wait for their order, in ticks.")
                .defineInRange("customer_wait_time", 1000, 20, Integer.MAX_VALUE);

        customerOrderTime = builder
                .comment("How long it takes a customer to put in their order, in ticks.")
                .defineInRange("customer_order_time", 200, 20, Integer.MAX_VALUE);

        customerUsernames = builder
                .comment("List of Minecraft usernames whose skins will be used for spawning customers.")
                .defineList("customer_usernames", Arrays.asList("Chakyl", "Nitbe", "MHF_Steve", "MHF_Alex", "MHF_Herobrine", "MHF_Chicken", "MHF_Cow", "MHF_Pig", "MHF_Sheep", "MHF_Squid", "MHF_Villager", "MHF_Ocelot", "MHF_Blaze", "MHF_CaveSpider", "MHF_Creeper", "MHF_Enderman", "MHF_Ghast", "MHF_Golem", "MHF_LavaSlime", "MHF_PigZombie", "MHF_Skeleton", "MHF_Slime", "MHF_Spider", "MHF_Witch", "MHF_Zombie", "MHF_Cake", "MHF_Chest", "MHF_CoconutB", "MHF_CoconutG", "MHF_Melon", "MHF_OakLog", "MHF_Present1", "MHF_Present2", "MHF_Pumpkin", "MHF_TNT", "MHF_TNT2", "MHF_ArrowUp", "MHF_ArrowDown", "MHF_ArrowLeft", "MHF_ArrowRight", "MHF_Exclamation", "MHF_Question"), obj -> obj instanceof String);

        // Currency
        currency_1 = builder.comment("The item to use as a 1 denomination of currency (e.g.: minecraft:emerald)").define("currency_1", "minecraft:gold_nugget");
        currency_8 = builder.comment("The item to use as a 8 denomination of currency (e.g.: minecraft:emerald)").define("currency_8", "minecraft:gold_ingot");
        currency_16 = builder.comment("The item to use as a 16 denomination of currency (e.g.: minecraft:emerald)").define("currency_16", "minecraft:emerald");
        currency_64 = builder.comment("The item to use as a 64 denomination of currency (e.g.: minecraft:diamond)").define("currency_64", "");
        currency_512 = builder.comment("The item to use as a 512 denomination of currency (e.g.: minecraft:emerald)").define("currency_512", "minecraft:diamond");
        currency_4096 = builder.comment("The item to use as a 4096 denomination of currency (e.g.: minecraft:emerald)").define("currency_4096", "");

        numismaticsUtilsPayment = builder.comment("Allows customers to pay to player's bank account directly if Create Numismatics Utils is installed. Set to false if you don't want that to happen.").define("numismatics_utils_payment", true);
        dailyLimit = builder.comment("When enabled, limits cafe opening to once per Minecraft day.").define("daily_limit", true);
        platingRequired = builder.comment("When enabled, main dishes that aren't in bowls will need to be plated using a Plating Station before serving.").define("plating_required", true);
        // Stage Config, mainly for SSV
        quality_bonus_stage = builder
                .comment("Requires KubeJs and Quality Food. The stage name with the following impact: Quality impacts of non-fish prices are doubled. Can be added to the player using the command e.g, /kubejs stages add playerName the_quality_of_the_earth")
                .define("quality_bonus_stage", "the_quality_of_the_earth");
        pickle_bonus_stage = builder
                .comment("Requires KubeJs. The stage name with the following impact: Items with the tag cozy_cafe:pickles are worth 100% more. Can be added to the player using the command e.g, /kubejs stages add playerName brine_and_punishment")
                .define("pickle_bonus_stage", "brine_and_punishment");
    }
}