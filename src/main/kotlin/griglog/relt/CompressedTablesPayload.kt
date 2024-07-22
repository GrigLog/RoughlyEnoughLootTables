package griglog.relt

import griglog.relt.RELT.MOD_ID
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

class CompressedTablesPayload(val tables: ByteArray) : CustomPayload {
    companion object {
        val TYPE = CustomPayload.Id<CompressedTablesPayload>(Identifier.of(MOD_ID, "send_loot_tables"))
        val CODEC: PacketCodec<RegistryByteBuf, CompressedTablesPayload> =
            PacketCodec.tuple(PacketCodecs.BYTE_ARRAY, { it.tables }, { CompressedTablesPayload(it) })
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return TYPE
    }
}
