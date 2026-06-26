package io.github.chakyl.cozycafe;

import net.minecraftforge.common.ForgeConfigSpec;

public class CozyConfig {

    public final ForgeConfigSpec.IntValue menuSizePerStar;
    public final ForgeConfigSpec.IntValue customerSpawnInterval;
    public final ForgeConfigSpec.DoubleValue groupCustomerChance;
    public final ForgeConfigSpec.IntValue customerWaitTime;
    public final ForgeConfigSpec.IntValue customerOrderTime;

    public CozyConfig(final ForgeConfigSpec.Builder builder) {
        menuSizePerStar = builder
                .comment("How many menu items are required to open your cafe, per star + 1. Has a very large impact on difficulty!")
                .defineInRange("menu_size_per_star", 3, 1, 256);

        customerSpawnInterval = builder
                .comment("The minimum seconds it can take for a customer to spawn at max reputation. Every star of reputation adds 20% of this value. For example, if its set to 20 seconds, these are the times per star: 400 | 320 | 240 | 160 | 80 | 20")
                .defineInRange("customer_spawn_interval", 40, 4, 4096);

        groupCustomerChance = builder
                .comment("Chance an additional customer is added to the currently spawning group of customers. Repeats until a roll is failed or 4 customers are spawned")
                .defineInRange("group_customer_chance", 0.5D, 0D, 1D);

        customerWaitTime = builder
                .comment("How long a customer will wait for their order, in ticks.")
                .defineInRange("customer_wait_time", 1000, 20, Integer.MAX_VALUE);

        customerOrderTime = builder
                .comment("How long it takes a customer to put in their order, in ticks.")
                .defineInRange("customer_order_time", 200, 20, Integer.MAX_VALUE);

    }
}