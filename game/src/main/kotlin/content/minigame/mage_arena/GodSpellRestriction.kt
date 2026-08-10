package content.minigame.mage_arena

import content.skill.magic.spell.spell
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas

class GodSpellRestriction : Script {

    val godSpells = setOf("saradomin_strike", "claws_of_guthix", "flames_of_zamorak")

    init {
        combatPrepare(style = "magic") { _ ->
            val spell = spell
            if (spell in godSpells && get("${spell}_casts", 0) < CASTS_REQUIRED && tile !in Areas["mage_arena"]) {
                val remaining = CASTS_REQUIRED - get("${spell}_casts", 0)
                message("You need to cast ${spell.toSentenceCase()} $remaining more times inside the Mage Arena.")
                clear("spell")
                false
            } else {
                true
            }
        }

        combatAttack("magic") { attack ->
            if (attack.spell in godSpells && inc("${attack.spell}_casts") == CASTS_REQUIRED) {
                message("You can now cast ${attack.spell.toSentenceCase()} outside the Arena.")
            }
        }
    }

    companion object {
        const val CASTS_REQUIRED = 100
    }
}
