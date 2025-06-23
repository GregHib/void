package content.area.misthalin.lumbridge.church

import content.entity.effect.transform
import content.entity.player.inv.item.drop.canDrop
import content.entity.player.modal.map.MapMarkers
import content.entity.sound.sound
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.*
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

playerSpawn { player ->
    val tile: Tile = player["gravestone_tile"] ?: return@playerSpawn
    val time: Long = player["gravestone_time"] ?: return@playerSpawn
    val remaining = time - epochSeconds()
    if (remaining > 0) {
        MapMarkers.add(player, tile, "grave")
        player.sendScript("gravestone_set_timer", remaining / 60 * 100)
    } else {
        player.clear("gravestone_tile")
        player.clear("gravestone_time")
    }
}

val players: Players by inject()
val npcs: NPCs by inject()

npcSpawn("gravestone_*") { npc ->
    val minutes = Gravestone.times[npc.id.removePrefix("gravestone_")] ?: return@npcSpawn
    val seconds = TimeUnit.MINUTES.toSeconds(minutes.toLong()).toInt()
    npc.start("grave_timer", seconds, epochSeconds())
    npc.softTimers.start("grave_degrade")
}

npcTimerStart("grave_degrade") { npc ->
    this.interval = 60
    val player = players.get(npc["player_name", ""])
    if (player != null) {
        val remaining = npc.remaining("grave_timer", epochSeconds())
        player.sendScript("gravestone_set_timer", remaining / 60 * 100)
    }
}

npcTimerTick("grave_degrade") { npc ->
    val remaining = npc.remaining("grave_timer", epochSeconds())
    if (remaining <= 120 && !npc.transform.endsWith("broken")) {
        npc.transform("${npc.id}_broken")
    } else if (remaining <= 60 && !npc.transform.endsWith("collapse")) {
        npc.transform("${npc.id}_collapse")
        val player = players.get(npc["player_name", ""])
        player?.message("Your gravestone has collapsed.")
    }
}

npcTimerStop("grave_degrade") { npc ->
    val player = players.get(npc.remove("player_name") ?: "")
    if (player != null) {
        player.clear("gravestone_time")
        val tile: Tile? = player.remove("gravestone_tile")
        if (tile != null) {
            MapMarkers.remove(player, tile, "grave")
        }
    }
    npc.stop("grave_timer")
    npcs.remove(npc)
}

npcOperate("Read", "gravestone_*") {
    val remainder = target.remaining("grave_timer", epochSeconds())
    remainMessage(player, target)
    when {
        player.name == target["player_name", ""] -> player.message("Isn't there something a bit odd about reading your own gravestone?")
        remainder < 60 -> player.message("The inscription is too unclear to read.")
        else -> {
            player.open("gravestone_plaque")
            val gravestone = target.id.removePrefix("gravestone_").removeSuffix("_broken")
            val message = Gravestone.messages[gravestone] ?: return@npcOperate
            val name = target["player_name", ""]
            player.interfaces.sendText(
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

val floorItems: FloorItems by inject()

npcOperate("Repair", "gravestone_*") {
    val name = target["player_name", ""]
    if (name == player.name) {
        player.message("The gods don't seem to approve of people attempting to repair their own gravestones.")
        return@npcOperate
    }
    if (target["blessed", false]) {
        player.message("This gravestone can no longer be repaired.")
        return@npcOperate
    }
    if (!player.has(Skill.Prayer, 2)) {
        player.message("You need a Prayer level of 2 to repair a gravestone.")
        return@npcOperate
    }
    val seconds = 300 // 5 minutes
    target.start("grave_timer", seconds, epochSeconds())
    updateItems(target.tile, name, seconds)
    delay(2)
    val deceased = players.get(name)
    val remainder = target.remaining("grave_timer", epochSeconds())
    val minutes = TimeUnit.SECONDS.toMinutes(remainder.toLong())
    deceased?.message("${player.name} has repaired your gravestone. It should survive another $minutes ${"minute".plural(minutes)}.")
}

npcOperate("Bless", "gravestone_*") {
    val name = target["player_name", ""]
    if (name == player.name) {
        player.message("The gods don't seem to approve of people attempting to bless their own gravestones.")
        return@npcOperate
    }
    if (!player.has(Skill.Prayer, 70)) {
        player.message("You need a prayer level of 70 to bless a gravestone.")
        return@npcOperate
    }
    if (player.levels.get(Skill.Prayer) == 0) {
        player.message("You don't have enough prayer points to bless the gravestone.")
        return@npcOperate
    }
    if (target["blessed", false]) {
        player.message("This gravestone has already been blessed.")
        return@npcOperate
    }
    val seconds = 3600 // 1 hour
    target.start("grave_timer", seconds, epochSeconds())
    updateItems(target.tile, name, seconds)
    player.anim("altar_pray")
    player.gfx("bless_grave")
    player.sound("self_heal")
    delay(2)
    player.message("The gods hear your prayers; the gravestone will remain for a little longer.")
    target["blessed"] = true
    val deceased = players.get(name)
    val remainder = target.remaining("grave_timer", epochSeconds())
    val minutes = TimeUnit.SECONDS.toMinutes(remainder.toLong())
    deceased?.message("${player.name} has blessed your gravestone. It should survive another $minutes ${"minute".plural(minutes)}.")
}

npcOperate("Demolish", "gravestone_*") {
    if (player.name != target["player_name", ""]) {
        player.message("It would be impolite to demolish someone else's gravestone.")
        return@npcOperate
    }
    val remainder = target.remaining("grave_timer", epochSeconds())
    val minutes = TimeUnit.SECONDS.toMinutes(remainder.toLong())
    target.softTimers.stop("grave_degrade")
    npcs.remove(target)
    player.message("It looks like it'll survive another $minutes ${"minute".plural(minutes)}. You demolish it anyway.")
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

canDrop { player ->
    val list = npcs[tile].filter { it.id.startsWith("gravestone_") }
    for (grave in list) {
        if (grave["player_name", ""] == player.name) {
            player.message("Surely you aren't going to drop litter on your own grave!")
            cancel()
            return@canDrop
        }
    }
}

fun updateItems(tile: Tile, name: String, seconds: Int) {
    val items = floorItems[tile].filter { it.owner == name }
    for (item in items) {
        item.revealTicks = TimeUnit.SECONDS.toTicks(seconds)
        item.disappearTicks = TimeUnit.SECONDS.toTicks(seconds) + 60
    }
}
