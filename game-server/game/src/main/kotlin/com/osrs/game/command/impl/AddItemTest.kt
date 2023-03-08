package com.osrs.game.command.impl

import com.google.inject.Singleton
import com.osrs.game.actor.player.Player
import com.osrs.game.command.CommandListener
import com.osrs.game.item.FloorItem
import com.osrs.game.world.map.zone.ZoneUpdateRequest.ObjAddRequest

@Singleton
class AddItemTest : CommandListener(
    name = "drop_item"
) {
    override fun execute(command: String, player: Player) {
        val floorItem = FloorItem(995, Int.MAX_VALUE, player.location, 30)

        player.zone.update(
            player,
            ObjAddRequest(
                floorItem = floorItem
            )
        )
    }
}