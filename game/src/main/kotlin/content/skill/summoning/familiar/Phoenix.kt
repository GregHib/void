package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.skill.summoning.FamiliarSpecialMoves
import content.skill.summoning.familiarSpecialHit
import content.skill.summoning.follower
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.random
import kotlin.math.abs

/** The offensive stats Rise from the Ashes sears away from its target. */
private val RISE_DRAIN_SKILLS = arrayOf(Skill.Attack, Skill.Magic, Skill.Ranged)

/** The seared stats return after ~6 seconds, unlike a normal decaying drain. */
private const val RISE_DRAIN_TICKS = 10

/** The blast hits up to the phoenix's own magic max. */
private const val RISE_MAX_HIT = 160

class Phoenix : Script {
    init {
        // A plain click on the cast button has no target - point at both triggers.
        FamiliarSpecialMoves.instant("phoenix_familiar") {
            message("Cast Rise from the Ashes on a foe, or on ashes to rejuvenate the phoenix.")
            false
        }

        // Rise from the Ashes - a searing blast that halves the target's Attack, Magic and Ranged
        // for a few seconds. Neither reference implements this (darkan's is a TODO stub), so the
        // mechanics adapt the wiki: magic damage plus a severe short-lived offensive drain.
        FamiliarSpecialMoves.npc("phoenix_familiar") { target -> riseFromTheAshes(target) }
        FamiliarSpecialMoves.player("phoenix_familiar") { target -> riseFromTheAshes(target) }

        // Cast on ashes instead, the phoenix is reborn: the ashes burn away and its wounds with them.
        FamiliarSpecialMoves.item("phoenix_familiar") { item ->
            if (item.id != "ashes") {
                message("The phoenix can only rise from ashes.")
                return@item false
            }
            val phoenix = follower ?: return@item false
            if (!inventory.remove("ashes")) {
                return@item false
            }
            phoenix.anim("phoenix_spawn")
            phoenix.levels.restore(Skill.Constitution, phoenix.levels.getMax(Skill.Constitution))
            message("Your phoenix rises from the ashes, its wounds burning away.", ChatType.Filter)
            true
        }

        npcOperate("Interact", "phoenix_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Skreee skree skrooo skrooooouuu. (I want to burn something.)")
                    player<Happy>("Why are you looking at me like that?")
                    npc<Neutral>("Skeeeeooouoou! Skree skrooo, skrooouuee skreee! (Please! It won't hurt that much, and I'll bring you back straight away!)")
                    player<Happy>("Maybe later. Much later. When I'm dead from natural causes already. And medicine has failed to bring me back.")
                    npc<Neutral>("Skreee skreeeooouu skroou! (I'll hold you to it!)")
                }
                1 -> {
                    npc<Neutral>("May I ask you a question?")
                    player<Happy>("Skreeoooouuu, skreeee skreeeeoooo. (Yes, but you have already asked me a question.)")
                    player<Happy>("Skreeeooo, skreee skreeeeee skreeoooo. (You should have said 'May I ask you two questions?'.)")
                    npc<Neutral>("Erm, may I ask you two questions?")
                    player<Happy>("Skroo. (No.)")
                    npc<Neutral>("...")
                }
                2 -> {
                    player<Happy>("May I ask you... TWO questions?")
                    npc<Neutral>("Skree ree ree! Skree, skreee skrooou skreeeoou. (Heh heh heh. The answer to your first is yes. You may ask your second.)")
                    player<Happy>("What was RuneScape like in the distant past?")
                    npc<Neutral>("Skreee skreeeeout skreeou. Skreee skree. (It was like it is now, only younger.)")
                    player<Happy>("...")
                    player<Happy>("You, madam, are the most pestiferous poultry I have ever met.")
                    npc<Neutral>("Skree ree ree! (Heh heh heh!)")
                }
                3 -> {
                    player<Happy>("Skreeee, skree skrooo. Skrooooou skreee!")
                    npc<Neutral>("Skreee skroooue, skreeee skreeeeeeeou. (Either you need to practice your phoenixspeak, or I should burn you where you stand.)")
                    player<Happy>("So that didn't mean 'How are you feeling today?'")
                    npc<Neutral>("Skroo. Skroo, skreee skreou. (No, it didn't.")
                }
            }
        }
    }

    /**
     * The searing blast: magic damage with the phoenix's own attack animation, then half the
     * target's offensive stats burn away, returning once the flames die down.
     */
    private fun Player.riseFromTheAshes(target: Character): Boolean {
        val cast = familiarSpecialHit(target, maxHit = RISE_MAX_HIT, anim = "phoenix_familiar_attack")
        if (cast) {
            val drained = RISE_DRAIN_SKILLS.associateWith { abs(target.levels.drain(it, multiplier = 0.5)) }
            target.queue("rise_from_the_ashes_restore", RISE_DRAIN_TICKS) {
                // Give back exactly what was seared - restore() caps at the base level, which would
                // swallow the returned stats of anything fighting above it (boosts, set levels).
                for ((skill, amount) in drained) {
                    target.levels.set(skill, target.levels.get(skill) + amount)
                }
            }
        }
        return cast
    }
}
