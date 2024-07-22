package griglog.relt.entry_points

import griglog.relt.CompressedTablesPayload
import griglog.relt.SkipConfig
import griglog.relt.table_storage.onReloadOrServerStart
import griglog.relt.table_storage.serverTablesCache
import griglog.relt.table_storage.tryUpdateLootTables
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

class ReltCommon : ModInitializer {

    override fun onInitialize() {
        PayloadTypeRegistry.playS2C().register(CompressedTablesPayload.TYPE, CompressedTablesPayload.CODEC)

        ServerLifecycleEvents.SERVER_STARTING.register { onReloadOrServerStart() }
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { _, _, _ -> onReloadOrServerStart() }
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register { player, _ ->
            tryUpdateLootTables(player.server)
            ServerPlayNetworking.send(player, CompressedTablesPayload(serverTablesCache))
        }
    }

    companion object {
        val config = SkipConfig("server")
    }
}