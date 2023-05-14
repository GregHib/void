package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.Minimap
import world.gregs.voidps.engine.client.clearMinimap
import world.gregs.voidps.engine.client.minimap
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.chunk.DynamicChunks
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.suspend.delay
import world.gregs.voidps.world.interact.dialogue.type.statement

private val tabs = listOf(
    "combat_styles",
    "task_system",
    "stats",
    "quest_journals",
    "inventory",
    "worn_equipment",
    "prayer_list",
    "modern_spellbook",
    "emotes",
    "notes"
)

suspend fun Interaction.startCutscene(region: Region, statement: String = ""): Region {
    player.open("fade_out")
    statement(statement, clickToContinue = false)
    delay(2)
    val instance = Instances.small()
    get<DynamicChunks>().copy(region, instance)
    tabs.forEach {
        player.close(it)
    }
    player.minimap(Minimap.HideMap)
    return instance
}

fun Interaction.stopCutscene(instance: Region) {
    Instances.free(instance)
    get<DynamicChunks>().clear(instance)
    tabs.forEach {
        player.open(it)
    }
    player.open("fade_in")
    player.clearMinimap()
}