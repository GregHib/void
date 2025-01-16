package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.Minimap
import world.gregs.voidps.engine.client.clearMinimap
import world.gregs.voidps.engine.client.minimap
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.type.Region

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

fun CharacterContext<Player>.startCutscene(region: Region): Region {
    val instance = Instances.small()
    get<DynamicZones>().copy(region, instance)
    hideTabs()
    return instance
}

fun CharacterContext<Player>.hideTabs() {
    tabs.forEach {
        player.close(it)
    }
    player.minimap(Minimap.HideMap)
}

fun CharacterContext<Player>.stopCutscene(instance: Region) {
    Instances.free(instance)
    get<DynamicZones>().clear(instance)
    player.open("fade_in")
    showTabs()
}

fun CharacterContext<Player>.showTabs() {
    tabs.forEach {
        player.open(it)
    }
    player.clearMinimap()
}