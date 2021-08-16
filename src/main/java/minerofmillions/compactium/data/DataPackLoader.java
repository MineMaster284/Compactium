package minerofmillions.compactium.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DataPackLoader {
    private static final String DATAPACK_NAME = "compactium:internals";
    public static final DataPackLoader INSTANCE = new DataPackLoader();
    private static final Gson GSON = new Gson();

    private static class MemoryDataPack implements IResourcePack {
        private static final JsonObject meta = new JsonObject();

        static {
            meta.add("pack_format", new JsonPrimitive(4));
            meta.add("description", new JsonPrimitive("Data for compactium."));
        }

        private final HashMap<ResourceLocation, Supplier<? extends InputStream>> assets = new HashMap<>();
        private final HashMap<ResourceLocation, Supplier<? extends InputStream>> data = new HashMap<>();

        private HashMap<ResourceLocation, Supplier<? extends InputStream>> getResourcePackTypeMap(ResourcePackType type) {
            if (type.equals(ResourcePackType.CLIENT_RESOURCES)) return assets;
            else if (type.equals(ResourcePackType.SERVER_DATA)) return data;
            else return null;
        }

        public void putJson(ResourcePackType type, ResourceLocation location, JsonElement json) {
            HashMap<ResourceLocation, Supplier<? extends InputStream>> map = getResourcePackTypeMap(type);
            if (map != null) {
                map.put(location, () -> new ByteArrayInputStream(GSON.toJson(json).getBytes(StandardCharsets.UTF_8)));
            }
        }

        @Override
        public InputStream getRootResourceStream(String fileName) throws IOException {
            if (fileName.contains("/") || fileName.contains("\\"))
                throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
            throw new FileNotFoundException(fileName);
        }

        @Override
        public InputStream getResourceStream(ResourcePackType type, ResourceLocation location) throws IOException {
            Map<ResourceLocation, Supplier<? extends InputStream>> map = getResourcePackTypeMap(type);
            if (map != null && map.containsKey(location)) {
                return map.get(location).get();
            }
            throw new FileNotFoundException(location.toString());
        }

        @Override
        public Collection<ResourceLocation> getAllResourceLocations(ResourcePackType type, String namespaceIn, String pathIn, int maxDepthIn, Predicate<String> filterIn) {
            Map<ResourceLocation, Supplier<? extends InputStream>> map = getResourcePackTypeMap(type);
            if (map == null) return Collections.emptyList();

            return map.keySet().stream()
                    .filter(location -> location.getNamespace().equals(namespaceIn))
                    .filter(location -> location.getPath().split("/").length < maxDepthIn)
                    .filter(location -> location.getPath().startsWith(pathIn))
                    .filter(location -> filterIn.test(location.getPath().substring(Math.max(location.getPath().lastIndexOf('/'), 0))))
                    .collect(Collectors.toSet());
        }

        @Override
        public boolean resourceExists(ResourcePackType type, ResourceLocation location) {
            Map<ResourceLocation, Supplier<? extends InputStream>> map = getResourcePackTypeMap(type);
            return map != null && map.containsKey(location);
        }

        @Override
        public Set<String> getResourceNamespaces(ResourcePackType type) {
            Map<ResourceLocation, Supplier<? extends InputStream>> map = getResourcePackTypeMap(type);
            if (map == null) return Collections.emptySet();
            return map.keySet().stream().map(ResourceLocation::getNamespace).collect(Collectors.toSet());
        }

        @Nullable
        @Override
        public <T> T getMetadata(IMetadataSectionSerializer<T> deserializer) throws IOException {
            return deserializer.deserialize(meta);
        }

        @Override
        public String getName() {
            return DATAPACK_NAME;
        }

        @Override
        public void close() {

        }

        @Override
        public boolean isHidden() {
            return true;
        }
    }
}
