package griglog.relt
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager

object RELT{
    const val id = "roughly_enough_loot_tables"
    val SEND_LOOT_TABLES = ResourceLocation(id, "send_loot_tables")
    val logger = LogManager.getLogger(id)
}