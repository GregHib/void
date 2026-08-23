package content.area.misthalin.ham_hideout

import content.entity.player.dialogue.Expression
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class HamMemberDialogue : Script {

    companion object {
        private const val HAM_MEMBER_4_IDLE_EMOTE = "ham_member_ham_cave_sit_fidget"
        private const val HAM_MEMBER_4_IDLE_EMOTE_MIN_DELAY = 10
        private const val HAM_MEMBER_4_IDLE_EMOTE_MAX_DELAY = 15
        private const val HAM_MEMBER_4_LAST_IDLE_EMOTE_TICK = "ham_member_4_last_idle_emote_tick"
    }

    private val talkToMembers = setOf(
        "ham_member_ham_cave",
        "ham_member_ham_cave_2",
        "ham_member_ham_cave_3",
        "ham_member_ham_cave_4",
        "ham_guard_ham_cave",
        "ham_guard_ham_cave_2",
        "ham_guard_ham_cave_3",
    )

    private val whatAreYouDoingHereResponses = listOf(
        listOf(
            "Many of us disagree with the king about what freedoms the local monster population should have. We're taking a stand and mobilising our forces against the monstrous hordes.",
        ),
        listOf(
            "I'm totally in awe of Johanhus, he really knows what's what. I know he keeps going on about monsters and it's clear there are too many of them, so hey, I agree with whatever Johanhus says.",
        ),
        listOf(
            "We're against the monsters..like Johanhus says...we don't like them...you know.",
        ),
    )

    private val fixedHamMemberDirections = mapOf(
        Tile(3153, 9625) to Direction.WEST,
        Tile(3151, 9625) to Direction.EAST,
        Tile(3181, 9632) to Direction.SOUTH,
        Tile(3182, 9631) to Direction.WEST,
        Tile(3180, 9630) to Direction.NORTH_EAST,
    )

    init {
        npcSpawn("ham_member_ham_cave_4") {
            lockHamMember4()
            scheduleHamMember4IdleEmote()
        }
        npcDespawn("ham_member_ham_cave_4") {
            World.clearQueue(idleEmoteQueueName(index))
        }
        npcMoved("ham_member_ham_cave_4") { _ ->
            val spawn = get("spawn_tile", tile)
            if (tile != spawn) {
                tele(spawn)
            }
            lockHamMember4()
        }
        for (id in talkToMembers) {
            npcOperate("Talk-to", id) { (target) ->
                if (target.id == "ham_member_ham_cave_4") {
                    World.clearQueue(idleEmoteQueueName(target.index))
                }
                target.lockHamMember4()
                memberDialogue(target)
                target.lockHamMember4()
                if (target.id == "ham_member_ham_cave_4") {
                    target.scheduleHamMember4IdleEmote()
                }
            }
        }
    }

    private suspend fun Player.memberDialogue(target: NPC) {
        memberTopics(target)
    }

    private suspend fun Player.memberTopics(target: NPC) {
        choice("Select an Option") {
            option<Neutral>("What are all you people doing here?") {
                randomDialogue(target, whatAreYouDoingHereResponses)
                memberTopics(target)
            }
            option<Neutral>("Who are you and what do you do here?") {
                memberNpc<Neutral>(target, "I'm a strong believer in the non-monsters policy...we should really get rid of them...and if that means I have to live in a cave like a monster, so be it!")
                memberTopics(target)
            }
            option<Neutral>("What do you think you're going to achieve?") {
                memberNpc<Neutral>(target, "We want a world without monsters, to live in safety and without fear of being attacked by these ferocious beasts.")
                player<Neutral>("But there aren't that many ferocious beasts in the towns and cities.")
                memberNpc<Neutral>(target, "That's not enough, we want to get rid of them totally, we want to enjoy the surrounding lands and not worry about our children playing in caves and so on.")
                memberTopics(target)
            }
            option<Neutral>("Where did all you people come from?") {
                memberNpc<Neutral>(target, "Most of us came from small towns that had been attacked by monsters. We all got fed up with it and so decided to join this movement. We're hoping to return to the towns and cities when we've cleaned up the areas that these monsters live in.")
                closeDialogue()
            }
            option<Neutral>("Okay, thanks.") {
                closeDialogue()
            }
        }
    }

    private suspend fun Player.randomDialogue(target: NPC, responses: List<List<String>>) {
        val lines = responses[random.nextInt(responses.size)]
        for ((index, line) in lines.withIndex()) {
            if (index == 0) {
                memberNpc<Neutral>(target, line)
            } else {
                memberNpc<Neutral>(target, line)
            }
        }
    }

    private suspend inline fun <reified E : Expression> Player.memberNpc(target: NPC, text: String) {
        if (target.id == "ham_member_ham_cave_4") {
            npc<E>(target.id, text)
            target.lockHamMember4()
        } else {
            npc<E>(text)
        }
    }

    private fun NPC.lockHamMember4(clearWatch: Boolean = true) {
        val spawn = get("spawn_tile", tile)
        val direction = fixedHamMemberDirections[spawn] ?: return
        mode = PauseMode
        if (clearWatch) {
            clearWatch()
        }
        if (this.direction != direction) {
            face(direction)
        }
    }

    private fun NPC.scheduleHamMember4IdleEmote() {
        if (index == -1) {
            return
        }
        World.clearQueue(idleEmoteQueueName(index))
        World.queue(idleEmoteQueueName(index), random.nextInt(HAM_MEMBER_4_IDLE_EMOTE_MIN_DELAY, HAM_MEMBER_4_IDLE_EMOTE_MAX_DELAY + 1)) {
            if (index == -1) {
                return@queue
            }
            if (visuals.watch.index != -1) {
                scheduleHamMember4IdleEmote()
                return@queue
            }
            lockHamMember4(clearWatch = false)
            this[HAM_MEMBER_4_LAST_IDLE_EMOTE_TICK] = GameLoop.tick
            anim(HAM_MEMBER_4_IDLE_EMOTE, override = true)
            scheduleHamMember4IdleEmote()
        }
    }

    private fun idleEmoteQueueName(index: Int) = "ham_member_4_idle_emote_$index"
}
