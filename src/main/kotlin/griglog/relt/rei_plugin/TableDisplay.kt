package griglog.relt.rei_plugin

import griglog.relt.table_resolving.ItemLike
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.util.EntryIngredients
import net.minecraft.resources.ResourceLocation
import java.util.*

class TableDisplay : BasicDisplay {
    constructor(name: ResourceLocation, result: Collection<ItemLike>, inputs: EntryIngredient?) : super(
        mutableListOf(tableIng(name), inputs).filterNotNull(),
        result.map { EntryIngredients.ofItemStacks(it.genStacks())},
        Optional.of(name)
    )

    override fun getCategoryIdentifier() = categoryId
}

