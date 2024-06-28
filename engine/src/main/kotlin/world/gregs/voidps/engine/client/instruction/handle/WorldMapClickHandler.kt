package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.WorldMapClick

class WorldMapClickHandler : InstructionHandler<WorldMapClick>() {

    override fun validate(player: Player, instruction: WorldMapClick) {
        if (player.hasClock("world_map_double_click") && player["previous_world_map_click", 0] == instruction.tile) {
            player["world_map_marker_custom"] = instruction.tile
        }
        player["previous_world_map_click"] = instruction.tile
        player.start("world_map_double_click", 1)
    }

}