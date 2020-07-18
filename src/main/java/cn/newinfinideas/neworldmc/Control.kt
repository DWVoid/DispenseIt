package cn.newinfinideas.neworldmc

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Container
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.plugin.Plugin


fun makeInfiniteItemRecipe(
    parent: Plugin, // parent plugin
    item: ItemStack // The item involved
): NamespacedKey {
    val newItem = item.clone()
    newItem.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 8)
    val meta = newItem.itemMeta
    meta.setDisplayName(ChatColor.GREEN.toString() + item.i18NDisplayName)
    newItem.itemMeta = meta
    val key = NamespacedKey(parent, "infinite_" + newItem.type.name)
    val recipe = ShapedRecipe(key, newItem)
    recipe.shape("EEE", "ESE", "EEE")
    recipe.setIngredient('E', getEnchantmentBook(Enchantment.ARROW_INFINITE, 1))
    recipe.setIngredient('S', item)
    Bukkit.addRecipe(recipe)
    return key
}

@Suppress("SameParameterValue")
private fun getEnchantmentBook(enchant: Enchantment, level: Int): ItemStack {
    val book = ItemStack(Material.ENCHANTED_BOOK)
    val meta = book.itemMeta as EnchantmentStorageMeta
    meta.addStoredEnchant(enchant, level, true)
    book.itemMeta = meta
    return book
}

