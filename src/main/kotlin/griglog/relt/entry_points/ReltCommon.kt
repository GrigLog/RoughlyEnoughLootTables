package griglog.relt.entry_points

import griglog.relt.RELT
import griglog.relt.table_storage.onReloadOrServerStart
import griglog.relt.table_storage.serverTablesCache
import griglog.relt.table_storage.tryUpdateLootTables
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

class ReltCommon : ModInitializer{
    override fun onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register{ server -> onReloadOrServerStart() }
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register{ server, rm, success -> onReloadOrServerStart() }
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register{ player, joined ->
            tryUpdateLootTables(player.server)
            ServerPlayNetworking.send(player,
                RELT.SEND_LOOT_TABLES, PacketByteBufs.create().writeByteArray(serverTablesCache))
        }
    }
}