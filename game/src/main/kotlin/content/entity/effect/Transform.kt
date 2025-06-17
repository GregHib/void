package content.entity.effect

import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.flagTransform
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider

fun Character.clearTransform() {
    if (this is Player) {
        appearance.apply {
            emote = 1426
            transform = -1
            size = 1
            idleSound = -1
            crawlSound = -1
            walkSound = -1
            runSound = -1
            soundDistance = 0
        }
        clear("transform_id")
        flagAppearance()
        collision = remove("old_collision") ?: return
    } else if (this is NPC) {
        visuals.transform.id = def.id
        clear("transform_id")
        flagTransform()
        collision = remove("old_collision") ?: return
    }
}

fun Character.transform(id: String) {
    if (id.isBlank() || id == "-1") {
        clearTransform()
        return
    }
    this["transform_id"] = id
    val definition = get<NPCDefinitions>().get(id)
    if (this is Player) {
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
    } else if (this is NPC) {
        visuals.transform.id = definition.id
        flagTransform()
    }
    this["old_collision"] = collision
    collision = get<CollisionStrategyProvider>().get(definition)
}

val Character.transform: String
    get() = this["transform_id", ""]
