package world.gregs.voidps.world.interact.entity.effect

import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.npc.flagTransform
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.timer.characterTimerStart
import world.gregs.voidps.engine.timer.npcTimerStop
import world.gregs.voidps.engine.timer.timerStop

val collision: CollisionStrategyProvider by inject()
val definitions: NPCDefinitions by inject()

characterTimerStart("transform") { character ->
    val def = definitions.get(character["transform_id", ""])
    character["old_collision"] = character.collision
    character.collision = collision.get(def)
}

timerStop("transform") { player ->
    player.appearance.apply {
        emote = 1426
        transform = -1
        size = 1
        idleSound = -1
        crawlSound = -1
        walkSound = -1
        runSound = -1
        soundDistance = 0
    }
    player.clear("transform_id")
    player.flagAppearance()
    player.collision = player.remove("old_collision") ?: return@timerStop
}

npcTimerStop("transform") { npc ->
    npc.visuals.transform.reset()
    npc.clear("transform_id")
    npc.flagTransform()
    npc.collision = npc.remove("old_collision") ?: return@npcTimerStop
}