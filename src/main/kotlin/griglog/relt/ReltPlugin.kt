package griglog.relt

import griglog.relt.piglin_bartering.BarteringCategory
import griglog.relt.piglin_bartering.BarteringDisplay
import griglog.relt.piglin_bartering.barteringCategoryId
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry
import me.shedaniel.rei.api.common.plugins.REIServerPlugin
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.server.ServerStartCallback
import net.minecraft.client.Minecraft
import net.minecraft.data.loot.PiglinBarterLoot
import net.minecraft.world.entity.monster.piglin.PiglinAi
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class ReltPlugin : REIClientPlugin {
    override fun registerCategories(registry: CategoryRegistry) {
        registry.add(BarteringCategory())

    }

    override fun registerDisplays(registry: DisplayRegistry) {
        registry.add(BarteringDisplay(ItemStack(Items.DIAMOND)))
    }
}