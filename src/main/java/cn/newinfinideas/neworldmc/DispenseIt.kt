package cn.newinfinideas.neworldmc

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Container
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class DispenseIt : JavaPlugin(), Listener {
    private var recipeUnloadList: Array<NamespacedKey>? = null

    override fun onEnable() {
        super.onEnable()
        prepareConfig()
        prepareInfiniteItems()
        server.pluginManager.registerEvents(this, this)
    }

    private fun prepareConfig() {
        saveDefaultConfig()
        this.config.options().copyDefaults(true)
    }

    private fun prepareInfiniteItems() {
        val conf = this.config
        val listInfinite = conf.getStringList("lists.infinite")
        val unregisterList = mutableListOf<NamespacedKey>()
        for (name in listInfinite) {
            Material.getMaterial(name)?.let { unregisterList.add(makeInfiniteItemRecipe(this, ItemStack(it))) }
        }
        recipeUnloadList = unregisterList.toTypedArray()
    }

    private fun unloadInfiniteItems() {
        for (key in recipeUnloadList!!) Bukkit.removeRecipe(key)
    }

    override fun onDisable() {
        unloadInfiniteItems()
        HandlerList.unregisterAll(this as Plugin)
        super.onDisable()
    }

    @EventHandler
    fun onDispenserFire(event: BlockDispenseEvent) {
        // Note: Originally we want both dispensers and droppers to have this behaviour
        //     but the dispenser event handling have pretty wacky behaviours, so now we will only allow droppers
        if (event.block.type == Material.DROPPER) {
            val now = event.item
            if (now.enchantments[Enchantment.ARROW_INFINITE] != 8) return
            val addBack = now.asOne()
            val inventory = (event.block.getState(false) as Container).inventory
            server.scheduler.scheduleSyncDelayedTask(this) { inventory.addItem(addBack) }
            event.item = prepareItemCommonDuplication(now)
        }
    }

    @EventHandler
    fun onInventoryMode(event: InventoryMoveItemEvent) {
        val inventory = event.initiator
        if (inventory.type == InventoryType.DROPPER) {
            val now = event.item
            if (now.enchantments[Enchantment.ARROW_INFINITE] != 8) return
            event.item = prepareItemCommonDuplication(now)
        }
    }

    private fun prepareItemCommonDuplication(now: ItemStack): ItemStack {
        val next = now.clone()
        next.removeEnchantment(Enchantment.ARROW_INFINITE)
        val meta = next.itemMeta
        meta.setDisplayName(next.i18NDisplayName)
        next.itemMeta = meta
        return next
    }
}