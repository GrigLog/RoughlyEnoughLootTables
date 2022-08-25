package griglog.relt.table_storage

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import griglog.relt.RELT
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.loot.LootTables
import org.apache.commons.lang3.ArrayUtils
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

var serverTablesCache: ByteArray = ArrayUtils.EMPTY_BYTE_ARRAY
var serverCacheValid = true

fun tryUpdateLootTables(server: MinecraftServer) {
    if (serverCacheValid)
        return
    val arr = JsonArray()
    server.lootTables.tables.forEach { rl, table ->
        val obj = JsonObject()
        val json = LootTables.GSON.toJsonTree(table)
        obj.add(rl.toString(), json)
        arr.add(obj)
        RELT.logger.info(rl)
        RELT.logger.info(json)
    }
    val str= arr.toString()
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