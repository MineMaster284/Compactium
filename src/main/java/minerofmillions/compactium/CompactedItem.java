package minerofmillions.compactium;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class CompactedItem extends Item {
    public ResourceLocation uncompactedRegistryName;
    public Item uncompactedItem;
    public ResourceLocation compactedRegistryName;

    public CompactedItem(ResourceLocation uncompactedLocation) {
        super(new Properties());
        uncompactedItem = ForgeRegistries.ITEMS.getValue(uncompactedLocation);
        compactedRegistryName = Compactium.getCompactedResourceLocation(Objects.requireNonNull(uncompactedItem.getRegistryName()));
        setRegistryName(compactedRegistryName);
    }
}
