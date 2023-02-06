package com.osrs.game.ui

import com.osrs.cache.entry.config.enum.EnumTypeProvider
import com.osrs.common.ui.InterfaceInfo
import com.osrs.common.ui.InterfaceInfoMap
import com.osrs.common.util.packInterface
import com.osrs.game.actor.player.Player
import com.osrs.game.network.packet.type.server.IfOpenSubPacket
import com.osrs.game.network.packet.type.server.IfOpenTopPacket
import com.osrs.game.ui.InterfaceLayout.RESIZABLE

class Interfaces constructor(
    val player: Player,
    private val interfaceInfoMap: InterfaceInfoMap,
    private val enums: EnumTypeProvider,
    private val open: MutableList<UserInterface> = mutableListOf()
) : MutableList<UserInterface> by open {
    var layout: InterfaceLayout = RESIZABLE
        set(value) {
            field = value
            sendInterfaceLayout(value)
        }

    fun sendInterfaceLayout(layout: InterfaceLayout) = player.write(IfOpenTopPacket(layout.interfaceId))

    operator fun plusAssign(userInterface: UserInterface) {
        val info = interfaceInfoMap[userInterface.name] ?: return

        val childId = info.resizableChildId.enumChildForLayout(layout)

        open += userInterface

        player.write(
            IfOpenSubPacket(
                interfaceId = info.id,
                toInterface = layout.interfaceId.packInterface(childId),
                isModal = info.isModal()
            )
        )
    }

    private fun InterfaceInfo.isModal() = resizableChildId == MODAL_CHILD_ID || resizableChildId == MODAL_CHILD_ID_EXTENDED

    private fun Int.enumChildForLayout(layout: InterfaceLayout): Int =
        (enums[layout.enumId]?.params?.get(RESIZABLE.interfaceId.packInterface(this)) as? Int)?.and(0xffff) ?: 0

    companion object {
        const val MODAL_CHILD_ID = 16
        const val MODAL_CHILD_ID_EXTENDED = 17
        const val INVENTORY_CHILD_ID = 73
    }
}