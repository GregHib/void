package content.skill.summoning.familiar

import content.entity.combat.hit.hit
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

/** One in [FEROCIOUS_CHANCE] of the spirit dagannoth's melee attacks strikes twice. */
private const val FEROCIOUS_CHANCE = 5

/** The extra ferocious strike rolls up to the dagannoth's own melee max. */
private const val FEROCIOUS_MAX_HIT = 108

class SpiritDagannoth : Script {
    init {
        // Ferocious - about a fifth of the dagannoth's melee attacks lash out a second time.
        npcCombatAttack("spirit_dagannoth_familiar") { attack ->
            if (attack.type != "melee" || this["ferocious", false] || random.nextInt(FEROCIOUS_CHANCE) != 0) {
                return@npcCombatAttack
            }
            // The extra hit re-fires this handler - the flag keeps it from chaining.
            this["ferocious"] = true
            anim("ferocious")
            gfx("ferocious")
            hit(attack.target, offensiveType = "melee", damage = random.nextInt(FEROCIOUS_MAX_HIT + 1), delay = attack.delay + 30)
            clear("ferocious")
        }

        npcOperate("Interact", "spirit_dagannoth_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Grooooooowl graaaaawl raaaawl? (Are you ready to surrender to the power of the Deep Waters?)")
                    player<Happy>("Err, not really.")
                    npc<Neutral>("Rooooowl? (How about now?)")
                    player<Happy>("No, sorry.")
                    npc<Neutral>("Rooooowl? (How about now?)")
                    player<Happy>("No, sorry. You might want to try again a little later.")
                }
                1 -> {
                    npc<Neutral>("Groooooowl. Hsssssssssssssss! (The Deeps will swallow the lands. None will stand before us!)")
                    player<Happy>("What if we build boats?")
                    npc<Neutral>("Hsssssssss groooooowl? Hssssshsss grrooooooowl? (What are boats? The tasty wooden containers full of meat?)")
                    player<Happy>("I suppose they could be described as such, yes.")
                }
                2 -> {
                    npc<Neutral>("Hssssss graaaawl grooooowl, growwwwwwwwwl! (Oh how the bleak gulfs hunger for the Day of Rising.)")
                    player<Happy>("My brain hurts when I listen to you talk...")
                    npc<Neutral>("Raaaaawl groooowl grrrrawl! (That's the truth biting into your clouded mind!)")
                    player<Happy>("Could you try using a little less truth please?")
                }
                3 -> {
                    npc<Neutral>("Raaaawl! (Submit!)")
                    player<Happy>("Submit to what?")
                    npc<Neutral>("Hssssssssss rawwwwwl graaaawl! (To the inevitable defeat of all life on the Surface!)")
                    player<Happy>("I think I'll wait a little longer before I just keep over and submit, thanks")
                    npc<Neutral>("Hsssss, grooooowl, raaaaawl. (Well, it's your choice, but those that submit first will be eaten first.)")
                    player<Happy>("I'll pass on that one, thanks.")
                }
            }
        }
    }
}
