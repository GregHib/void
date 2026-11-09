package content.area.misthalin.ham_hideout

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.random

class HamMemberDialogue : Script {

    companion object {
        private const val SEATED_MEMBER_FIDGET_ANIMATION = "ham_member_ham_cave_sit_fidget"
        private const val SEATED_MEMBER_FIDGET_MIN_DELAY = 10
        private const val SEATED_MEMBER_FIDGET_MAX_DELAY = 20
        private const val SEATED_MEMBER_FIDGET_TIMER = "ham_member_ham_cave_3_fidget"
    }

    private val talkToMembers = setOf(
        "ham_member_male_ham_cave",
        "ham_member_female_ham_cave",
        "ham_member_ham_cave_3",
        "ham_guard_ham_cave",
        "ham_guard_ham_cave_2",
        "ham_guard_ham_cave_3",
    )

    private val whatAreYouDoingHereResponses = listOf(
        "Many of us disagree with the king about what freedoms the local monster population should have. We're taking a stand against the monstrous hordes.",
        "I'm totally in awe of Johanhus, he really knows what's what. I agree with whatever he says.",
        "We're against the monsters... like Johanhus says... we don't like them, you know.",
    )

    init {
        npcSpawn("ham_member_ham_cave_3") {
            softTimers.start(SEATED_MEMBER_FIDGET_TIMER)
        }
        npcTimerStart(SEATED_MEMBER_FIDGET_TIMER) {
            random.nextInt(SEATED_MEMBER_FIDGET_MIN_DELAY, SEATED_MEMBER_FIDGET_MAX_DELAY + 1)
        }
        npcTimerTick(SEATED_MEMBER_FIDGET_TIMER) {
            anim(SEATED_MEMBER_FIDGET_ANIMATION, override = true)
            random.nextInt(SEATED_MEMBER_FIDGET_MIN_DELAY, SEATED_MEMBER_FIDGET_MAX_DELAY + 1)
        }
        for (id in talkToMembers) {
            npcOperate("Talk-to", id) { (target) ->
                memberTopics(target)
            }
        }
    }

    private suspend fun Player.memberTopics(target: NPC) {
        choice("Select an Option") {
            option<Neutral>("What are all you people doing here?") {
                npc<Neutral>(whatAreYouDoingHereResponses.random(random))
                memberTopics(target)
            }
            option<Neutral>("Who are you and what do you do here?") {
                npc<Neutral>(target.id, "I'm a strong believer in the non-monsters policy... we should really get rid of them. If that means I have to live in a cave like a monster, so be it!")
                memberTopics(target)
            }
            option<Neutral>("What do you think you're going to achieve?") {
                npc<Neutral>(target.id, "We want a world without monsters, to live in safety and without fear of being attacked by these ferocious beasts.")
                player<Neutral>("But there aren't that many ferocious beasts in the towns and cities.")
                npc<Neutral>(target.id, "That's not enough. We want to get rid of them totally and enjoy the surrounding lands without fear.")
                memberTopics(target)
            }
            option<Neutral>("Where did all you people come from?") {
                npc<Neutral>(target.id, "Most of us came from small towns that had been attacked by monsters. We got fed up with it and joined this movement.")
                closeDialogue()
            }
            option<Neutral>("Ok, thanks.") {
                closeDialogue()
            }
        }
    }
}
