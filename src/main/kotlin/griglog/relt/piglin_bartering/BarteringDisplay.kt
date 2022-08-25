package griglog.relt.piglin_bartering

import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.api.common.display.DisplaySerializer
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.util.EntryIngredients
import net.minecraft.nbt.CompoundTag
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.ItemStack

val piglinLoved = listOf(EntryIngredients.ofItemTag(ItemTags.PIGLIN_LOVED))

class BarteringDisplay(val result: ItemStack) : Display {

    override fun getInputEntries(): List<EntryIngredient> = piglinLoved

    override fun getOutputEntries(): List<EntryIngredient> = listOf(EntryIngredients.of(result))

    override fun getCategoryIdentifier() = barteringCategoryId

    class Serializer : DisplaySerializer<BarteringDisplay> {
        override fun save(tag: CompoundTag, display: BarteringDisplay): CompoundTag {
            tag.put("result", CompoundTag().apply{display.result.save(this)})
            return tag
        }

        override fun read(tag: CompoundTag): BarteringDisplay {
            return BarteringDisplay(ItemStack.of(tag.getCompound("result")))
        }
    }
}

