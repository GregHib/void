package content.skill.magic

import com.github.michaelbull.logging.InlineLogger
import content.skill.magic.spell.spell
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.player.name

class CombatSpellsOnPlayer : Script {
    private val logger = InlineLogger()

    init {
        Approachable.onPlayer.getOrPut("*") { mutableListOf() }.add { interact ->
            val id = interact.id
            if (!id.startsWith("192:") &&!id.startsWith("193:") &&!id.startsWith("194:") &&!id.startsWith("430:")) return@add
            if (hasClock("action_delay")) return@add

            val parts = id.split(":")
            val ifaceId = parts[0].toInt()
            val compId = parts[1].toInt()

            val defs = InterfaceDefinitions.definitions
            if (ifaceId >= defs.size) return@add
            val def = defs[ifaceId]
            val component = def.components?.get(compId)?: return@add
            val spell = component.stringId

            if (component.params?.get(Params.id("cast_id")) == null) return@add

            logger.debug { "Spell $id ($spell) on ${interact.target.name}" }
            approachRange(10)
            this.spell = spell
            set("one_time", true)

            start("action_delay", 4)
            Magic.castSpell(this@add, interact.target)

            // End Interact, let Combat mode handle subsequent casts
            if (mode == interact) {
                mode = EmptyMode
            }
        }
    }
}