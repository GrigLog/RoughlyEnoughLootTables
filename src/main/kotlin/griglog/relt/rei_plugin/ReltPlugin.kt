package griglog.relt.rei_plugin

import griglog.relt.table_storage.clientTables
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.util.EntryIngredients
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.BookItem
import net.minecraft.world.item.FishingRodItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionUtils
import net.minecraft.world.level.storage.loot.BuiltInLootTables
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets

class ReltPlugin : REIClientPlugin {
    override fun registerCategories(registry: CategoryRegistry) {
        registry.add(BarteringCategory())
    }

    override fun registerDisplays(registry: DisplayRegistry) {
        clientTables.forEach{(name, table) ->
            registry.add(TableDisplay(name, table.collectItems(), getInputs(name, table)))
        }
    }
}

fun getInputs(name: ResourceLocation, table: LootTable): EntryIngredient?{
    if (table.paramSet == LootContextParamSets.PIGLIN_BARTER)
        return EntryIngredients.ofItemTag(ItemTags.PIGLIN_LOVED)
    if (table.paramSet == LootContextParamSets.FISHING)
        return EntryIngredients.ofItemStacks(Registry.ITEM.filter{it is FishingRodItem}.map{ItemStack(it)})
    if (table.paramSet == LootContextParamSets.CHEST)
        return EntryIngredients.of(ItemStack(Items.CHEST))
    return null
}


fun LootTable.collectItems(): Collection<ItemLike> {
    val items = mutableListOf<ItemLike>()
    pools.forEach { pool ->
        pool.entries
            .filter { it is LootItem }
            .forEach itemLoop@{ entry ->
                val lootItem = entry as LootItem
                if (lootItem.item == Items.AIR)
                    return@itemLoop
                val item = ItemLike(lootItem.item)
                lootItem.functions.forEach { function ->
                    when (function) {
                        is SetNbtFunction -> item.applyAll { it.tag = function.tag }
                        is EnchantRandomlyFunction -> {
                            if (function.enchantments.isNotEmpty())
                                item.enchant(function.enchantments)
                            else item.enchant(Registry.ENCHANTMENT.filter {
                                it.isDiscoverable && (lootItem.item is BookItem || it.canEnchant(item.variants[0]))
                            })
                        }
                        is SetPotionFunction -> item.applyAll { PotionUtils.setPotion(it, function.potion) }
                    }
                    items.add(item)
                }
            }
    }
    return items
}