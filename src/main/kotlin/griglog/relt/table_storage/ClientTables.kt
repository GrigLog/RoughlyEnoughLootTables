package griglog.relt.table_storage

import com.google.gson.JsonParser
import griglog.relt.RELT
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.LootTables
import org.apache.commons.lang3.SystemUtils
import java.awt.Desktop
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.GZIPInputStream

val clientTables: HashMap<ResourceLocation, LootTable> = hashMapOf()
fun recieveLootTables(compressed: ByteArray){
    val t1 = System.nanoTime()
    val bytes: ByteArray
    ByteArrayInputStream(compressed).use{
        GZIPInputStream(it).apply{ bytes = readAllBytes(); close() }
    }
    val json = JsonParser.parseString(String(bytes))
    clientTables.clear()
    json.asJsonObject.entrySet().forEach {(key, value) ->
        val rl = ResourceLocation(key)
        val table = LootTables.GSON.fromJson(value, LootTable::class.java)
        clientTables.put(rl, table)
    }
    val t2 = System.nanoTime()
    RELT.logger.info("Recieved and decompressed ${clientTables.size} loot tables (${bytes.size} bytes). Took ${(t2 - t1) / 1000000} ms.")
}


fun openTableJson(name: ResourceLocation){
    val table = clientTables.get(name) ?: return
    val json = LootTables.GSON.newBuilder().setPrettyPrinting().create().toJson(table)
    val temp = File.createTempFile("RELT_" + name.toString().replace(':', '_').replace('/', '_'), ".json")
    temp.writeText(json.toString(), Charsets.UTF_8)

    if (Desktop.isDesktopSupported()){
        Desktop.getDesktop().open(temp)
    } else{
        RELT.logger.warn("Desktop is not supported. Let's try OS-specific operations.")
        try {
            if (SystemUtils.IS_OS_WINDOWS)
                Runtime.getRuntime().exec(arrayOf("rundll32", "url.dll,FileProtocolHandler", temp.absolutePath))
            else if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_UNIX)
                Runtime.getRuntime().exec(arrayOf("/usr/bin/open", temp.absolutePath))
            else {
                RELT.logger.error("Unknown operating system. Please report to the mod author.")
                RELT.logger.info("Anyways, here is your loot table:\n" + json.toString())
            }
        } catch(e: Exception) {
            RELT.logger.error("Unable to open file. Please report to the mod author.")
            RELT.logger.info("Anyways, here is your loot table:\n" + json.toString())
        }
    }
    temp.deleteOnExit()
}