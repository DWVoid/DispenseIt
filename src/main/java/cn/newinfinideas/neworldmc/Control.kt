package cn.newinfinideas.neworldmc

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.plugin.Plugin

fun makeInfiniteItemRecipe(
        parent: Plugin, // parent plugin
        item: ItemStack // The item involved
): NamespacedKey {
    val newItem = addInfinite8(item)
    val key = NamespacedKey(parent, "infinite_${newItem.type.name}")
    Bukkit.addRecipe(ShapedRecipe(key, newItem)
            .shape("EEE", "ESE", "EEE").setIngredient('S', item)
            .setIngredient('E', getEnchantmentBook(Enchantment.ARROW_INFINITE, 1))
    )
    return key
}

private fun addInfinite8(item: ItemStack): ItemStack {
    val newItem = item.clone()
    newItem.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 8)
    val meta = newItem.itemMeta
    meta.setDisplayName("${ChatColor.GREEN}${item.i18NDisplayName}")
    newItem.itemMeta = meta
    return newItem
}

fun makeExpensiveInfiniteItemRecipe(parent: Plugin, item: ItemStack, core: ItemStack): NamespacedKey {
    val newItem = addInfinite8(item)
    val key = NamespacedKey(parent, "infinite_${newItem.type.name}")
    Bukkit.addRecipe(ShapelessRecipe(key, newItem).addIngredient(item).addIngredient(core))
    return key
}

private fun makeExpensiveInfiniteCraftingCorePartial(name: String): ItemStack {
    val item = ItemStack(Material.NETHER_STAR)
    item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 7)
    val meta = item.itemMeta
    meta.setDisplayName("${ChatColor.GREEN}$name")
    item.itemMeta = meta
    return item
}

private fun makeExpensiveInfiniteCraftingCore(name: String): ItemStack {
    val item = ItemStack(Material.NETHER_STAR)
    item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 42)
    val meta = item.itemMeta
    meta.setDisplayName("${ChatColor.GOLD}$name")
    item.itemMeta = meta
    return item
}

fun makeExpensiveInfiniteCraftingCoreCraftingRecipe(parent: Plugin, partialName: String, name: String): ItemStack {
    val partialCore = makeExpensiveInfiniteCraftingCorePartial(partialName)
    val core = makeExpensiveInfiniteCraftingCore(name)
    val keyPart = NamespacedKey(parent, "infinite_core_partial")
    val keyFull = NamespacedKey(parent, "infinite_core")
    val stack = ItemStack(Material.CHORUS_FRUIT, 16)
    Bukkit.addRecipe(ShapedRecipe(keyPart, partialCore)
            .shape(" E ", "ESE", " E ").setIngredient('S', stack)
            .setIngredient('E', getEnchantmentBook(Enchantment.ARROW_INFINITE, 1))
    )
    Bukkit.addRecipe(ShapedRecipe(keyFull, core).shape(" E ", "EEE", " E ").setIngredient('E', partialCore))
    return core
}

fun removeExpensiveInfiniteCraftingCoreCraftingRecipe(parent: Plugin) {
    Bukkit.removeRecipe(NamespacedKey(parent, "infinite_core_partial"))
    Bukkit.removeRecipe(NamespacedKey(parent, "infinite_core"))
}

@Suppress("SameParameterValue")
private fun getEnchantmentBook(enchant: Enchantment, level: Int): ItemStack {
    val book = ItemStack(Material.ENCHANTED_BOOK)
    val meta = book.itemMeta as EnchantmentStorageMeta
    meta.addStoredEnchant(enchant, level, true)
    book.itemMeta = meta
    return book
}
