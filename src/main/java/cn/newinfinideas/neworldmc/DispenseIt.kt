package cn.newinfinideas.neworldmc

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.block.Container
import org.bukkit.block.data.Directional
import org.bukkit.block.Dispenser
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs
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
        val listExpensiveInfinite = conf.getStringList("lists.expensive_infinite")
        val core = makeExpensiveInfiniteCraftingCoreCraftingRecipe(
                this, config.getString("names.expensive_core_partial")!!, config.getString("names.expensive_core")!!
        )
        val unregisterList = mutableListOf<NamespacedKey>()
        for (name in listInfinite) {
            Material.getMaterial(name)?.let { unregisterList.add(makeInfiniteItemRecipe(this, ItemStack(it))) }
        }
        for (name in listExpensiveInfinite) {
            Material.getMaterial(name)?.let { unregisterList.add(makeExpensiveInfiniteItemRecipe(this, ItemStack(it), core)) }
        }
        recipeUnloadList = unregisterList.toTypedArray()
    }


    private fun unloadInfiniteItems() {
        for (key in recipeUnloadList!!) Bukkit.removeRecipe(key)
        removeExpensiveInfiniteCraftingCoreCraftingRecipe(this)
        recipeUnloadList = null
    }

    override fun onDisable() {
        HandlerList.unregisterAll(this as Plugin)
        unloadInfiniteItems()
        super.onDisable()
    }

    @EventHandler
    fun onDispenserFire(event: BlockDispenseEvent) {
        // Note: Originally we want both dispensers and droppers to have this behaviour
        //     but the dispenser event handling have pretty wacky behaviours, so now we will only allow droppers
        when (event.block.type) {
            Material.DROPPER -> tryDupeItemAction(event)
            Material.DISPENSER -> tryPlaceBlockAction(event)
            else->{}
        }
    }

    private fun tryPlaceBlockAction(event: BlockDispenseEvent) {
        val block = event.block
        val dispenser = block.getState(false) as Dispenser
        if (!checkBlockPlacementInventoryPattern(dispenser.inventory.contents)) return
        event.isCancelled = true
        val directional = block.getState(false).blockData as Directional
        val location = block.location.add(directional.facing.direction)
        server.scheduler.scheduleSyncDelayedTask(this) { tryPutBlockDispenser(dispenser, location, directional) }
    }

    private fun tryPutBlockDispenser(dispenser: Dispenser, location: Location, directional: Directional) {
        if (!dispenser.isPlaced) return
        val stack = dispenser.inventory.contents[4] ?: return
        if (!stack.type.isBlock) return
        if (stack.type == Material.REDSTONE_BLOCK) return // REDSTONE_BLOCK causes bug
        if (!checkBlockPlacementInventoryPattern(dispenser.inventory.contents)) return
        if (stack.amount > 0) {
            val blockAt = dispenser.world.getBlockAt(location)
            if (blockAt.type.isAir) {
                blockAt.type = stack.type
                val blockAtState = blockAt.blockData
                when (blockAtState) {
                    is Directional -> blockAtState.facing = directional.facing
                    is Slab -> blockAtState.type = getSlabType(directional.facing)
                }
                blockAt.blockData = blockAtState
                stack.subtract()
            }
        }
    }

    private fun getSlabType(facing: BlockFace): Slab.Type {
        return when (facing) {
            BlockFace.DOWN -> Slab.Type.TOP
            else -> Slab.Type.BOTTOM
        }
    }

    private fun tryDupeItemAction(event: BlockDispenseEvent) {
        val now = event.item
        if (now.enchantments[Enchantment.ARROW_INFINITE] != 8) return
        val addBack = now.asOne()
        val inventory = (event.block.getState(false) as Container).inventory
        server.scheduler.scheduleSyncDelayedTask(this) { inventory.addItem(addBack) }
        event.item = prepareItemCommonDuplication(now)
    }

    private fun checkBlockPlacementInventoryPattern(stacks: Array<ItemStack?>): Boolean {
        if (!checkPatternSingleStack(stacks[0])) return false
        if (!checkPatternSingleStack(stacks[1])) return false
        if (!checkPatternSingleStack(stacks[2])) return false
        if (!checkPatternSingleStack(stacks[3])) return false
        if (!checkPatternSingleStack(stacks[5])) return false
        if (!checkPatternSingleStack(stacks[6])) return false
        if (!checkPatternSingleStack(stacks[7])) return false
        if (!checkPatternSingleStack(stacks[8])) return false
        return true
    }

    private fun checkPatternSingleStack(stacks: ItemStack?): Boolean {
        if (stacks == null) return false
        if (stacks.type != Material.DROPPER) return false
        return true
    }

    @EventHandler
    fun onInventoryMoveItem(event: InventoryMoveItemEvent) {
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
        meta.setDisplayName(null)
        next.itemMeta = meta
        return next
    }
}