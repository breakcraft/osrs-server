package com.osrs.game.network.packet.server.builder.impl.sync.block

import com.osrs.game.actor.Actor
import com.osrs.game.actor.render.RenderType
import io.ktor.utils.io.core.ByteReadPacket

abstract class RenderingBlock<T : Actor, out R : RenderType>(val index: Int, val mask: Int) {
    abstract fun build(actor: T, render: @UnsafeVariance R): ByteReadPacket
}