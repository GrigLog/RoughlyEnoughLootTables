package griglog.relt.table_resolving

import griglog.relt.mixin.EnchantRandomlyLootFunctionAccessor
import griglog.relt.mixin.EnchantWithlevelsLootFunctionAccessor
import griglog.relt.mixin.SetComponentsLootFunctionAccessor
import griglog.relt.mixin.SetPotionLootFunctionAccessor
import griglog.relt.wrapHoverName
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.PotionContentsComponent
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.EnchantmentLevelEntry
import net.minecraft.item.BookItem
import net.minecraft.item.FilledMapItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.loot.function.*
import net.minecraft.loot.provider.number.LootNumberProvider
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntryList
import net.minecraft.registry.tag.EnchantmentTags
import net.minecraft.world.World
import java.util.*
import kotlin.jvm.optionals.getOrNull

class ItemLike(item: Item, world: World) {

    var stack = ItemStack(item)
    private var enchantments = enchantmentSet(world)


    fun enchantWithLevels(world: World, levels: LootNumberProvider, treasure: Boolean) {
        val lvls = resolveNumber(levels)
        var minPoints = lvls.first
        var maxPoints = lvls.last

        minPoints += 1
        maxPoints += 1 + (stack.item.enchantability / 4) * 2
        minPoints = Math.round((minPoints * 0.85f))
        maxPoints = Math.round((maxPoints * 1.15f))

        if (minPoints <= 0) {
            minPoints = 1
        }

        if (maxPoints <= 0) {
            maxPoints = 1
        }

        val registry = world.registryManager.get(RegistryKeys.ENCHANTMENT)

        for (enchantment in registry) {
            val entry = registry.getEntry(enchantment)

            if (!treasure && entry.isIn(EnchantmentTags.TREASURE)
                || !entry.isIn(EnchantmentTags.IN_ENCHANTING_TABLE)
                || !stack.isOf(Items.BOOK) && (!enchantment.isPrimaryItem(stack) || !enchantment.isSupportedItem(stack))
            ) {
                continue
            }

            for (lvl in enchantment.minLevel..enchantment.maxLevel) {
                if (enchantment.getMaxPower(lvl) < minPoints || enchantment.getMinPower(lvl) > maxPoints) {
                    continue
                }

                enchantments.add(EnchantmentLevelEntry(entry, lvl))
            }
        }
        tryEnchantedBook()
    }

    fun enchantRandom(enchants: RegistryEntryList<Enchantment>) {
        tryEnchantedBook()
        for (enchantmentKey in enchants) {
            val enchantment = enchantmentKey.value()
            for (lvl in enchantment.minLevel..enchantment.maxLevel)
                enchantments.add(EnchantmentLevelEntry(enchantmentKey, lvl))
        }
    }

    fun enchantRandom(world: World) {
        tryEnchantedBook()
        enchantments = getAllDiscoverableEnchantments(world)
    }

    private fun tryEnchantedBook() {
        if (stack.item is BookItem) {
            stack = ItemStack(Items.ENCHANTED_BOOK).apply { applyComponentsFrom(stack.components) }
        }
    }

    fun writeMap() {
        if (stack.item is FilledMapItem) {
            stack = ItemStack(Items.FILLED_MAP).apply { applyComponentsFrom(stack.components) }
        }
        wrapHoverName(stack)
    }

    fun genStacks(world: World): Collection<ItemStack> {
        if (enchantments.size == 0) {
            return listOf(stack)
        }

        val res = mutableListOf<ItemStack>()

        for (entry in enchantments) {
            if (stack.item != Items.ENCHANTED_BOOK && (!entry.enchantment.value().isPrimaryItem(stack) || !entry.enchantment.value().isSupportedItem(stack))) {
                continue
            }

            res.add(stack.copy().apply {
                EnchantmentHelper.enchant(world.random, this, entry.level, world.registryManager, world.registryManager.get(RegistryKeys.ENCHANTMENT).getEntryList(EnchantmentTags.ON_RANDOM_LOOT))
            })
        }
        return res
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemLike
        if (!ItemStack.areItemsAndComponentsEqual(stack, other.stack)) {
            return false
        }

        if (enchantments == other.enchantments) {
            return true
        }

        if (enchantments.size != other.enchantments.size) {
            return false
        }

        val i1 = enchantments.iterator()
        val i2 = other.enchantments.iterator()

        while (i1.hasNext()) {
            if (i1.next() != i2.next()) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        return stack.item.hashCode()
    }
}

private fun enchantmentSet(world: World) = TreeSet<EnchantmentLevelEntry> { a, b ->
    if (a.enchantment != b.enchantment) {
        val registry = world.registryManager.get(RegistryKeys.ENCHANTMENT)
        registry.getId(a.enchantment.value())?.compareTo(registry.getId(b.enchantment.value())) ?: 0
    } else {
        a.level.compareTo(b.level)
    }
}

private fun getAllDiscoverableEnchantments(world: World): TreeSet<EnchantmentLevelEntry> {
    return enchantmentSet(world).apply {
        val registry = world.registryManager.get(RegistryKeys.ENCHANTMENT)
        
        registry.forEach { enchantment ->
            val entry = registry.getEntry(enchantment)
            if (entry.isIn(EnchantmentTags.TREASURE)) {
                for (lvl in enchantment.minLevel..enchantment.maxLevel) {
                    add(EnchantmentLevelEntry(entry, lvl))
                }
            }
        }
    }
}


fun resolveItem(lootItem: Item, functions: List<LootFunction>, world: World): ItemLike? {
    if (lootItem == Items.AIR) {
        return null//I hope no modder will ever do this but better safe than sorry
    }

    val item = ItemLike(lootItem, world)

    functions.forEach { function ->
        when (function) {
            is SetComponentsLootFunction -> item.stack.applyUnvalidatedChanges((function as SetComponentsLootFunctionAccessor).changes)
            is EnchantRandomlyLootFunction -> {
                (function as EnchantRandomlyLootFunctionAccessor).options.ifPresentOrElse(
                    { item.enchantRandom(it) },
                    { item.enchantRandom(world) })
            }

            is EnchantWithLevelsLootFunction -> item.enchantWithLevels(
                world,
                (function as EnchantWithlevelsLootFunctionAccessor).levels,
                function.options.getOrNull()?.any { it.isIn(EnchantmentTags.TREASURE) } ?: false)

            is SetPotionLootFunction -> item.stack.apply(
                DataComponentTypes.POTION_CONTENTS,
                PotionContentsComponent.DEFAULT,
                (function as SetPotionLootFunctionAccessor).potion,
                PotionContentsComponent::with
            )

            is ExplorationMapLootFunction -> item.writeMap()
            //is SetContainerContents
            //is SmeltItemFunction
        }
    }
    return item
}
