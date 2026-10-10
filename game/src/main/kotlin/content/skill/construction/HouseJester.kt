package content.skill.construction

import content.entity.gfx.areaGfx
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.skill.construction.House.Companion.houseOwner
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Games room jester: summoning Jacky Jester who performs a sequence of emotes for the player to copy, the sequence gets longer with each score.
 * Getting it wrong doesn't end the game, the jester stays until he's banished.
 * There is one jester per house which everyone in it can play along with, the state of the game is kept on the house owner.
 * Only the owner or whoever summoned the jester can banish him.
 * https://oldschool.runescape.wiki/w/Games_room
 */
class HouseJester : Script {
    init {
        objectOperate("Activate", "jester") { (target) ->
            if (get("house_build_mode", false)) {
                message("You cannot summon a jester while in building mode.") // TODO proper message
                return@objectOperate
            }
            val owner = houseOwner() ?: return@objectOperate
            if (owner.jester != null) {
                message("There is already a jester.") // TODO proper message
                return@objectOperate
            }
            owner.summon(this, target)
        }

        objectOperate("Activate", "jester_active") {
            message("Nothing interesting happens.", ChatType.Engine)
        }

        npcOperate("Banish", JESTER) { (target) ->
            val owner = houseOwner()
            if (owner == null || target != owner.jester) {
                message("That isn't your jester.") // TODO proper message
                return@npcOperate
            }
            if (this != owner && accountName != owner.get<String>("jester_summoner")) {
                message("Only the house owner or whoever summoned the jester can banish him.") // TODO proper message
                return@npcOperate
            }
            npc<Happy>("Have you had enough fun for now?")
            choice("Banish the jester?") {
                option("Yes") {
                    owner.banish(target)
                }
                option("No")
            }
        }

        timerStart("house_jester") { 1 }

        timerTick("house_jester") {
            val jester = jester ?: return@timerTick Timer.CANCEL
            val wait = get("jester_wait", 0) - 1
            set("jester_wait", wait)
            if (wait > 0) {
                return@timerTick Timer.CONTINUE
            }
            when (get("jester_phase", "greet")) {
                "greet" -> greet(jester)
                "demo" -> demonstrate(jester)
                "copy" -> finishRound(jester)
                else -> round(jester)
            }
            Timer.CONTINUE
        }

        timerStop("house_jester") {
            clear("jester_phase")
        }

        interfaceOption(id = "emotes:*") {
            val owner = houseOwner() ?: return@interfaceOption
            val jester = owner.jester ?: return@interfaceOption
            val round = owner["jester_round", 0]
            if (owner["jester_phase", ""] != "copy" || get("jester_done", -1) == round) {
                return@interfaceOption
            }
            val sequence = owner.sequence()
            val progress = get("jester_progress", 0)
            if (it.option.toSnakeCase() != sequence[progress]) {
                fail(round)
                return@interfaceOption
            }
            if (progress + 1 < sequence.size) {
                set("jester_progress", progress + 1)
                owner["jester_wait"] = COPY_TIME
                return@interfaceOption
            }
            set("jester_done", round)
            val score = get("jester_score", 0) + 1
            set("jester_score", score)
            message("You got it right! Your score: $score")
            jester.anim("emote_cheer")
            // Others get a moment longer to finish before the next round
            owner["jester_solved"] = true
            owner["jester_wait"] = SETTLE
        }
    }

    private fun Player.fail(round: Int) {
        set("jester_done", round)
        message("You got it wrong.")
        jingle("burthorpe_games_room_loss")
    }

    /**
     * Summons the jester for the house owner, [summoner] is who gets to banish him
     */
    private fun Player.summon(summoner: Player, target: GameObject) {
        summoner.message("You summon a jester.", ChatType.Filter)
        areaSound("poh_jester_appear", target.tile, radius = 5)
        target.anim("poh_jester_elevate")
        val tile = target.tile.add(forward(target.rotation).delta.x * 3, forward(target.rotation).delta.y * 3)
        areaGfx("poh_jester_appear", tile, height = 128)
        val jester = NPCs.add(JESTER, tile, forward(target.rotation).inverse(), ticks = -1, owner = this)
        set("house_jester", jester)
        set("jester_summoner", summoner.accountName)
        set("jester_tile", target.tile)
        set("jester_level", 0)
        set("jester_round", 0)
        set("jester_phase", "greet")
        set("jester_wait", GREET_DELAY)
        for (player in participants()) {
            player.clear("jester_score")
        }
        target.replace("jester_active")
        softTimers.start("house_jester")
    }

    private fun Player.greet(jester: NPC) {
        when (get("jester_step", 0)) {
            0 -> {
                jester.anim("emote_bow", delay = 15)
                set("jester_step", 1)
                set("jester_wait", 2)
            }
            1 -> {
                jester.say("Let's play a game!")
                set("jester_step", 2)
                set("jester_wait", 2)
            }
            else -> {
                jester.say("Everyone copy me!")
                clear("jester_step")
                set("jester_wait", 4)
                set("jester_phase", "round")
            }
        }
    }

    /**
     * Picks a new sequence of emotes, one longer for every point scored up to [MAX_LENGTH]
     */
    private fun Player.round(jester: NPC) {
        val length = (get("jester_level", 0) + 1).coerceAtMost(MAX_LENGTH)
        set("jester_round", get("jester_round", 0) + 1)
        clear("jester_solved")
        set("jester_sequence", List(length) { EMOTES[random.nextInt(EMOTES.size)] })
        set("jester_step", 0)
        set("jester_phase", "demo")
        set("jester_wait", 1)
    }

    private fun Player.demonstrate(jester: NPC) {
        val sequence = sequence()
        val step = get("jester_step", 0)
        if (step >= sequence.size) {
            clear("jester_step")
            for (player in participants()) {
                player["jester_progress"] = 0
            }
            set("jester_phase", "copy")
            set("jester_wait", COPY_TIME)
            return
        }
        jester.anim("emote_${sequence[step]}")
        set("jester_step", step + 1)
        set("jester_wait", EMOTE_GAP)
    }

    /**
     * Ends the round when the time is up, anyone who hasn't finished the sequence got it wrong
     */
    private fun Player.finishRound(jester: NPC) {
        val round = get("jester_round", 0)
        for (player in participants()) {
            if (player["jester_done", -1] != round) {
                player.fail(round)
            }
        }
        if (get("jester_solved", false)) {
            set("jester_level", get("jester_level", 0) + 1)
        } else {
            jester.anim("emote_jump_for_joy")
        }
        nextRound(PAUSE)
    }

    private fun Player.participants() = Players.filter { it.get<String>("house_owner") == accountName }

    private fun Player.nextRound(ticks: Int) {
        set("jester_phase", "round")
        set("jester_wait", ticks)
    }

    private fun Player.sequence(): List<String> = get("jester_sequence", emptyList())

    private fun Player.banish(jester: NPC) {
        jester.say("Goodbye!")
        jester.anim("emote_bow", delay = 15)
        areaSound("pop2", jester.tile, radius = 5)
        areaGfx("random_event_puff", jester.tile, height = 124)
        NPCs.remove(jester)
        remove<NPC>("house_jester")
        val tile: Tile? = remove("jester_tile")
        if (tile != null) {
            GameObjects.findOrNull(tile, "jester_active")?.replace("jester")
        }
        softTimers.stop("house_jester")
        clear("jester_level")
        clear("jester_summoner")
        clear("jester_sequence")
        for (player in participants()) {
            player.clear("jester_score")
        }
    }

    private fun forward(rotation: Int) = when (rotation and 0x3) {
        0 -> Direction.WEST
        1 -> Direction.NORTH
        2 -> Direction.EAST
        else -> Direction.SOUTH
    }

    companion object {
        private const val JESTER = "3955"
        private const val GREET_DELAY = 8
        private const val EMOTE_GAP = 6
        private const val COPY_TIME = 16
        private const val PAUSE = 4
        private const val SETTLE = 4
        private const val MAX_LENGTH = 6
        private val EMOTES = listOf("bow", "angry", "wave", "cheer", "beckon", "jump_for_joy", "clap", "raspberry", "salute", "twirl", "dance", "panic")

        private val Player.jester: NPC?
            get() = get<NPC>("house_jester")?.takeIf { NPCs.indexed(it.index) == it }
    }
}
