package griglog.relt.table_resolving

import griglog.relt.wrapHoverName
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.*
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentInstance
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider
import java.util.*

private fun enchSet() = TreeSet<EnchantmentInstance> { a, b ->
    if (a.enchantment != b.enchantment)
        BuiltInRegistries.ENCHANTMENT.getId(a.enchantment).compareTo(BuiltInRegistries.ENCHANTMENT.getId(b.enchantment))
    else
        a.level.compareTo(b.level)
}

private val allDiscoverableEnchants = enchSet().apply{
    for (ench in BuiltInRegistries.ENCHANTMENT.filter{it.isDiscoverable}){
        for (lvl in ench.minLevel..ench.maxLevel)
            add(EnchantmentInstance(ench, lvl))
    }
}

//todo: in case of poor performance, cache BuiltInRegistries.Item.getId(stack.item)
class ItemLike{
    constructor(item: Item){
        stack = ItemStack(item)
    }
    var stack: ItemStack
    private var enchantments = enchSet()

    fun enchantWithLevels(levels: NumberProvider, treasure: Boolean){
        val lvls = resolveNumber(levels)
        var minPoints = lvls.start
        var maxPoints = lvls.endInclusive
        minPoints += 1
        maxPoints += 3 + (stack.item.enchantmentValue / 4f).toInt() * 2
        minPoints = (minPoints * 0.85f).toInt()
        maxPoints = (maxPoints * 1.15f).toInt()
        if (minPoints <= 0) minPoints = 1
        if (maxPoints <= 0) maxPoints = 1

        for (ench in BuiltInRegistries.ENCHANTMENT){
            if ((ench.isTreasureOnly && !treasure)
                || !ench.isDiscoverable
                || (stack.item != Items.BOOK && !ench.category.canEnchant(stack.item)))
                continue
            for (lvl in ench.minLevel..ench.maxLevel){
                if (ench.getMaxCost(lvl) < minPoints || ench.getMinCost(lvl) > maxPoints)
                    continue
                enchantments.add(EnchantmentInstance(ench, lvl))
            }
        }
        tryEnchantedBook()
        //wrapHoverName(stack)
    }

    fun enchantRandom(enchants: Collection<Enchantment>){
        tryEnchantedBook()
        for (ench in enchants){
            for (lvl in ench.minLevel..ench.maxLevel)
                enchantments.add(EnchantmentInstance(ench, lvl))
        }
    }

    fun enchantRandom(){
        tryEnchantedBook()
        enchantments = allDiscoverableEnchants
    }

    fun tryEnchantedBook(){
        if (stack.item is BookItem) stack = ItemStack(Items.ENCHANTED_BOOK).apply{tag = stack.tag}
    }

    fun writeMap(){
        if (stack.item is MapItem) stack = ItemStack(Items.FILLED_MAP).apply{tag = stack.tag}
        wrapHoverName(stack)
    }

    fun genStacks(): Collection<ItemStack>{
        if (enchantments.size == 0)
            return listOf(stack) //todo: cache these?
        val res = mutableListOf<ItemStack>()
        for (pair in enchantments){
            if (stack.item != Items.ENCHANTED_BOOK && !pair.enchantment.canEnchant(stack))
                continue
            res.add(stack.copy().apply{enchant(pair.enchantment, pair.level)})
        }
        return res
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemLike
        if (!ItemStack.isSameItemSameTags(stack, other.stack))
            return false

        if (enchantments == other.enchantments)
            return true
        if (enchantments.size != other.enchantments.size)
            return false
        val i1 = enchantments.iterator()
        val i2 = other.enchantments.iterator()
        while (i1.hasNext()){
            if (i1.next() != i2.next())
                return false
        }
        return true
    }

    override fun hashCode(): Int {
        return stack.item.hashCode()
    }


}
