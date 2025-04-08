package me.josielcm.jcm.api.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;

import me.josielcm.jcm.Base;
import me.josielcm.jcm.api.formats.Color;
import net.kyori.adventure.text.Component;

/**
 * A builder class for creating and manipulating ItemStacks with a fluent API.
 * This class provides methods for customizing items with display names, lore, enchantments,
 * custom model data, and more specialized features like player head textures.
 * 
 * <p>Example usage:
 * <pre>
 * ItemStack sword = ItemBuilder.builder()
 *     .material(Material.DIAMOND_SWORD)
 *     .displayName("<gold>Excalibur")
 *     .lore("<gold>The legendary sword")
 *     .enchant(Enchantment.DAMAGE_ALL, 5)
 *     .unbreakable(true)
 *     .build();
 * </pre>
 * 
 * @author JosielCM
 */
public class ItemBuilder {

    private ItemStack item;
    private ItemMeta meta;
    private SkullMeta skullMeta;
    private final List<Component> lore = new ArrayList<>();
    private final Map<Enchantment, Integer> enchantments = new HashMap<>();

    /**
     * Creates a new ItemBuilder with the specified material.
     * 
     * @param material The material for the item
     */
    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
        if (item.getType() == Material.PLAYER_HEAD)
            this.skullMeta = (SkullMeta) meta;
    }

    /**
     * Static factory method to create a new ItemBuilder instance.
     * Creates a builder with STONE as the default material.
     * 
     * @return A new ItemBuilder instance
     */
    public static ItemBuilder builder() {
        return new ItemBuilder(Material.STONE);
    }

    /**
     * Sets the material of the item and updates the item meta accordingly.
     * This will preserve any existing display name, lore, or custom model data.
     * 
     * @param material The material to set
     * @return This builder for method chaining
     */
    @SuppressWarnings("deprecation")
    public ItemBuilder material(Material material) {
        item.setType(material);
        
        ItemMeta newMeta = Bukkit.getItemFactory().getItemMeta(material);

        if (meta != null) {
            if (meta.hasDisplayName()) newMeta.displayName(meta.displayName());
            if (meta.hasLore()) newMeta.lore(meta.lore());
            if (meta.hasCustomModelData()) newMeta.setCustomModelData(meta.getCustomModelData());
        }

        item.setItemMeta(newMeta);

        if (item.getType() == Material.PLAYER_HEAD) {
            this.skullMeta = (SkullMeta) item.getItemMeta();
            this.meta = this.skullMeta;
        } else {
            this.meta = item.getItemMeta();
            this.skullMeta = null;
        }

        return this;
    }

    /**
     * Sets the display name of the item with color formatting.
     * 
     * @param displayName The display name to set (supports color codes)
     * @return This builder for method chaining
     */
    public ItemBuilder displayName(String displayName) {
        if (skullMeta != null)
            skullMeta.displayName(Color.parse(displayName));
        else if (meta != null)
            meta.displayName(Color.parse(displayName));
        return this;
    }

    /**
     * Adds a single line to the item's lore with color formatting.
     * 
     * @param lore The lore line to add (supports color codes)
     * @return This builder for method chaining
     */
    public ItemBuilder lore(String lore) {
        this.lore.add(Color.parse(lore));
        return this;
    }

    /**
     * Sets multiple lines of lore on the item with color formatting.
     * 
     * @param lore A list of lore lines (supports color codes)
     * @return This builder for method chaining
     */
    public ItemBuilder lore(List<String> lore) {
        for (String line : lore) {
            this.lore.add(Color.parse(line));
        }
        return this;
    }

    /**
     * Sets multiple lines of lore using pre-formatted Components.
     * 
     * @param lore A list of Component lore lines
     * @return This builder for method chaining
     */
    public ItemBuilder loreComponents(List<Component> lore) {
        this.lore.forEach(this.lore::add);
        return this;
    }

    /**
     * Applies a custom modification function to the item's meta.
     * Useful for applying changes not covered by other methods.
     * 
     * @param modifier A Consumer function that modifies the ItemMeta
     * @return This builder for method chaining
     */
    public ItemBuilder metaModifier(Consumer<ItemMeta> modifier) {
        if (skullMeta != null)
            modifier.accept(skullMeta);
        else if (meta != null)
            modifier.accept(meta);
        return this;
    }

    /**
     * Adds an enchantment to the item.
     * Will be applied to the item during the build process.
     * 
     * @param enchantment The enchantment to add
     * @param level The level of the enchantment
     * @return This builder for method chaining
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        enchantments.put(enchantment, level);
        return this;
    }

    /**
     * Sets the stack size of the item.
     * 
     * @param amount The amount to set
     * @return This builder for method chaining
     */
    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    /**
     * Adds a persistent data container entry to the item.
     * Currently only supports string values.
     * 
     * @param key The namespaced key for the data
     * @param value The value to store (will be converted to string)
     * @return This builder for method chaining
     */
    public ItemBuilder pdc(NamespacedKey key, Object value) {
        if (skullMeta != null)
            skullMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value.toString());
        else if (meta != null)
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value.toString());
        return this;
    }

    /**
     * Sets a custom texture for a player head item.
     * Requires the item to be a PLAYER_HEAD material.
     * 
     * @param texture The base64 texture data
     * @return This builder for method chaining
     * @throws IllegalArgumentException if the item is not a player head or the texture is invalid
     */
    public ItemBuilder skullTexture(String texture) {
        if (item.getType() != Material.PLAYER_HEAD)
            throw new IllegalArgumentException("Item must be a player head.");
        if (texture == null || texture.isEmpty())
            throw new IllegalArgumentException("Texture cannot be null.");
        if (texture.equalsIgnoreCase("NONE"))
            return this;

        if (skullMeta != null) {
            try {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
                profile.setProperty(new ProfileProperty("textures", texture));
                skullMeta.setPlayerProfile(profile);
            } catch (Exception e) {
                Base.getInstance().getLogger().log(Level.WARNING, "Error while setting skull texture: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return this;
    }

    /**
     * Sets whether the item is unbreakable.
     * 
     * @param unbreakable Whether the item should be unbreakable
     * @return This builder for method chaining
     */
    public ItemBuilder unbreakable(boolean unbreakable) {
        if (skullMeta != null)
            skullMeta.setUnbreakable(unbreakable);
        else if (meta != null)
            meta.setUnbreakable(unbreakable);
        return this;
    }

    /**
     * Hides the attributes of the item (like attack damage, armor points).
     * 
     * @return This builder for method chaining
     */
    public ItemBuilder hideAttributes() {
        if (skullMeta != null)
            skullMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        else if (meta != null)
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        return this;
    }

    /**
     * Hides enchantments from the item tooltip.
     * 
     * @return This builder for method chaining
     */
    public ItemBuilder hideEnchants() {
        if (skullMeta != null)
            skullMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        else if (meta != null)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    /**
     * Hides the unbreakable state from the item tooltip.
     * 
     * @return This builder for method chaining
     */
    public ItemBuilder hideUnbreakable() {
        if (skullMeta != null)
            skullMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        else if (meta != null)
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        return this;
    }

    /**
     * Hides dye information from the item tooltip.
     * 
     * @return This builder for method chaining
     */
    public ItemBuilder hideDye() {
        if (skullMeta != null)
            skullMeta.addItemFlags(ItemFlag.HIDE_DYE);
        else if (meta != null)
            meta.addItemFlags(ItemFlag.HIDE_DYE);
        return this;
    }

    /**
     * Hides potion effects from the item tooltip.
     * 
     * @return This builder for method chaining
     */
    public ItemBuilder hidePotionEffects() {
        if (skullMeta != null)
            skullMeta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        else if (meta != null)
            meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        return this;
    }

    /**
     * Sets the custom model data for the item.
     * Used for custom textures with resource packs.
     * 
     * @param data The custom model data integer
     * @return This builder for method chaining
     */
    public ItemBuilder customModelData(int data) {
        if (skullMeta != null)
            skullMeta.setCustomModelData(data);
        else if (meta != null)
            meta.setCustomModelData(data);
        return this;
    }
    
    /**
     * Builds the final ItemStack with all applied changes.
     * 
     * @return The constructed ItemStack
     */
    public ItemStack build() {
        if (skullMeta != null)
            item.setItemMeta(skullMeta);
        else
            item.setItemMeta(meta);

        item.addUnsafeEnchantments(enchantments);
        return item;
    }

    /**
     * Convenience method to quickly create a basic item.
     * 
     * @param displayName The display name for the item
     * @param material The material type
     * @param amount The stack size
     * @return The constructed ItemStack
     */
    public static ItemStack createItem(String displayName, Material material, int amount) {
        return ItemBuilder.builder()
                .material(material)
                .displayName(displayName)
                .amount(amount)
                .build();
    }

    /**
     * Creates an item from configuration values.
     * Supports material, display name, lore, custom model data, player head textures,
     * and persistent data container values.
     * 
     * @param section The configuration section to read from
     * @param path The base path within the section for item properties
     * @param key The NamespacedKey for storing PDC data
     * @param pdcValue The value to store in the PDC
     * @return The constructed ItemStack
     * @throws IllegalArgumentException if the material is invalid
     */
    public static ItemStack createItem(ConfigurationSection section, String path, NamespacedKey key, String pdcValue) {
        String materialName = section.getString(path + ".material");
        Material material = Material.matchMaterial(materialName);
        List<String> lore = section.getStringList(path + ".lore") == null ? new ArrayList<>() : section.getStringList(path + ".lore");
        int customModelData = section.getInt(path + ".modeldata", 0);
        if (material == null) {
            throw new IllegalArgumentException("Invalid material: " + materialName + " at " + section.getName() + " " + path);
        }

        String texture = section.getString(path + ".texture");

        ItemBuilder builder = ItemBuilder.builder()
                .material(material)
                .displayName(section.getString(path + ".displayname"))
                .lore(lore)
                .pdc(key, pdcValue);

        if (material == Material.PLAYER_HEAD && texture != null) builder.skullTexture(texture);
        if (customModelData != 0) builder.customModelData(customModelData);

        return builder.build();
    }
}