package griglog.relt.table_resolving

import griglog.relt.rei_plugin.TableEntryDef
import me.shedaniel.rei.api.common.entry.EntryStack
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionUtils
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.*
import net.minecraft.world.level.storage.loot.functions.*


fun LootTable.resolve(): Pair<Collection<ItemLike>, Collection<EntryStack<ResourceLocation>>> {
    val items = hashSetOf<ItemLike>()
    val tables = hashSetOf<EntryStack<ResourceLocation>>()
    for (pool in pools){
        for (entry in pool.entries) {
            resolveEntry(entry, items, tables, pool.functions)
        }
    }
    return Pair(items, tables)
}

fun resolveEntry(entry: LootPoolEntryContainer, items: MutableSet<ItemLike>,
                 tables: MutableSet<EntryStack<ResourceLocation>>, poolFunctions: Array<LootItemFunction>){
    when(entry){
        is LootItem -> resolveItem(entry.item, entry.functions + poolFunctions)?.let{items.add(it)}
        is LootTableReference -> tables.add(EntryStack.of(TableEntryDef.type, entry.name))
        is TagEntry -> BuiltInRegistries.ITEM.getTagOrEmpty(entry.tag).forEach{ item ->
            resolveItem(item.value(), entry.functions + poolFunctions)?.let{items.add(it)}
        }
        is CompositeEntryBase -> entry.children.forEach{resolveEntry(it, items, tables, poolFunctions)}
    }
}

fun resolveItem(lootItem: Item, functions: Array<LootItemFunction>): ItemLike?{
    if (lootItem == Items.AIR)
        return null //I hope no modder will ever do this but better safe than sorry
    val item = ItemLike(lootItem)
    functions.forEach { function ->
        when (function) {
            is SetNbtFunction -> item.stack.tag = function.tag
            is EnchantRandomlyFunction -> {
                if (function.enchantments.isNotEmpty())
                    item.enchantRandom(function.enchantments)
                else item.enchantRandom()
            }

            is EnchantWithLevelsFunction -> item.enchantWithLevels(function.levels, function.treasure)
            is SetPotionFunction -> PotionUtils.setPotion(item.stack, function.potion)
            is ExplorationMapFunction -> item.writeMap()
            //is SetContainerContents
            //is SmeltItemFunction
        }
    }
    return item
}
