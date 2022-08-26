package griglog.relt.rei_plugin

import griglog.relt.table_resolving.ItemLike
import me.shedaniel.rei.api.client.util.ClientEntryIngredients
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.util.EntryIngredients
import net.minecraft.resources.ResourceLocation
import java.util.*

val tableEntry: EntryIngredient = ClientEntryIngredients.of(TableEntry())

class TableDisplay : BasicDisplay {
    constructor(name: ResourceLocation, result: Collection<ItemLike>, inputs: EntryIngredient?) : super(
        mutableListOf(tableEntry, inputs).filterNotNull(),
        result.map { EntryIngredients.ofItemStacks(it.genStacks())},
        Optional.of(name)
    )

    override fun getCategoryIdentifier() = categoryId
}

