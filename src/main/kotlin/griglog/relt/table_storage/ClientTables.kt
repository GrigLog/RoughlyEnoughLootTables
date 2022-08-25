package griglog.relt.table_storage

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import griglog.relt.RELT
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.LootTables
import java.io.ByteArrayInputStream
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
    for (obj in json.asJsonArray){
        (obj as JsonObject).entrySet().forEach{(key, value) ->
        //foreach only called once
        val rl = ResourceLocation(key)
        val table = LootTables.GSON.fromJson(value, LootTable::class.java)
        clientTables.put(rl, table) }
    }
    val t2 = System.nanoTime()
    RELT.logger.info("Recieved and decompressed ${json.asJsonArray.size()} loot tables (${bytes.size} bytes). Took ${(t2 - t1) / 1000000} ms.")
}