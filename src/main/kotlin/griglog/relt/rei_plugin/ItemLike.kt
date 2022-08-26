package griglog.relt.rei_plugin

import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantment

class ItemLike{
    constructor (item: Item){
        variants.add(ItemStack(item))
    }
    var variants: MutableList<ItemStack> = mutableListOf()

    fun enchant(enchants: Collection<Enchantment>){
        var newVariants = mutableListOf<ItemStack>()
        enchants.forEach{ench ->
            variants.forEach {
                for (lvl in ench.minLevel..ench.maxLevel)
                newVariants.add(it.copy().apply{enchant(ench, lvl)})
            }
        }
        variants = newVariants
    }

    fun applyAll(func: (ItemStack)->Unit){
        variants.forEach(func)
    }
}