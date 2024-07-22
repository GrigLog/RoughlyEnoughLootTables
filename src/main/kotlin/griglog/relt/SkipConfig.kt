package griglog.relt

import net.minecraft.loot.context.LootContextType
import net.minecraft.loot.context.LootContextTypes
import java.util.*

class SkipConfig(side: String) {    //no reloading because fuck you
    val skipEmptyTables: Boolean
    val skipTypes: Set<LootContextType>

    init {
        val config = SimpleConfig.of(RELT.MOD_ID + '_' + side).provider {
            """|#These options are present on both client and server sides. Both must be set to false in order to disable skipping
                |skipEmptyTables=true
                |skipBlockLoot=true
                |skipEntityLoot=true
                |skipCommandLoot=true""".trimMargin()
        }.request()

        skipEmptyTables = config.getOrDefault("skipEmptyTables", true)

        val skipTypes = mutableSetOf<LootContextType>()

        if (config.getOrDefault("skipBlockLoot", true)) {
            skipTypes.add(LootContextTypes.BLOCK)
        }
        if (config.getOrDefault("skipEntityLoot", true)) {
            skipTypes.add(LootContextTypes.ENTITY)
        }
        if (config.getOrDefault("skipCommandLoot", true)) {
            skipTypes.add(LootContextTypes.COMMAND)
        }
        this.skipTypes = Collections.unmodifiableSet(skipTypes)
    }
}