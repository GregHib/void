package content.area.misthalin.lumbridge.church

import content.entity.effect.transform
import content.entity.player.modal.map.MapMarkers
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.timer.*
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

class Gravestones : Script {

    init {
        playerSpawn {
            val tile: Tile = get("gravestone_tile") ?: return@playerSpawn
            val time: Long = get("gravestone_time") ?: return@playerSpawn
            val remaining = time - epochSeconds()
            if (remaining > 0) {
                MapMarkers.add(this, tile, "grave")
                sendScript("gravestone_set_timer", remaining / 60 * 100)
            } else {
                clear("gravestone_tile")
                clear("gravestone_time")
            }
        }

        npcTimerStart("grave_degrade", ::start)
        npcTimerTick("grave_degrade", ::tick)
        npcTimerStop("grave_degrade", ::stop)

        npcSpawn("gravestone_*") {
            val minutes = Gravestone.times[id.removePrefix("gravestone_")] ?: return@npcSpawn
            val seconds = TimeUnit.MINUTES.toSeconds(minutes.toLong()).toInt()
            start("grave_timer", seconds, epochSeconds())
            softTimers.start("grave_degrade")
        }

        npcOperate("Read", "gravestone_*") { (target) ->
            val remainder = target.remaining("grave_timer", epochSeconds())
            remainMessage(this, target)
            when {
                name == target["player_name", ""] -> message("Isn't there something a bit odd about reading your own gravestone?")
                remainder < 60 -> message("The inscription is too unclear to read.")
                else -> {
                    open("gravestone_plaque")
                    val gravestone = target.id.removePrefix("gravestone_").removeSuffix("_broken")
                    val message = Gravestone.messages[gravestone] ?: return@npcOperate
                    val name = target["player_name", ""]
                    interfaces.sendText(
                        "gravestone_plaque",
                        "text",
                        message
                            .replace("<name>", name)
                            .replace("<time>", TimeUnit.SECONDS.toMinutes(remainder.toLong()).toString())
                            .replace("<gender>", if (target["player_male", true]) "His" else "Her"),
                    )
                }
            }
        }

        npcOperate("Repair", "gravestone_*") { (target) ->
            val name = target["player_name", ""]
            if (name == this.name) {
                message("The gods don't seem to approve of people attempting to repair their own gravestones.")
                return@npcOperate
            }
            if (target["blessed", false]) {
                message("This gravestone can no longer be repaired.")
                return@npcOperate
            }
            if (!has(Skill.Prayer, 2)) {
                message("You need a Prayer level of 2 to repair a gravestone.")
                return@npcOperate
            }
            val seconds = 300 // 5 minutes
            target.start("grave_timer", seconds, epochSeconds())
            updateItems(target.tile, name, seconds)
            delay(2)
            val deceased = Players.find(name)
            val remainder = target.remaining("grave_timer", epochSeconds())
            val minutes = TimeUnit.SECONDS.toMinutes(remainder.toLong())
            deceased?.message("$name has repaired your gravestone. It should survive another $minutes ${"minute".plural(minutes)}.")
        }

        npcOperate("Bless", "gravestone_*") { (target) ->
            val name = target["player_name", ""]
            if (name == this.name) {
                message("The gods don't seem to approve of people attempting to bless their own gravestones.")
                return@npcOperate
            }
            if (!has(Skill.Prayer, 70)) {
                message("You need a prayer level of 70 to bless a gravestone.")
                return@npcOperate
            }
            if (levels.get(Skill.Prayer) == 0) {
                message("You don't have enough prayer points to bless the gravestone.")
                return@npcOperate
            }
            if (target["blessed", false]) {
                message("This gravestone has already been blessed.")
                return@npcOperate
            }
            val seconds = 3600 // 1 hour
            target.start("grave_timer", seconds, epochSeconds())
            updateItems(target.tile, name, seconds)
            anim("altar_pray")
            gfx("bless_grave")
            sound("self_heal")
            delay(2)
            message("The gods hear your prayers; the gravestone will remain for a little longer.")
            target["blessed"] = true
            val deceased = Players.find(name)
            val remainder = target.remaining("grave_timer", epochSeconds())
            val minutes = TimeUnit.SECONDS.toMinutes(remainder.toLong())
            deceased?.message("$name has blessed your gravestone. It should survive another $minutes ${"minute".plural(minutes)}.")
        }

        npcOperate("Demolish", "gravestone_*") { (target) ->
            if (this.name != target["player_name", ""]) {
                message("It would be impolite to demolish someone else's gravestone.")
                return@npcOperate
            }
            val remainder = target.remaining("grave_timer", epochSeconds())
            val minutes = TimeUnit.SECONDS.toMinutes(remainder.toLong())
            target.softTimers.stop("grave_degrade")
            NPCs.remove(target)
            message("It looks like it'll survive another $minutes ${"minute".plural(minutes)}. You demolish it anyway.")
        }

        droppable {
            var droppable = true
            // TODO can you drop items on someone else's grave?
            for (grave in NPCs.at(tile).filter { it.id.startsWith("gravestone_") }) {
                if (grave["player_name", ""] == name) {
                    message("Surely you aren't going to drop litter on your own grave!")
                    droppable = false
                }
            }
            droppable
        }
    }

    fun start(npc: NPC, restart: Boolean): Int {
        val player = Players.find(npc["player_name", ""])
        if (player != null) {
            val remaining = npc.remaining("grave_timer", epochSeconds())
            player.sendScript("gravestone_set_timer", remaining / 60 * 100)
        }
        return 60
    }

    fun tick(npc: NPC): Int {
        val remaining = npc.remaining("grave_timer", epochSeconds())
        if (remaining <= 120 && !npc.transform.endsWith("broken")) {
            npc.transform("${npc.id}_broken")
        } else if (remaining <= 60 && !npc.transform.endsWith("collapse")) {
            npc.transform("${npc.id}_collapse")
            val player = Players.find(npc["player_name", ""])
            player?.message("Your gravestone has collapsed.")
        }
        return Timer.CONTINUE
    }

    fun stop(npc: NPC, death: Boolean) {
        val player = Players.find(npc.remove("player_name") ?: "")
        if (player != null) {
            player.clear("gravestone_time")
            val tile: Tile? = player.remove("gravestone_tile")
            if (tile != null) {
                MapMarkers.remove(player, tile, "grave")
            }
        }
        npc.stop("grave_timer")
        NPCs.remove(npc)
    }

    fun remainMessage(player: Player, grave: NPC) {
        // https://www.youtube.com/watch?v=FnYqafcg7Ow
        val remainder = grave.remaining("grave_timer", epochSeconds())
        if (remainder < 10) {
            player.message("It's about to collapse!")
            return
        }
        val time = when (val minutes = TimeUnit.SECONDS.toMinutes(remainder.toLong()).toInt()) {
            1 -> "minute and $remainder ${"second".plural(remainder.rem(60))}"
            0 -> "$remainder ${"second".plural(remainder)}"
            else -> "$minutes ${"minute".plural(minutes)}"
        }
        player.message("It looks like it'll survive another $time.")
    }

    fun updateItems(tile: Tile, name: String, seconds: Int) {
        val items = FloorItems.at(tile).filter { it.owner == name }
        for (item in items) {
            item.revealTicks = TimeUnit.SECONDS.toTicks(seconds)
            item.disappearTicks = TimeUnit.SECONDS.toTicks(seconds) + 60
        }
    }
}
