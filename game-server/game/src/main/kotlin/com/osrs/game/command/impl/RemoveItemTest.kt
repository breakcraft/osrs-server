package com.osrs.game.command.impl

import com.google.inject.Singleton
import com.osrs.common.map.location.Location
import com.osrs.game.actor.player.Player
import com.osrs.game.command.CommandListener
import com.osrs.game.item.FloorItem
import com.osrs.game.world.map.zone.ZoneUpdateRequest.ObjRemoveRequest

@Singleton
class RemoveItemTest : CommandListener(
    name = "remove_item"
) {
    override fun execute(command: String, player: Player) {
        player
            .world.
            zone(Location(3222, 3222, 0)).
            update(ObjRemoveRequest(FloorItem(995, Int.MAX_VALUE, Location(3222, 3222, 0))))

        player
            .world
            .zone(Location(3222, 3224, 0))
            .update(
                ObjRemoveRequest(
                    FloorItem(
                        4151,1,
                        Location(3222, 3224, 0)
                    ),
                )
            )
    }
}
