package griglog.relt.table_resolving

import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator

fun resolveNumber(n: NumberProvider): IntRange{
    when (n){
        is ConstantValue -> return n.value.toInt()..n.value.toInt()
        is UniformGenerator -> return resolveNumber(n.min).start..resolveNumber(n.max).endInclusive
        is BinomialDistributionGenerator -> return 0..resolveNumber(n.n).endInclusive
    }
    return 1..1
}