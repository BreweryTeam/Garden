package dev.jsinco.brewery.garden.constants;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.jsinco.brewery.garden.BreweryGarden;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.FoodProperties;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

import static dev.jsinco.brewery.garden.utility.PlayerSkinUtil.fromHashCode;


@Getter
public final class PlantType extends GenericPlantType {

    public static final PlantType BERRY = new PlantType(
            "<dark_purple>Berry", "berry",
            fromHashCode("1e4883a1e22c324e753151e2ac424c74f1cc646eec8ea0db3420f1dd1d8b")
    );
    public static final PlantType STRAWBERRY = new PlantType(
            "<red>Strawberry", "strawberry",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWM5YTgzMTVmZGMzMDdiODMxNTkxOGVlYWU3ZGQ4NDI2OTEwNGIzZDliZDc3OWZjMmJhNzc5NTE1YjgwMjE0ZCJ9fX0="
            //fromHashCode("cbc826aaafb8dbf67881e68944414f13985064a3f8f044d8edfb4443e76ba")
    );
    public static final PlantType LEMON = new PlantType(
            "<yellow>Lemon", "lemon",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM3OGI1ODJkMTljY2M1NWIwMjNlYjgyZWRhMjcxYmFjNDc0NGZhMjAwNmNmNWUxOTAyNDZlMmI0ZDVkIn19fQ=="
            //fromHashCode("957fd56ca15978779324df519354b6639a8d9bc1192c7c3de925a329baef6c")
    );
    public static final PlantType LIME = new PlantType(
            "<green>Lime", "lime",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2YyNGI3MTM1Nzg5ZmU3OTlkZjM0NTk0ZDY4MDVmNTExMmJlZTYyMzI2MDViYTZkZTIxNTE4NmFkOTQifX19"
            //fromHashCode("5a5153479d9f146a5ee3c9e218f5e7e84c4fa375e4f86d31772ba71f6468")
    );
    public static final PlantType ORANGE = new PlantType(
            "<#FFA500>Orange", "orange",
            fromHashCode("65b1db547d1b7956d4511accb1533e21756d7cbc38eb64355a2626412212")
    );
    public static final PlantType GRAPE = new PlantType(
            "<#6f2da8>Grape", "grape",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODRjZWRiOTY4NjQ1ZTI0M2U1YWRhZWIxODY4Njk2YTY3M2I5MDdhNjc4NDE4ODA3MWI2M2QxZmE5Y2Q3YjUifX19"
            //fromHashCode("6ee97649bd999955413fcbf0b269c91be4342b10d0755bad7a17e95fcefdab0")
    );
    public static final PlantType APPLE = new PlantType(
            "<dark_red>Apple", "apple",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2JiMzExZjNiYTFjMDdjM2QxMTQ3Y2QyMTBkODFmZTExZmQ4YWU5ZTNkYjIxMmEwZmE3NDg5NDZjMzYzMyJ9fX0="
            //fromHashCode("cbb311f3ba1c07c3d1147cd210d81fe11fd8ae9e3db212a0fa748946c3633")
    );
    public static final PlantType PEACH = new PlantType(
            "<#FCCCC4>Peach", "peach",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmFkYmJhYjM4ODFhYWNiYTU3N2UyN2JiZWUxZmJlNGI5YTUwZTE5ZjVhODdmOGQ0OWI2MzYwNTRmYTE3ODhmYyJ9fX0="
    );
    public static final PlantType CRANBERRY = new PlantType(
            "<#791826>Cranberry", "cranberry",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWI0N2UwNjc3MzY3NzgwYjc2NTNkY2ViZjhjZjg4YmViNGRhYzk0Yzk4ZTY0NDYzNzVjYjVlYzhlOWEzOGRiNCJ9fX0="
    );
    public static final PlantType BLUEBERRY = new PlantType(
            "<#4f86f7>Blueberry", "blueberry",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDI2OTdmM2VmOGY0NjI5YjY0NWZkMmU2NDQ2NDEzMjRhMWMxMTgzNTQ5OGU2MzhmNzU3ZjI3OGFmYmNlNWRiMSJ9fX0="
    );
    public static final PlantType CHERRY = new PlantType(
            "<#461a27>Cherry", "cherry",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGVlOTdhMDhhZDA1NGI4MDY4NGU3NmYxMzI5ZGRkMGIxZmEyNzNiMDY5OWVlODZiMjEzNzk3MDRmNzQ2OGNhIn19fQ=="
            //"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGI5YjIzODNiYWU3Yjg0ZmRjMzFiNTQxNzlhZmI3MTNhMWMxODdiODNlN2EwYzVlMzg0NzBhZTJhM2UyYTMwZiJ9fX0="
    );

    // Forever constant UUID so that all plant ItemStacks are stackable. AKA. Don't change me!
    private static final UUID CONSTANT_UUID = UUID.fromString("f714a407-f7c9-425c-958d-c9914aeac05c");
    private static final NamespacedKey PERSISTENT_DATA_KEY = new NamespacedKey("brewerygarden", "plant");
    private static final NamespacedKey TBP_TAG = new NamespacedKey("brewery", "tag");
    private static final NamespacedKey TBP_SCORE = new NamespacedKey("brewery", "score");
    private static final NamespacedKey TBP_DISPLAY_NAME = new NamespacedKey("brewery", "display_name");
    private static final Random RANDOM = new Random();
    private final String key;

    private final String base64;
    private final String serializedDisplayName;

    private PlantType(String name, String key, String skin) {
        super(MiniMessage.miniMessage().deserialize("<!i>" + name));
        this.serializedDisplayName = name;
        this.base64 = skin;
        this.key = key;
    }

    public void setSkullTexture(Block block) { // Think you can do this through the DataComponent API, but I'm in a rush to test this.
        Skull skull = (Skull) block.getState();
        skull.setPlayerProfile(this.getPlayerProfile());
        skull.update();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public ItemStack getItemStack(int amount) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, amount);
        item.setData(DataComponentTypes.CUSTOM_NAME, name);
        item.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(this.getPlayerProfile()));
        item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable().hasConsumeParticles(false).build());
        item.setData(DataComponentTypes.FOOD, FoodProperties.food().nutrition(3).saturation(2.0f).build());

        // TODO: Ask in Paper discord how to use PDC with new ItemMeta API
        ItemMeta meta = item.getItemMeta();
        meta.lore(List.of(Component.text("A sweet fruit").color(NamedTextColor.DARK_GRAY)));
        PersistentDataContainer persistentDataContainer = meta.getPersistentDataContainer();
        persistentDataContainer.set(PERSISTENT_DATA_KEY, PersistentDataType.STRING, key);
        persistentDataContainer.set(TBP_TAG, PersistentDataType.STRING, "garden:" + key);
        persistentDataContainer.set(TBP_SCORE, PersistentDataType.DOUBLE, 1D);
        persistentDataContainer.set(TBP_DISPLAY_NAME, PersistentDataType.STRING, serializedDisplayName);
        item.setItemMeta(meta);
        return item;
    }

    public void setDataOnPlayerSkullBlock(Block block) {
        if (block.getType() != Material.PLAYER_HEAD) {
            return;
        }
        Skull skull = (Skull) block.getState();
        skull.getPersistentDataContainer().set(PERSISTENT_DATA_KEY, PersistentDataType.STRING, key);
        skull.update();
    }

    public static boolean isPlayerSkullBlock(Block block) {
        if (block.getType() != Material.PLAYER_HEAD) {
            return false;
        }
        Skull skull = (Skull) block.getState();
        return skull.getPersistentDataContainer().has(PERSISTENT_DATA_KEY);
    }

    private PlayerProfile getPlayerProfile() {
        PlayerProfile profile = Bukkit.createProfile(CONSTANT_UUID);
        profile.getProperties().add(new ProfileProperty("textures", base64));
        return profile;
    }

    // Util

    public static boolean isPlant(ItemStack item) {
        if (item == null) return false;
        return item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(PERSISTENT_DATA_KEY, PersistentDataType.STRING);
    }

    @Nullable
    public static PlantType getPlantType(ItemStack item) {
        if (!isPlant(item)) return null;
        String field_name = item.getItemMeta().getPersistentDataContainer().get(PERSISTENT_DATA_KEY, PersistentDataType.STRING);
        if (field_name == null) return null;
        return valueOf(field_name);
    }

    @Nullable
    public static PlantType getPlantType(Block block) {
        if (block.getType() != Material.PLAYER_HEAD) return null;
        Skull skull = (Skull) block.getState();
        String field_name = skull.getPersistentDataContainer().get(PERSISTENT_DATA_KEY, PersistentDataType.STRING);
        if (field_name == null) return null;
        return valueOf(field_name);
    }


    // Reflect

    private static final Map<String, PlantType> VALUES = new HashMap<>();

    static {
        for (Field field : PlantType.class.getDeclaredFields()) {
            if (field.getType() != PlantType.class) continue;

            try {
                PlantType plantType = (PlantType) field.get(null);
                VALUES.put(plantType.key(), plantType);
            } catch (IllegalAccessException e) {
                BreweryGarden.getInstance().getLogger().severe("Failed to get field reflectively.");
                e.printStackTrace();
            }
        }
    }

    public static PlantType valueOf(String name) {
        return VALUES.get(name);
    }

    public static List<PlantType> values() {
        return VALUES.values().stream().toList();
    }

    @Override
    public String toString() {
        return key;
    }

    @Override
    public String name() {
        return key;
    }

    @Override
    public String key() {
        return key;
    }
}
