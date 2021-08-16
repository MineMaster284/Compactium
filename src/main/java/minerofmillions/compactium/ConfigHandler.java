package minerofmillions.compactium;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigHandler {
    public static class Common {
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> compactedResources;
        public final ForgeConfigSpec.IntValue maxCompactingDepth;

        public Common(ForgeConfigSpec.Builder builder) {
            compactedResources = builder
                    .comment("List of compacted resources", "Default: [minecraft:cobblestone, minecraft:dirt]")
                    .define("compactedResources", defaultCompactedResources);

            maxCompactingDepth = builder
                    .comment("Maximum compacted depth", "Number of resources = 9 ^ this value")
                    .defineInRange("maxCompactingDepth", 3, 1, Integer.MAX_VALUE);
        }

        private static List<String> defaultCompactedResources = new ArrayList<>();

        static {
            defaultCompactedResources.add("minecraft:cobblestone");
            defaultCompactedResources.add("minecraft:dirt");
        }
    }

    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    public static Set<ResourceLocation> compactedResources;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = specPair.getKey();
        COMMON_SPEC = specPair.getValue();

        compactedResources = COMMON.compactedResources.get().stream().map(ResourceLocation::new).collect(Collectors.toSet());
    }
}
