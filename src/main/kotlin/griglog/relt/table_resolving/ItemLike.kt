package griglog.relt.table_resolving

import griglog.relt.RELT
import griglog.relt.wrapHoverName
import net.minecraft.core.Registry
import net.minecraft.nbt.NbtUtils
import net.minecraft.world.item.BookItem
import net.minecraft.world.item.EnchantedBookItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.MapItem
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider
import java.util.*

private fun enchSet() = TreeSet<Enchantment>(Comparator.comparingInt{ench -> Registry.ENCHANTMENT.getId(ench)})

private val allDiscoverableEnchants = enchSet().apply{
    for (ench in Registry.ENCHANTMENT.filter{it.isDiscoverable}){
        add(ench)
    }
}

//todo: in case of poor performance, cache Registry.Item.getId(stack.item)
class ItemLike{
    constructor(item: Item){
        stack = ItemStack(item)
    }
    var stack: ItemStack
    private var enchantments = enchSet()

    fun enchantWithLevels(){ //TODO: smartass implementation???
        tryEnchantedBook()
        wrapHoverName(stack)
    }

    fun enchantRandom(enchants: Collection<Enchantment>){
        tryEnchantedBook()
        enchantments.addAll(enchants)
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
        if (stack.item is BookItem || stack.item is EnchantedBookItem){
            for (ench in enchantments){
                for (lvl in ench.minLevel..ench.maxLevel){
                    res.add(ItemStack(Items.ENCHANTED_BOOK).apply{enchant(ench, lvl)})
                }
            }
        }
        else {
            for (ench in enchantments){
                if (!ench.canEnchant(stack))
                    continue
                for (lvl in ench.minLevel..ench.maxLevel){
                    res.add(stack.copy().apply{enchant(ench, lvl)})
                }
            }
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