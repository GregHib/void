package content.area.misthalin.lumbridge.church

import content.entity.effect.transform
import content.quest.questCompleted
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.notEnough
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.engine.timer.npcTimerStart
import world.gregs.voidps.engine.timer.npcTimerStop
import world.gregs.voidps.engine.timer.npcTimerTick
import java.util.concurrent.TimeUnit

val enums: EnumDefinitions by inject()

interfaceOpen("gravestone_shop") { player ->
    if (player.questCompleted("the_restless_ghost")) {
        player.addVarbit("unlocked_gravestones", "flag")
        player.addVarbit("unlocked_gravestones", "small_gravestone")
        player.addVarbit("unlocked_gravestones", "ornate_gravestone")
    }
    if (player.questCompleted("the_giant_dwarf")) {
        player.addVarbit("unlocked_gravestones", "font_of_life")
        player.addVarbit("unlocked_gravestones", "stele")
        player.addVarbit("unlocked_gravestones", "symbol_of_saradomin")
        player.addVarbit("unlocked_gravestones", "symbol_of_zamorak")
        player.addVarbit("unlocked_gravestones", "symbol_of_guthix")
        player.addVarbit("unlocked_gravestones", "angel_of_death")
        if (player.questCompleted("land_of_the_goblins")) {
            player.addVarbit("unlocked_gravestones", "symbol_of_bandos")
        }
        if (player.questCompleted("temple_of_ikov")) {
            player.addVarbit("unlocked_gravestones", "symbol_of_armadyl")
        }
        if (player.questCompleted("desert_treasure")) {
            player.addVarbit("unlocked_gravestones", "ancient_symbol")
        }
    }
    if (player.questCompleted("king_of_the_dwarves")) {
        player.addVarbit("unlocked_gravestones", "royal_dwarven_gravestone")
    }
    player.interfaceOptions.unlockAll(id, "button", 0 until 13)
}

interfaceOption("*", "button", "gravestone_shop") {
    val name = enums.get("gravestone_names").getString(itemSlot)
    val id = name.replace(" ", "_").lowercase()
    if (player["gravestone_current", "memorial_plaque"] == id) {
        // TODO already have
        return@interfaceOption
    }
    val cost = enums.get("gravestone_price").getInt(itemSlot)
    if (cost > 0 && !player.inventory.remove("coins", cost)) {
        player.notEnough("coins")
        return@interfaceOption
    }
    player["gravestone_current"] = id
//    player["gravestone_index"] = itemSlot
}

val players: Players by inject()
val npcs: NPCs by inject()

npcSpawn("gravestone_*") { npc ->
    val minutes = Gravestone.times[npc.id.removePrefix("gravestone_")] ?: return@npcSpawn
    npc.start("grave_timer", TimeUnit.MINUTES.toSeconds(minutes.toLong()).toInt(), epochSeconds())
    npc.softTimers.start("grave_degrade")
}

npcTimerStart("grave_degrade") {
    this.interval = 60
}

npcTimerTick("grave_degrade") { npc ->
    val remaining = npc.remaining("grave_timer", epochSeconds())
    if (remaining <= 120 && !npc.transform.endsWith("broken")) {
        npc.transform("${npc.id}_broken")
        val player = players.get(npc["player_name", ""])
        if (player != null) {
//            player.message("TODO")
        }
    } else if (remaining <= 60 && !npc.transform.endsWith("collapse")) {
        npc.transform("${npc.id}_collapse")
        val player = players.get(npc["player_name", ""])
        if (player != null) {
//            player.message("TODO")
        }
    }
}

npcTimerStop("grave_degrade") { npc ->
    npcs.remove(npc)
}

npcOperate("Read", "gravestone_*") {
    val remainder = target.remaining("grave_timer", epochSeconds())
    val name = target["player_name", ""]

    // https://www.youtube.com/watch?v=FnYqafcg7Ow
    if (remainder < 10) {
        player.message("It's about to collapse!")
        player.message("The inscription is too unclear to read.")
    } else {
        remainMessage(player, target)
    }
    when {
//        player.name == target["player_name", ""] -> player.message("Isn't there something a bit odd about reading your own gravestone?")
        remainder < 60 -> player.message("The inscription is too unclear to read.")
        else -> {
            player.open("gravestone_plaque")
            val gravestone = target.id.removePrefix("gravestone_").removeSuffix("_broken")
            val message = Gravestone.messages[gravestone] ?: return@npcOperate
            player.interfaces.sendText(
                "gravestone_plaque", "text", message
                    .replace("<name>", name)
                    .replace("<time>", TimeUnit.SECONDS.toMinutes(remainder.toLong()).toString())
                    .replace("<gender>", if (target["player_male", true]) "His" else "Her")
            )
        }
    }
}

npcOperate("Repair", "gravestone_*") {
    // TODO anim and gfx
    player.message("The gods hear your prayers; the gravestone will remain for a little longer.")
    remainMessage(player, target)
    player.message("That cost you 3 Prayer points.")
}

npcOperate("Bless", "gravestone_*") {
    if (!player.has(Skill.Prayer, 77)) {
        return@npcOperate
    }
    val player = players.get(target["player_name", ""])
    if (player != null) {
        player.message("${player.name} has blessed your gravestone. It should survive another 59 minutes.")
    }
}

fun remainMessage(player: Player, grave: NPC) {
    val remainder = grave.remaining("grave_timer", epochSeconds())
    val time = when (val minutes = TimeUnit.SECONDS.toMinutes(remainder.toLong()).toInt()) {
        1 -> "minute and $remainder ${"second".plural(remainder)}"
        0 -> "$remainder ${"second".plural(remainder)}"
        else -> "$minutes ${"minute".plural(minutes)}"
    }
    player.message("It looks like it'll survive another $time.")
}