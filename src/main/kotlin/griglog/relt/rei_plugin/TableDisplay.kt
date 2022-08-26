package griglog.relt.rei_plugin

import griglog.relt.ItemLike
import me.shedaniel.rei.api.client.util.ClientEntryIngredients
import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.util.EntryIngredients
import net.minecraft.resources.ResourceLocation

val tableEntry: EntryIngredient = ClientEntryIngredients.of(TableEntry())

class TableDisplay(
    val name: ResourceLocation,
    val result: Collection<ItemLike>,
    val inputs: EntryIngredient?
    ) : Display {
    constructor(name: ResourceLocation, result: Collection<ItemLike>) : this(name, result, null)

    override fun getInputEntries(): List<EntryIngredient> = mutableListOf(tableEntry).apply {inputs?.let {this.add(it) }}

    override fun getOutputEntries(): List<EntryIngredient> = result.map{EntryIngredients.ofItemStacks(it.variants)}

    override fun getCategoryIdentifier() = categoryId
}

