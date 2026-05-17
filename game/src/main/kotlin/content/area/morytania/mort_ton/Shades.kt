package content.area.morytania.mort_ton

import content.entity.effect.transform
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.queue.queue

class Shades : Script {
    init {
        npcCombatDamage("loar_shadow") { (source) ->
            if (transform != "") {
                return@npcCombatDamage
            }
            anim("shade_rise")
            areaSound("shade_appear", tile, radius = 10)
            source.mode = EmptyMode
            mode = PauseMode
            transform(id.replace("_shadow", "_shade"))
            queue("shade_transform", 1) {
                if (source is Player) {
                    interactPlayer(source, "Attack")
                }
            }
        }
    }
}
