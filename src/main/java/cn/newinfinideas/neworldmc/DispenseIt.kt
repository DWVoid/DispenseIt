package cn.newinfinideas.neworldmc

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class DispenseIt : JavaPlugin()  {
    private var recipeUnloadList: Array<NamespacedKey>? = null

    override fun onEnable() {
        super.onEnable()
        prepareConfig()
        prepareInfiniteItems()
        server.pluginManager.registerEvents(Events(), this)
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
        HandlerList.unregisterAll(this)
        super.onDisable()
    }
}