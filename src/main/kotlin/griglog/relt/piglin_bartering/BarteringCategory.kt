package griglog.relt.piglin_bartering

import griglog.relt.RELT
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.util.EntryStacks
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

val barteringCategoryId: CategoryIdentifier<BarteringDisplay> = CategoryIdentifier.of(RELT.id, "plugins/piglin_bartering")

class BarteringCategory : DisplayCategory<BarteringDisplay> {
    override fun getCategoryIdentifier() = barteringCategoryId

    override fun getTitle() = EntityType.PIGLIN.description

    override fun getIcon() = EntryStacks.of(ItemStack(Items.GOLD_INGOT))
}