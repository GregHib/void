package content.skill.summoning.familiar

import content.entity.combat.hit.hit
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.skill.summoning.FamiliarSpecialMoves
import content.skill.summoning.follower
import content.skill.summoning.nearbyAttackableNpcs
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.random

/** How long the phoenix drops in place and glows before reappearing atop the ashes. */
private const val RISE_REBIRTH_DELAY = 3

/** How many enemies around the ashes the rebirth flare can scorch. */
private const val RISE_MAX_TARGETS = 5

/** How long the reborn phoenix stands over the spent ashes before falling back in behind its owner. */
private const val RISE_STAND_TICKS = 4

class Phoenix : Script {
    init {
        // A plain click on the cast button has no target - point at the trigger.
        FamiliarSpecialMoves.instant("phoenix_familiar") {
            message("Cast Rise from the Ashes on a pile of ashes on the ground.")
            false
        }

        FamiliarSpecialMoves.floorItem("phoenix_familiar") { ashes ->
            if (ashes.id != "ashes") {
                message("The phoenix can only rise from ashes.")
                return@floorItem false
            }
            val phoenix = follower ?: return@floorItem false
            // The rebirth's fury: a quarter of the life points the phoenix was missing.
            val maxHit = (phoenix.levels.getMax(Skill.Constitution) - phoenix.levels.get(Skill.Constitution)) / 4
            phoenix.anim("phoenix_familiar_death")
            phoenix.gfx("summon_familiar_size_${phoenix.size}")
            queue("rise_from_the_ashes", RISE_REBIRTH_DELAY) {
                FloorItems.remove(ashes)
                phoenix.tele(ashes.tile, clearMode = false)
                // Stand over the spent ashes for a moment - the follow would otherwise drag the
                // reborn phoenix straight back to its owner mid-flare.
                phoenix.mode = PauseMode
                phoenix.watch(this)
                phoenix.anim("phoenix_spawn")
                phoenix.levels.restore(Skill.Constitution, phoenix.levels.getMax(Skill.Constitution))
                for (scorched in nearbyAttackableNpcs(ashes.tile, radius = 1).take(RISE_MAX_TARGETS)) {
                    phoenix.hit(scorched, offensiveType = "magic", damage = random.nextInt(maxHit + 1))
                }
                message("Your phoenix rises from the ashes, its wounds burning away.", ChatType.Filter)
            }
            queue("rise_from_the_ashes_return", RISE_REBIRTH_DELAY + RISE_STAND_TICKS) {
                if (follower == phoenix && phoenix.mode is PauseMode) {
                    phoenix.mode = Follow(phoenix, this)
                }
            }
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
}
