package com.osrs.game.world

import com.osrs.game.actor.PlayerList

class World(
    val worldId: Int
) {
    val players = PlayerList(MAX_PLAYERS)

    companion object {
        const val MAX_PLAYERS = 2048
        const val MAX_NPCS = Short.MAX_VALUE.toInt()
    }
}