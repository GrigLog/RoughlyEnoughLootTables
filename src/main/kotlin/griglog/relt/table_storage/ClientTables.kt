package griglog.relt.table_storage

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonWriter
import com.mojang.serialization.JsonOps
import griglog.relt.RELT
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.LootTables
import java.awt.Desktop
import java.awt.HeadlessException
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileWriter
import java.lang.reflect.Type
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
    try {
        Desktop.getDesktop().open(temp)
    } catch (e: HeadlessException){
        RELT.logger.error("Unable to open loot table json file: Headless Exception (desktop is not supported)")
    }
    temp.deleteOnExit()
}