package world.gregs.voidps.world.interact.entity.effect

import world.gregs.voidps.engine.client.variable.clearVar
import world.gregs.voidps.engine.client.variable.removeVar
import world.gregs.voidps.engine.data.definition.extra.NPCDefinitions
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.flagTransform
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop

val collision: CollisionStrategyProvider by inject()
val definitions: NPCDefinitions by inject()

on<TimerStart>({ timer == "transform" }) { character: Character ->
    val def = definitions.get(character["transform", ""])
    character["old_collision"] = character.collision
    character.collision = collision.get(def)
}

on<TimerStop>({ timer == "transform" }) { player: Player ->
    player.size = Size.ONE
    player.appearance.apply {
        emote = 1426
        transform = -1
        size = Size.ONE.width
        idleSound = -1
        crawlSound = -1
        walkSound = -1
        runSound = -1
        soundDistance = 0
    }
    player.clearVar("transform")
    player.flagAppearance()
    player.collision = player.removeVar("old_collision") ?: return@on
}

on<TimerStop>({ timer == "transform" }) { npc: NPC ->
    npc.visuals.transform.reset()
    npc.clearVar("transform")
    npc.flagTransform()
    npc.collision = npc.removeVar("old_collision") ?: return@on
}