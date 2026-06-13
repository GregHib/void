package content.skill.magic.book.lunar

import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound

class Dream : Script {

    init {
        interfaceOption("Cast", "lunar_spellbook:dream") {
            val spell = it.component
            if (levels.getOffset(Skill.Constitution) >= 0) {
                message("You have no need to cast this spell since your hitpoints are already full.")
                return@interfaceOption
            }
            if (!removeSpellItems(spell)) {
                return@interfaceOption
            }
            gfx(spell)
            sound(spell)
            exp(Skill.Magic, Tables.int("spells.$spell.xp") / 10.0)
            softTimers.startIfAbsent("restore_hitpoints")
            mode = DreamMode(this)
        }
    }
}

private class DreamMode(private val player: Player) : Mode {

    private var ticks = 0

    override fun start() {
        player["dream"] = true
        ticks = player.anim("dream_start")
    }

    override fun tick() {
        if (player.levels.getOffset(Skill.Constitution) >= 0) {
            player.mode = EmptyMode
            return
        }
        if (--ticks <= 0) {
            ticks = player.anim("dream_middle")
        }
    }

    override fun stop(replacement: Mode) {
        player.clear("dream")
        player.anim("dream_wake")
    }
}
