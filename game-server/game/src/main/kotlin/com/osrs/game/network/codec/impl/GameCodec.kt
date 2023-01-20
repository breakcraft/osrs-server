package com.osrs.game.network.codec.impl

import com.github.michaelbull.logging.InlineLogger
import com.google.inject.Inject
import com.osrs.common.buffer.readPacketOpcode
import com.osrs.common.buffer.readPacketSize
import com.osrs.game.network.Session
import com.osrs.game.network.codec.CodecChannelHandler
import com.osrs.game.network.packet.Packet
import com.osrs.game.network.packet.PacketGroup
import com.osrs.game.network.packet.client.handler.PacketHandler
import com.osrs.game.network.packet.client.reader.PacketReader
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlin.reflect.KClass

class GameCodec @Inject constructor(
    environment: ApplicationEnvironment,
    private val packetReaders: Set<PacketReader<Packet>>,
    private val packetHandlers: Map<KClass<*>, PacketHandler<Packet>>
) : CodecChannelHandler {
    private val logger = InlineLogger()
    private val packetSizes = environment.config.property("game.packet.sizes").getList().map(String::toInt)
    private val mappedPacketReaders = packetReaders.associateBy { it.opcode }

    override suspend fun handle(session: Session, readChannel: ByteReadChannel, writeChannel: ByteWriteChannel) = coroutineScope {
        val (clientCipher, _) = session.getIsaacCiphers()
        try {
            while (this.isActive) {
                if (readChannel.isClosedForRead) break
                val opcode = readChannel.readPacketOpcode(clientCipher)

                if (opcode < 0 || opcode >= packetSizes.size) continue
                val size = readChannel.readPacketSize(packetSizes[opcode])

                val reader = mappedPacketReaders[opcode]

                logger.info { "Incoming client packet. Opcode: $opcode." }

                if (reader == null) {
                    readChannel.discard(size.toLong())
                    continue
                }

                if (reader.size != -1 && reader.size != size) {
                    logger.debug { "Packet size miss-match. Packet Reader size was ${reader.size} and server size was $size." }
                    // Discard the bytes from the read channel.
                    readChannel.discard(readChannel.availableForRead.toLong())
                    continue
                }

                val packet = reader.read(readChannel, size)

                if (packet == null) {
                    readChannel.discard(readChannel.availableForRead.toLong())
                    continue
                }

                val handler = packetHandlers[packet::class]

                if (handler == null) {
                    logger.debug { "Packet ${packet::class.qualifiedName} has no handler." }
                    continue
                }

                val player = session.player

                if (player == null) {
                    session.disconnect("Session player was not set!")
                    break
                }

                player.addToPacketGroup(PacketGroup(packet, handler))
            }
        } catch (exception: Exception) {
            session.handleSessionException(exception)
        }
    }
}
