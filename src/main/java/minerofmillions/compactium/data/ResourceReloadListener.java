package minerofmillions.compactium.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import minerofmillions.compactium.CompactedItem;
import minerofmillions.compactium.Compactium;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.data.LanguageMetadataSection;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourcePack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResourceReloadListener implements ISelectiveResourceReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static RecipeManager recipeManager;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(this);
        ResourceReloadListener.recipeManager = event.getDataPackRegistries().getRecipeManager();
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        reloadRecipes();
        if (resourcePredicate.test(VanillaResourceType.LANGUAGES)) {
            Map<String, Language> languageMap = getLanguageMap(resourceManager.getResourcePackStream());
            Language language = languageMap.getOrDefault("en_us", new Language("en_us", "US", "English", false));

        }
    }

    private void reloadRecipes() {
        Collection<IRecipe<?>> recipeList = recipeManager.getRecipes();
        recipeList.addAll(Compactium.compactedItems.stream().map(this::makeCompactingRecipe).collect(Collectors.toSet()));
        recipeList.addAll(Compactium.compactedItems.stream().map(this::makeUncompactingRecipe).collect(Collectors.toSet()));
    }

    private IRecipe<?> makeCompactingRecipe(CompactedItem compactedItem) {
        Ingredient uncompactedIngredient = Ingredient.fromStacks(new ItemStack(compactedItem.uncompactedItem));
        return new ShapedRecipe(
                getRecipeLocation("compacting/", compactedItem.uncompactedRegistryName),
                "",
                3,
                3,
                NonNullList.from(Ingredient.EMPTY,
                        uncompactedIngredient, uncompactedIngredient, uncompactedIngredient,
                        uncompactedIngredient, uncompactedIngredient, uncompactedIngredient,
                        uncompactedIngredient, uncompactedIngredient, uncompactedIngredient
                ),
                new ItemStack(compactedItem)
        );
    }

    private IRecipe<?> makeUncompactingRecipe(CompactedItem compactedItem) {
        Ingredient compactedIngredient = Ingredient.fromStacks(new ItemStack(compactedItem));
        return new ShapelessRecipe(
                getRecipeLocation("uncompacting/", compactedItem.compactedRegistryName),
                "",
                new ItemStack(compactedItem.uncompactedItem, 9),
                NonNullList.from(Ingredient.EMPTY, compactedIngredient)
        );
    }

    private ResourceLocation getRecipeLocation(String seed, ResourceLocation uncompactedLocation) {
        if (uncompactedLocation.getNamespace().equals(Compactium.ID)) {
            seed += uncompactedLocation.getPath();
        } else {
            seed += uncompactedLocation.getNamespace() + "_" + uncompactedLocation.getPath();
        }
        return new ResourceLocation(Compactium.ID, seed);
    }


    private static Map<String, Language> getLanguageMap(Stream<IResourcePack> resourcePackStream) {
        Map<String, Language> map = Maps.newHashMap();
        resourcePackStream.forEach((p_239505_1_) -> {
            try {
                LanguageMetadataSection languagemetadatasection = p_239505_1_.getMetadata(LanguageMetadataSection.field_195818_a);
                if (languagemetadatasection != null) {
                    for (Language language : languagemetadatasection.getLanguages()) {
                        map.putIfAbsent(language.getCode(), language);
                    }
                }
            } catch (IOException | RuntimeException runtimeexception) {
                LOGGER.warn("Unable to parse language metadata section of resourcepack: {}", p_239505_1_.getName(), runtimeexception);
            }
        });
        return ImmutableMap.copyOf(map);
    }
}
