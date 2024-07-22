package griglog.relt.rei_plugin

import griglog.relt.table_resolving.ItemLike
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.util.EntryIngredients
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.util.*

/** This class has nothing to do with rendering, it is responsible for storing input items and output items,
which is required to link recipes together */
class TableDisplay(
    name: Identifier,
    result: Collection<ItemLike>,
    tables: Collection<EntryStack<Identifier>>,
    inputs: EntryIngredient?
) : BasicDisplay(
    mutableListOf(tableIng(name), inputs).filterNotNull(),
    mutableListOf<EntryIngredient>().apply {
        addAll(result.sortedWith(Comparator.comparingInt { il -> Registries.ITEM.getRawId(il.stack.item) })
            .map { EntryIngredients.ofItemStacks(it.genStacks(MinecraftClient.getInstance().world!!)) })
        addAll(tables.map { EntryIngredient.of(it) })
    },
    Optional.of(name)
) {

    override fun getCategoryIdentifier() = categoryId
}

fun tableIng(lootTableName: Identifier): EntryIngredient {
    return EntryIngredients.of(TableEntryDef.type, listOf(lootTableName))
}

