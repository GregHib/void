package world.gregs.voidps.world.interact.entity.effect

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.data.definition.extra.NPCDefinitions
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.flagTransform
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.get

fun Player.transform(npc: String) {
    if (npc.isBlank()) {
        softTimers.stop("transform")
        return
    }
    this["transform"] = npc
    transform(get<NPCDefinitions>().get(npc))
}

private fun Player.transform(definition: NPCDefinition) {
    softTimers.start("transform")
    size = Size(definition.size, definition.size)
    appearance.apply {
        emote = definition.renderEmote
        transform = definition.id
        size = definition.size
        idleSound = definition.idleSound
        crawlSound = definition.crawlSound
        walkSound = definition.walkSound
        runSound = definition.runSound
        soundDistance = definition.soundDistance
    }
    flagAppearance()
}

fun NPC.transform(npc: String) {
    if (npc.isBlank()) {
        softTimers.stop("transform")
        return
    }
    softTimers.start("transform")
    this["transform"] = npc
    val definitions: NPCDefinitions = get()
    val definition = definitions.get(npc)
    visuals.transform.id = definition.id
    flagTransform()
}