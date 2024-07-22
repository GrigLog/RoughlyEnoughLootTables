package griglog.relt.entry_points

import griglog.relt.CompressedTablesPayload
import griglog.relt.SkipConfig
import griglog.relt.table_storage.receiveLootTables
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

class ReltClient : ClientModInitializer {
    override fun onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(CompressedTablesPayload.TYPE) { payload, context ->
            context.client().execute { receiveLootTables(payload.tables) }
        }
        System.setProperty(
            "java.awt.headless",
            "false"
        ) //it does not help if something else calls GraphicsEnvironment.isHeadless() first
    }

    companion object {
        val config = SkipConfig("client")
    }
}
