package content.skill.magic.book.modern

import content.skill.magic.spell.removeSpellItems
import content.skill.magic.spell.spell
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.queue.queue

class TeleportOther : Script {
    init {
        onPlayerApproach("modern_spellbook:teleother*") { (target, id) ->
            steps.clear()
            face(target)
            if (hasClock("action_delay")) {
                return@onPlayerApproach
            }
            if (!target["accept_aid", true]) {
                message("That player won't let you teleport them.")
                return@onPlayerApproach
            }
            if (target.menu != null || target.dialogue != null || target.mode != EmptyMode || contains("delay")) {
                message("That player is busy.")
                return@onPlayerApproach
            }

            val spell = id.substringAfter(":")
            if (!removeSpellItems(spell)) {
                return@onPlayerApproach
            }
            anim("teleport_other")
            gfx("teleport_other_casting")
            areaSound("tele_other_cast", tile = tile, radius = 1)
            exp(Skill.Magic, Tables.int("spells.$spell.xp") / 10.0)
            start("action_delay", 3)
            val location = spell.removePrefix("teleother_")
            target.interfaces.sendText("teleport_other", "name", name)
            target.interfaces.sendText("teleport_other", "location", location.toSentenceCase())
            target["tele_other_target"] = location
            target.open("teleport_other")
        }

        continueDialogue("teleport_other:continue") {
            val location: String = get("tele_other_target") ?: return@continueDialogue
            closeInterfaces()
            if (queue.contains("teleport")) {
                return@continueDialogue
            }
            queue("teleport", onCancel = null) {
                areaSound("teleport", tile = tile, radius = 10)
                anim("teleport_other_impact")
                gfx("teleport_other_impact")
                delay(3)
                clearAnim()
                tele(Areas["${location}_teleport"])
            }
        }

        combatPrepare("magic") {
            !spell.startsWith("teleother")
        }
    }
}
