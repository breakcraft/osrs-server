package com.osrs.network.codec.impl

import com.osrs.cache.Cache
import com.osrs.network.Session
import com.osrs.network.codec.ByteChannelCodec
import com.osrs.network.io.readUMedium
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import java.nio.ByteBuffer

class Js5Codec constructor(
    private val session: Session,
    private val cache: Cache,
) : ByteChannelCodec {

    override suspend fun handle(readChannel: ByteReadChannel, writeChannel: ByteWriteChannel) = coroutineScope {
        try {
            while (this.isActive) {
                when (val opcode = readChannel.readByte().toInt()) {
                    JS5_HIGH_PRIORITY_OPCODE, JS5_LOW_PRIORITY_OPCODE -> {
                        val uid = readChannel.readUMedium()
                        val indexId = uid shr 16
                        val groupId = uid and 0xFFFF
                        val masterRequest = indexId == 0xFF && groupId == 0xFF
                        ByteBuffer.wrap(if (masterRequest) cache.checksums else cache.groupReferenceTable(indexId, groupId)).apply {
                            if (capacity() == 0 || limit() == 0) return@coroutineScope
                            val compression = if (masterRequest) 0 else get().toInt() and 0xff
                            val size = if (masterRequest) cache.checksums.size else int
                            writeChannel.writeJs5ResponseAndFlush(indexId, groupId, compression, size, this)
                        }
                    }
                    JS5_ENCRYPTION_OPCODE, JS5_SWITCH_OPCODE, JS5_LOGGED_IN_OPCODE -> readChannel.discard(3)
                    else -> throw IllegalStateException("Unhandled Js5 opcode. Opcode: $opcode")
                }
            }
        } catch (exception: Exception) {
            session.handleSessionException(exception)
        }
    }

    private suspend fun ByteWriteChannel.writeJs5ResponseAndFlush(indexId: Int, groupId: Int, compression: Int, size: Int, buffer: ByteBuffer) {
        writeByte(indexId.toByte())
        writeShort(groupId.toShort())
        writeByte(compression.toByte())
        writeInt(size)

        var writeOffset = 8
        repeat(if (compression != 0) size + 4 else size) {
            if (writeOffset % 512 == 0) {
                writeByte(0xff.toByte())
                writeOffset = 1
            }
            writeByte(buffer[it + buffer.position()])
            writeOffset++
        }

        flush()
    }

    private companion object {
        const val JS5_HIGH_PRIORITY_OPCODE = 0
        const val JS5_LOW_PRIORITY_OPCODE = 1
        const val JS5_LOGGED_IN_OPCODE = 2
        const val JS5_SWITCH_OPCODE = 3
        const val JS5_ENCRYPTION_OPCODE = 4
    }
}
