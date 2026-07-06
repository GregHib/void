package content.skill.summoning.familiar

import content.entity.combat.target
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Frustrated
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.skill.summoning.familiarSpecialHit
import content.skill.summoning.follower
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.random

class SmokeDevil : Script {
    init {
        // "Flames" makes the devil instantly scorch the owner's current foe with its magic-based
        // fire attack, at the cost of two summoning points.
        npcOperate("Flames", "smoke_devil_familiar") { (clicked) ->
            if (clicked != follower) {
                return@npcOperate
            }
            val enemy = (follower?.target ?: target) as? NPC
            if (enemy == null) {
                message("Your familiar has no target to attack.")
                return@npcOperate
            }
            if (levels.get(Skill.Summoning) < 2) {
                message("You do not have enough summoning points to do that.")
                return@npcOperate
            }
            val cast = familiarSpecialHit(
                enemy,
                maxHit = 150,
                anim = "smoke_devil_familiar_attack",
                projectile = "smoke_devil_familiar_flames_projectile",
            )
            if (cast) {
                levels.drain(Skill.Summoning, 2)
            }
        }

        npcOperate("Interact", "smoke_devil_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("When are you going to be done with that?")
                    player<Happy>("Soon, I hope.")
                    npc<Neutral>("Good, because this place is too breezy.")
                    player<Happy>("What do you mean?")
                    npc<Neutral>("I mean, it's tricky to keep hovering in this draft.")
                    player<Happy>("Ok, we'll move around a little if you like.")
                    npc<Neutral>("Yes please!")
                }
                1 -> {
                    npc<Neutral>("Hey!")
                    player<Happy>("Yes?")
                    npc<Neutral>("Where are we going again?")
                    player<Happy>("Well, I have a lot of things to do today, so we might go a lot of places.")
                    npc<Neutral>("Are we there yet?")
                    player<Happy>("No, not yet.")
                    npc<Neutral>("How about now?")
                    player<Frustrated>("No.")
                    npc<Neutral>("Are we still not there?")
                    player<Angry>("NO!")
                    npc<Neutral>("Okay, just checking.")
                }
                2 -> {
                    npc<Neutral>("Ah, this is the life!")
                    player<Happy>("Having a good time up there?")
                    npc<Neutral>("Yeah! It's great to feel the wind in your tentacles.")
                    player<Happy>("Sadly, I don't know what that feels like.")
                    npc<Neutral>("Why not?")
                    player<Happy>("No tentacles for a start.")
                    npc<Neutral>("Well, nobody's perfect.")
                }
                3 -> {
                    npc<Neutral>("Why is it always so cold here?")
                    player<Happy>("I don't think it's that cold.")
                    npc<Neutral>("It is compared to back home.")
                    player<Happy>("How hot is it where you are from?")
                    npc<Neutral>("I can never remember. What is the vaporisation point of steel again?")
                    player<Happy>("Pretty high.")
                    player<Happy>("No wonder you feel cold here...")
                }
            }
        }
    }
}
