package world.gregs.voidps.world.interact.entity.effect

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.data.definition.extra.NPCDefinitions
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.flagTransform
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.get

fun Player.clearTransform() {
    softTimers.stop("transform")
}

fun Player.transform(npc: String) {
    if (npc.isBlank() || npc == "-1") {
        clearTransform()
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
    if (npc.isBlank() || npc == "-1") {
        softTimers.stop("transform")
        return
    }
    softTimers.start("transform")
    this["transform_id"] = npc
    val definitions: NPCDefinitions = get()
    val definition = definitions.get(npc)
    visuals.transform.id = definition.id
    flagTransform()
}