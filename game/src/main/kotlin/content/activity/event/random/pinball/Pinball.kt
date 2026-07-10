package content.activity.event.random.pinball

import content.activity.event.random.RandomEvents
import content.activity.event.random.kidnap
import content.activity.event.random.mysteriousOldMan
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.closeTabs
import content.quest.instanceOffset
import content.quest.openTabs
import content.quest.smallInstance
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.Minimap
import world.gregs.voidps.engine.client.clearMinimap
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.minimap
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import world.gregs.voidps.type.random

/**
 * Pinball random event: the Mysterious Old Man teleports the player into a private copy of the trolls'
 * pinball arena. One of five posts flashes with rings; tagging it scores a point and lights a new one,
 * while tagging an unlit post resets the score to zero. Score ten points, then leave through the cave
 * exit (the troll guards Flippa and Tilt block the way until then) for a random event gift.
 * https://runescape.wiki/w/Random_events?oldid=3667851#Pinball
 */
class Pinball : Script {

    init {
        RandomEvents.register("pinball") { startEvent() }

        objectOperate("Tag", "pinball_post_*") { (post) ->
            if (get<String>("random_event") != "pinball") {
                return@objectOperate
            }
            tagPost(post)
        }

        objectOperate("Exit", "pinball_cave_exit") {
            if (get<String>("random_event") != "pinball") {
                return@objectOperate
            }
            exitArena()
        }

        // The trolls' cache option is "Talk-To" (note the capital O), not the usual "Talk-to".
        for (guard in GUARDS) {
            npcOperate("Talk-To", guard) {
                if (get<String>("random_event") != "pinball") {
                    message("He's busy right now.")
                    return@npcOperate
                }
                guardTalk(guard)
            }
        }
    }

    private suspend fun Player.startEvent() {
        set("pinball_score", 0)
        clear("pinball_target")
        mysteriousOldMan()
        val offset = copyArena()
        kidnap(ARENA.add(offset))
        NPCs.add("flippa_pinball", FLIPPA.add(offset), Direction.NORTH, ticks = -1, owner = this)
        NPCs.add("tilt_pinball", TILT.add(offset), Direction.NORTH, ticks = -1, owner = this)
        closeTabs()
        minimap(Minimap.HideMap)
        open("pinball_overlay")
        sendScore()
        rules(offset)
        lightNextPost()
    }

    /**
     * Allocate a private instance and copy only the arena's zones (the box between [AREA_SW] and
     * [AREA_NE]) into it, leaving the rest of the instance empty. Returns the tile offset from the real
     * arena to its copy.
     */
    private fun Player.copyArena(): Delta {
        val instance = smallInstance()
        val offset = instance.offset(REGION)
        val zones = get<DynamicZones>()
        for (zoneX in (AREA_SW.x shr 3)..(AREA_NE.x shr 3)) {
            for (zoneY in (AREA_SW.y shr 3)..(AREA_NE.y shr 3)) {
                val from = Zone(zoneX, zoneY, 0)
                val base = from.tile.add(offset)
                zones.copy(from, Zone(base.x shr 3, base.y shr 3, base.level))
            }
        }
        set("instance_offset", offset.id)
        return offset
    }

    private suspend fun Player.rules(offset: Delta) {
        val oldMan = NPCs.add("mysterious_old_man", OLD_MAN.add(offset), ticks = -1, owner = this)
        oldMan.face(this)
        face(oldMan)
        npc<Neutral>("mysterious_old_man", "The rules of the game are quite simple. You have to score 10 points by tagging the flashing pillars.")
        npc<Neutral>("mysterious_old_man", "Don't tag the ones that do not have rings around the base, as that will reset your points, and don't try and get past those trolls until you are done!")
        npc<Neutral>("mysterious_old_man", "See you later!")
        oldMan.anim("emote_wave")
        delay(4)
        oldMan.gfx("imp_puff")
        delay(1)
        NPCs.remove(oldMan)
        statement("Tag the post with the $FLASHING_RINGS.", clickToContinue = false)
    }

    private suspend fun Player.tagPost(post: GameObject) {
        if (get("pinball_score", 0) >= REQUIRED) {
            return
        }
        anim("take")
        val tagged = POSTS.indexOfFirst { it.id == post.id }
        if (tagged != get("pinball_target", 0) - 1) {
            set("pinball_score", 0)
            sendScore()
            lightNextPost()
            statement("Wrong post! Your score has been reset. Tag the post with the $FLASHING_RINGS.", clickToContinue = false)
            return
        }
        val score = inc("pinball_score")
        sendScore()
        if (score >= REQUIRED) {
            resetPost(tagged)
            clear("pinball_target")
            statement("Congratulations - you can now leave the arena.", clickToContinue = false)
        } else {
            lightNextPost()
            statement("Well done! Now tag the next post.", clickToContinue = false)
        }
    }

    private suspend fun Player.exitArena() {
        if (get("pinball_score", 0) < REQUIRED) {
            guardTalk(GUARDS.first())
            return
        }
        reward()
        close("pinball_overlay")
        openTabs()
        clearMinimap()
        clear("pinball_score")
        clear("pinball_target")
        RandomEvents.complete(this) // clears the instance and teleports the player home
    }

    private suspend fun Player.guardTalk(guard: String) {
        if (get("pinball_score", 0) >= REQUIRED) {
            player<Quiz>("So...I'm free to go now, right?")
            npc<Neutral>(guard, "Yer, get going. We get break now.")
            player<Neutral>("Ok. Err...have a nice break.")
        } else {
            player<Confused>("Err...what am I supposed to do here again?")
            npc<Neutral>(guard, "You gotta poke dem pillars.")
            player<Confused>("What? All of them?")
            npc<Neutral>(guard, "No, no. If you poke dem ones dat don't have a dem flashin' rings, den dats bad.")
            player<Quiz>("So I have to poke the pillars that are flashing?")
            npc<Neutral>(guard, "Yer. You do dat ten times, you get prize.")
        }
    }

    private fun Player.reward() {
        addOrDrop("random_event_gift")
    }

    /** Reset the previous flashing post and start a new random one flashing; the target is 1-based. */
    private fun Player.lightNextPost() {
        resetPost(get("pinball_target", 0) - 1)
        val next = POSTS.indices.random(random)
        animatePost(next, "pinball_post_flash")
        set("pinball_target", next + 1)
    }

    private fun Player.resetPost(index: Int) {
        if (index in POSTS.indices) {
            animatePost(index, "pinball_post_reset")
        }
    }

    private fun Player.animatePost(index: Int, anim: String) {
        val post = POSTS[index]
        GameObjects.at(post.tile.add(instanceOffset())).firstOrNull { it.id == post.id }?.anim(anim)
    }

    private fun Player.sendScore() {
        interfaces.sendText("pinball_overlay", "score", "Score: ${get("pinball_score", 0)}")
    }

    private data class Post(val id: String, val tile: Tile)

    companion object {
        private const val REQUIRED = 10
        private const val FLASHING_RINGS = "<col=0000ff>flashing rings</col>"
        private val REGION = Region(30, 78)

        // Only this box is copied into the instance: south-west and north-east corners of the arena.
        private val AREA_SW = Tile(1960, 5032)
        private val AREA_NE = Tile(1982, 5055)

        private val ARENA = Tile(1972, 5046)
        private val OLD_MAN = Tile(1971, 5046)
        private val FLIPPA = Tile(1973, 5038)
        private val TILT = Tile(1970, 5038)
        private val GUARDS = listOf("flippa_pinball", "tilt_pinball")

        private val POSTS = listOf(
            Post("pinball_post_1", Tile(1967, 5046)),
            Post("pinball_post_2", Tile(1969, 5049)),
            Post("pinball_post_3", Tile(1972, 5050)),
            Post("pinball_post_4", Tile(1975, 5049)),
            Post("pinball_post_5", Tile(1977, 5046)),
        )
    }
}
