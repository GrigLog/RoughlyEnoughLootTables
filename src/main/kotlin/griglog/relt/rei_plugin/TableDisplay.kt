package griglog.relt.rei_plugin

import griglog.relt.table_resolving.ItemLike
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.util.EntryIngredients
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import java.util.*

class TableDisplay : BasicDisplay {
    constructor(name: ResourceLocation, result: Collection<ItemLike>, tables:Collection<EntryStack<ResourceLocation>>, inputs: EntryIngredient?) : super(
        mutableListOf(tableIng(name), inputs).filterNotNull(),
        mutableListOf<EntryIngredient>().apply{
            addAll(result.sortedWith(Comparator.comparingInt{il -> Registry.ITEM.getId(il.stack.item)})
                .map{EntryIngredients.ofItemStacks(it.genStacks())})
            addAll(tables.map{EntryIngredient.of(it)})
        },
        Optional.of(name)
    )

    override fun getCategoryIdentifier() = categoryId
}

