package griglog.relt.table_storage

import com.google.gson.JsonObject
import griglog.relt.RELT
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.loot.LootTables
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.*
import org.apache.commons.lang3.ArrayUtils
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

var serverTablesCache: ByteArray = ArrayUtils.EMPTY_BYTE_ARRAY
var serverCacheValid = true
private val skipTypes = setOf(BLOCK, ENTITY, COMMAND)
fun tryUpdateLootTables(server: MinecraftServer) {
    if (serverCacheValid)
        return
    val obj = JsonObject()
    server.lootTables.tables.forEach { rl, table ->
        val type = LootContextParamSets.getKey(table.paramSet)
        if (table.paramSet in skipTypes)
            return@forEach
        val json = LootTables.GSON.toJsonTree(table)
        obj.add(rl.toString(), json)
    }
    /*
    for (i in 0..50) {
        val test: LootTable = LootTable.lootTable().pool(LootPool.lootPool().also {
            for (item in BuiltInRegistries.ITEM) {
                it.with(LootItem.lootTableItem(item).build())
            }
        }.build()).build()
        obj.add(RELT.id + ":big_test" + i, LootTables.GSON.toJsonTree(test))
    }*/
    val str = obj.toString()
    //RELT.logger.info(str)
    ByteArrayOutputStream().use {
        GZIPOutputStream(it).apply { write(str.toByteArray()); close() }
        serverTablesCache = it.toByteArray()
    }
    RELT.logger.info("Loot tables compressed: ${serverTablesCache.size} bytes.")
    serverCacheValid = true
}

fun onReloadOrServerStart(){
    serverCacheValid = false
}
