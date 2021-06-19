import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Died
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.npc.turn
import world.gregs.voidps.engine.entity.character.update.visual.player.move
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.getOrNull
import world.gregs.voidps.engine.entity.stopAllEffects
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Area
import world.gregs.voidps.utility.func.toUnderscoreCase
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.sound.playSound

val npcs: NPCs by inject()

on<Died> { npc: NPC ->
    npc.action(ActionType.Death) {
        delay(2)
        val name = npc.def["category", npc.def.name.toUnderscoreCase()]
        val killer: Player? = npc.getOrNull("killer")
        killer?.playSound("${name}_death", delay = 40)
        npc.playAnimation("${name}_death")
        npc.stopAllEffects()
        npcs.remove(npc)
        val area: Area? = npc.getOrNull("area")
        if (area != null) {
            delay(npc["respawn_delay", 60])
            var tile = area.random(npc.movement.traversal)
            var increment = 1
            while (tile == null) {
                delay(increment++)
                tile = area.random(npc.movement.traversal)
                if (increment > 10) {
                    break
                }
            }
            if (tile != null) {
                npc.levels.clear()
                npc.move(tile)
                npc.turn(npc["respawn_direction", Direction.NORTH], update = false)
                npcs.add(npc)
            }
        }
    }
}