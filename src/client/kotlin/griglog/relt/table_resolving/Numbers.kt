package griglog.relt.table_resolving

import net.minecraft.loot.provider.number.BinomialLootNumberProvider
import net.minecraft.loot.provider.number.ConstantLootNumberProvider
import net.minecraft.loot.provider.number.LootNumberProvider
import net.minecraft.loot.provider.number.UniformLootNumberProvider

fun resolveNumber(n: LootNumberProvider): IntRange{
    when (n){
        is ConstantLootNumberProvider -> return n.value.toInt()..n.value.toInt()
        is UniformLootNumberProvider -> return resolveNumber(n.min).first..resolveNumber(n.max).last
        is BinomialLootNumberProvider -> return 0..resolveNumber(n.n).last
    }
    return 1..1
}