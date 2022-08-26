package griglog.relt.table_resolving

import griglog.relt.wrapHoverName
import net.minecraft.core.Registry
import net.minecraft.world.item.BookItem
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionUtils
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.*


fun LootTable.collectItems(): Collection<ItemLike> {
    val items = hashSetOf<ItemLike>()
    pools.forEach { pool ->
        pool.entries
            .filter { it is LootItem }
            .forEach itemLoop@{ entry ->
                val lootItem = entry as LootItem
                if (lootItem.item == Items.AIR)
                    return@itemLoop
                val item = ItemLike(lootItem.item)
                lootItem.functions.forEach { function ->
                    when (function) {
                        is SetNbtFunction -> item.stack.tag = function.tag
                        is EnchantRandomlyFunction -> {
                            if (function.enchantments.isNotEmpty())
                                item.enchantRandom(function.enchantments)
                            else item.enchantRandom()
                        }
                        is EnchantWithLevelsFunction -> item.enchantWithLevels()
                        is SetPotionFunction -> PotionUtils.setPotion(item.stack, function.potion)
                        is ExplorationMapFunction -> wrapHoverName(item.stack)
                        //is SetContainerContents
                        //is SmeltItemFunction
                    }
                }
                items.add(item)
            }
    }
    return items
}