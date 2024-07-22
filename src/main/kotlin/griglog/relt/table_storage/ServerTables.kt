package griglog.relt.table_storage

import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import griglog.relt.RELT
import griglog.relt.entry_points.ReltCommon
import griglog.relt.mixin.LootTableAccessor
import net.minecraft.loot.LootDataType
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryOps
import net.minecraft.server.MinecraftServer
import org.apache.commons.lang3.ArrayUtils
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

var serverTablesCache: ByteArray = ArrayUtils.EMPTY_BYTE_ARRAY
var serverCacheValid = true

fun tryUpdateLootTables(server: MinecraftServer) {
    if (serverCacheValid) {
        return
    }
    val reloadableRegistries = server.reloadableRegistries
    val obj = JsonObject()
    val t1 = System.nanoTime()
    reloadableRegistries.getIds(RegistryKeys.LOOT_TABLE).forEach { rl ->
        val table = reloadableRegistries.getLootTable(RegistryKey.of(RegistryKeys.LOOT_TABLE, rl))
        if ((ReltCommon.config.skipEmptyTables && (table as LootTableAccessor).pools.isEmpty()) || table.type in ReltCommon.config.skipTypes) {
            return@forEach
        }

        val regOps = RegistryOps.of(JsonOps.INSTANCE, server.registryManager)
        LootDataType.LOOT_TABLES.codec.encodeStart(regOps, table).mapOrElse(
            { json -> obj.add(rl.toString(), json) },
            { error -> RELT.logger.error("Failed to serialize loot table $rl: $error") })
    }

    val str = obj.toString().toByteArray()
    val t2 = System.nanoTime()

    RELT.logger.info("Collected ${obj.size()} loot tables, ${str.size} bytes in total. Took ${(t2 - t1) / 1000000} ms.")
    val t3 = System.nanoTime()
    ByteArrayOutputStream().use {
        GZIPOutputStream(it).apply { write(str); close() }
        serverTablesCache = it.toByteArray()
    }
    val t4 = System.nanoTime()
    RELT.logger.info("Loot tables compressed to ${serverTablesCache.size} bytes. Took ${(t4 - t3) / 1000000} ms.")
    serverCacheValid = true
}

fun onReloadOrServerStart() {
    serverCacheValid = false
}
