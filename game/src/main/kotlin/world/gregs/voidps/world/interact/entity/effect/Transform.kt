package world.gregs.voidps.world.interact.entity.effect

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.data.definition.NPCDefinitions
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

var NPC.transform: String
    get() = this["transform_id", ""]
    set(value) {
        if (value.isBlank() || value == "-1") {
            softTimers.stop("transform")
            return
        }
        softTimers.start("transform")
        this["transform_id"] = value
        val definitions: NPCDefinitions = get()
        val definition = definitions.get(value)
        visuals.transform.id = definition.id
        flagTransform()
    }