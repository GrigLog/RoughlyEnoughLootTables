package griglog.relt
import griglog.relt.table_storage.*
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager

object RELT{
    const val id = "roughly_enough_loot_tables"
    val logger = LogManager.getLogger(id)


    fun commonInit(){
        serverInit() //yeah, makes 100% sense...
    }

    val SEND_LOOT_TABLES = ResourceLocation(id, "send_loot_tables")
    fun clientInit(){
        ClientPlayNetworking.registerGlobalReceiver(SEND_LOOT_TABLES){client, handler, buf, response ->
            val bytes = buf.readByteArray()
            client.execute { recieveLootTables(bytes) }
        }
    }

    fun serverInit(){
        ServerLifecycleEvents.SERVER_STARTING.register{ server -> onReloadOrServerStart() }
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register{ server, rm, success -> onReloadOrServerStart() }
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register{ player, joined ->
            tryUpdateLootTables(player.server)
            ServerPlayNetworking.send(player, SEND_LOOT_TABLES, PacketByteBufs.create().writeByteArray(serverTablesCache))
        }
    }
}