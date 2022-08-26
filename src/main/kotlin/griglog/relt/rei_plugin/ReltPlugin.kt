package griglog.relt.rei_plugin

import griglog.relt.ItemLike
import griglog.relt.table_storage.clientTables
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.util.EntryIngredients
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionUtils
import net.minecraft.world.level.storage.loot.BuiltInLootTables
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction

class ReltPlugin : REIClientPlugin {
    override fun registerCategories(registry: CategoryRegistry) {
        registry.add(BarteringCategory())

    }

    override fun registerDisplays(registry: DisplayRegistry) {
        registry.add(TableDisplay(ResourceLocation("relt:test"), listOf(ItemLike(Items.DIAMOND))))
        val table = clientTables.get(BuiltInLootTables.PIGLIN_BARTERING)!!
        val items = mutableListOf<ItemLike>()
        table.pools.forEach { pool ->
            pool.entries
                .filter{ it is LootItem}
                .forEach { entry ->
                    val lootItem = entry as LootItem
                    val item = ItemLike(lootItem.item)
                    lootItem.functions.forEach{ function ->
                        when(function) {
                             is SetNbtFunction -> item.applyAll{it.tag = function.tag}
                             is EnchantRandomlyFunction -> item.enchant(function.enchantments)
                             is SetPotionFunction -> item.applyAll{PotionUtils.setPotion(it, function.potion)}
                        }
                        items.add(item)
                    }
                }
        }
        registry.add(TableDisplay(BuiltInLootTables.PIGLIN_BARTERING, items, EntryIngredients.ofItemTag(ItemTags.PIGLIN_LOVED)))
    }
}