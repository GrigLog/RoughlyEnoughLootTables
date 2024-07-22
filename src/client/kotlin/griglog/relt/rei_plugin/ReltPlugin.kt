package griglog.relt.rei_plugin

import griglog.relt.entry_points.ReltClient
import griglog.relt.table_storage.clientTables
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry
import me.shedaniel.rei.api.common.plugins.REIServerPlugin
import me.shedaniel.rei.api.common.util.EntryIngredients
import net.minecraft.client.MinecraftClient
import net.minecraft.item.FishingRodItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.loot.LootTable
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.Identifier

class ReltClient : REIClientPlugin {
    override fun registerCategories(registry: CategoryRegistry) {
        registry.add(TableCategory())
        registry.addWorkstations(categoryId, tableIng(TableEntryDef.rootId))
    }

    override fun registerDisplays(registry: DisplayRegistry) {
        clientTables.forEach { (name, table) ->
            val (items, tables) = table.resolve(MinecraftClient.getInstance().world!!)
            if (ReltClient.config.skipEmptyTables && items.size + tables.size == 0) {
                return@forEach
            }
            registry.add(TableDisplay(name, items, tables, getInputs(name, table)))
        }
    }
}

class ReltServer : REIServerPlugin {
    override fun registerEntryTypes(registry: EntryTypeRegistry) {
        registry.register(TableEntryDef.type, TableEntryDef())
    }
}

fun getInputs(name: Identifier, table: LootTable): EntryIngredient? {
    if (table.type == LootContextTypes.BARTER) {
        return EntryIngredients.ofItemTag(ItemTags.PIGLIN_LOVED)
    }

    if (table.type == LootContextTypes.FISHING) {
        return EntryIngredients.ofItemStacks(Registries.ITEM.filterIsInstance<FishingRodItem>().map { ItemStack(it) })
    }

    if (table.type == LootContextTypes.CHEST) {
        return EntryIngredients.of(ItemStack(Items.CHEST))
    }

    return null
}
