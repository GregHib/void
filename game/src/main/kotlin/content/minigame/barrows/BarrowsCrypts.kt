package content.minigame.barrows

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class BarrowsCrypts(val definitions: VariableDefinitions) : Script {
    init {
        entered("barrows_crypts") {
            if (!interfaces.contains("barrows_overlay")) {
                open("barrows_overlay")
            }
            softTimers.start("barrows_prayer_drain")
        }

        exited("barrows_crypts") {
            softTimers.stop("barrows_prayer_drain")
        }

        timerStart("barrows_prayer_drain") { TimeUnit.SECONDS.toTicks(18) }

        timerTick("barrows_prayer_drain") {
            val row = Tables.get("barrows_brothers").rows().random(random)
            val brother = row.itemId
            set("barrows_brother_head", if (tile.level == 0) "${brother}_tunnels" else brother)
            Timer.CONTINUE
        }

        exited("barrows") {
            if (tile in Areas["barrows_crypts"]) {
                return@exited
            }
            close("barrows_overlay")
        }

        interfaceOpened("barrows_overlay") {
            sendVariable("ahrim_killed")
            sendVariable("dharok_killed")
            sendVariable("guthan_killed")
            sendVariable("karil_killed")
            sendVariable("torag_killed")
            sendVariable("verac_killed")
        }

        objectOperate("Open", "dharok_sarcophagus,verac_sarcophagus,ahrim_sarcophagus,guthan_sarcophagus,karil_sarcophagus,torag_sarcophagus") { (target) ->
            // TODO if selected one with tunnel
            val brother = target.id.substringBefore("_sarcophagus")
            if (get("${brother}_killed", false)) {
                message("You don't find anything.", ChatType.Filter)
                return@objectOperate
            }

            val spawn = tile.toCuboid(3).random()
            val id = Tables.npc("barrows_brothers.${brother}.npc")
            val npc = NPCs.add(id, spawn)
            set("${brother}_spawn", npc)

        }

        playerDespawn {
            removeBrother("ahrim")
            removeBrother("dharok")
            removeBrother("guthan")
            removeBrother("karil")
            removeBrother("torag")
            removeBrother("verac")
        }

        objectOperate("Climb-up", "dharok_stairs,verac_stairs,ahrim_stairs,guthan_stairs,karil_stairs,torag_stairs") {
            val brother = it.target.id.substringBefore("_stairs")
            removeBrother(brother)
            tele(Areas["${brother}_hill"])
        }
    }

    private fun Player.removeBrother(brother: String) {
        if (!contains("${brother}_spawn")) {
            return
        }
        val npc = remove<NPC>(brother)
        NPCs.remove(npc)
    }
}