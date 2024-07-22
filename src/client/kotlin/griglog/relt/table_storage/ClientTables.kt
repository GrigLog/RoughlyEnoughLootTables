package griglog.relt.table_storage

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import griglog.relt.RELT
import griglog.relt.entry_points.ReltClient
import griglog.relt.mixin.LootTableAccessor
import net.minecraft.client.MinecraftClient
import net.minecraft.loot.LootDataType
import net.minecraft.loot.LootTable
import net.minecraft.registry.RegistryOps
import net.minecraft.util.Identifier
import org.apache.commons.lang3.SystemUtils
import java.awt.Desktop
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.GZIPInputStream

val clientTables: HashMap<Identifier, LootTable> = hashMapOf()

fun receiveLootTables(compressed: ByteArray) {
    RELT.logger.info("Recieved loot tables (${compressed.size} bytes).")
    val t1 = System.nanoTime()
    val bytes: ByteArray
    ByteArrayInputStream(compressed).use {
        GZIPInputStream(it).apply { bytes = readAllBytes(); close() }
    }
    clientTables.clear()
    val json = JsonParser.parseString(String(bytes)).asJsonObject
    json.entrySet().forEach { (key, value) ->
        val rl = Identifier.of(key)
        val regOps = RegistryOps.of(JsonOps.INSTANCE, MinecraftClient.getInstance().world!!.registryManager)

        LootDataType.LOOT_TABLES.codec.decode(regOps, value).mapOrElse(
            {
                val table = it.first
                if (table.type !in ReltClient.config.skipTypes && (!ReltClient.config.skipEmptyTables || (table as LootTableAccessor).pools.isNotEmpty()))
                    clientTables[rl] = table
            },
            { error -> RELT.logger.error("Failed to parse loot table $rl: $error") })
    }
    val t2 = System.nanoTime()
    RELT.logger.info(
        "Decompressed loot tables to ${bytes.size} bytes, " +
                "${clientTables.size} saved out of ${json.size()} received. Took ${(t2 - t1) / 1000000} ms."
    )
}


fun openTableJson(name: Identifier) {
    val table = clientTables[name] ?: return

    val regOps = RegistryOps.of(JsonOps.INSTANCE, MinecraftClient.getInstance().world!!.registryManager)
    var json = LootDataType.LOOT_TABLES.codec.encodeStart(regOps, table).mapOrElse({ it }, { RELT.logger.error(it) })

    //add newlines and indentation
    val gson = GsonBuilder().setPrettyPrinting().create()
    json = gson.toJson(JsonParser.parseString(json.toString()))

    val temp = File.createTempFile("RELT_" + name.toString().replace(':', '_').replace('/', '_'), ".json")
    temp.writeText(json.toString(), Charsets.UTF_8)

    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(temp)
    } else {
        RELT.logger.warn("Desktop is not supported. Let's try OS-specific operations.")
        try {
            if (SystemUtils.IS_OS_WINDOWS)
                Runtime.getRuntime().exec(arrayOf("rundll32", "url.dll,FileProtocolHandler", temp.absolutePath))
            else if (SystemUtils.IS_OS_MAC)
                Runtime.getRuntime().exec(arrayOf("/usr/bin/open", temp.absolutePath))
            else if (SystemUtils.IS_OS_LINUX) {
                Runtime.getRuntime().exec(arrayOf("xdg-open", temp.absolutePath))
            } else {
                RELT.logger.error("Unknown operating system. Please report to the mod author.")
                RELT.logger.info("Anyways, here is your loot table:\n" + json.toString())
            }
        } catch (e: Exception) {
            RELT.logger.error("Unable to open file. Please report to the mod author.")
            RELT.logger.info("Anyways, here is your loot table:\n" + json.toString())
        }
    }
    temp.deleteOnExit()
}
