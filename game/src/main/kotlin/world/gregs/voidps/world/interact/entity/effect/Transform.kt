package world.gregs.voidps.world.interact.entity.effect

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.npc.flagTransform
import world.gregs.voidps.engine.entity.character.update.visual.npc.transform
import world.gregs.voidps.engine.entity.character.update.visual.player.appearance
import world.gregs.voidps.engine.entity.character.update.visual.player.emote
import world.gregs.voidps.engine.entity.character.update.visual.player.flagAppearance
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.entity.stop
import world.gregs.voidps.engine.utility.get

fun Player.transform(npc: String) {
    if (npc.isBlank()) {
        stop("transform")
        return
    }
    transform(get<NPCDefinitions>().get(npc))
}

private fun Player.transform(definition: NPCDefinition) {
    start("transform")
    emote = definition.renderEmote
    size = Size(definition.size, definition.size)
    appearance.apply {
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
        stop("transform")
        return
    }
    start("transform")
    val definitions: NPCDefinitions = get()
    val definition = definitions.get(npc)
    transform.id = definition.id
    flagTransform()
}