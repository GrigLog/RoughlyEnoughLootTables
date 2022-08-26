package griglog.relt.table_resolving

import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator

fun resolveNumber(number: NumberProvider): IntRange{
    when(number){
        is ConstantValue -> return number.value.toInt()..number.value.toInt()
        is UniformGenerator -> return resolveNumber(number.min).first..resolveNumber(number.max).last
        is BinomialDistributionGenerator -> return 0..resolveNumber(number.n).last
    }
    return 1..1
}