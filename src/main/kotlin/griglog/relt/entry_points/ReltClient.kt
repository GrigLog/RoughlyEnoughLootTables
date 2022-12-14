package griglog.relt.entry_points

import griglog.relt.RELT
import griglog.relt.table_storage.recieveLootTables
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import java.awt.Desktop
import java.awt.GraphicsEnvironment

class ReltClient : ClientModInitializer {
    override fun onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(RELT.SEND_LOOT_TABLES){ client, handler, buf, response ->
            val bytes = buf.readByteArray()
            client.execute { recieveLootTables(bytes) }
        }
        System.setProperty("java.awt.headless", "false") //it does not help if something else calls GraphicsEnvironment.isHeadless() first
    }
}