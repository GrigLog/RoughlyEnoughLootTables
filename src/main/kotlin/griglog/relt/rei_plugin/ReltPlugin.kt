package griglog.relt.rei_plugin

import griglog.relt.table_resolving.resolve
import griglog.relt.table_storage.clientTables
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry
import me.shedaniel.rei.api.common.plugins.REIServerPlugin
import me.shedaniel.rei.api.common.util.EntryIngredients
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.FishingRodItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets

class ReltClient : REIClientPlugin {
    override fun registerCategories(registry: CategoryRegistry) {
        registry.add(TableCategory())
        registry.addWorkstations(categoryId, tableIng(TableEntryDef.rootId))
    }

    override fun registerDisplays(registry: DisplayRegistry) {
        clientTables.forEach{(name, table) ->
            val (items, tables) = table.resolve()
            registry.add(TableDisplay(name, items, tables, getInputs(name, table)))
        }
    }
}

class ReltServer : REIServerPlugin{
    override fun registerEntryTypes(registry: EntryTypeRegistry) {
        registry.register(TableEntryDef.type, TableEntryDef())
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
