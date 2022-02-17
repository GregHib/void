package world.gregs.voidps.world.interact.entity.effect

import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.npc.flagTransform
import world.gregs.voidps.engine.entity.character.update.visual.npc.transform
import world.gregs.voidps.engine.entity.character.update.visual.player.appearance
import world.gregs.voidps.engine.entity.character.update.visual.player.emote
import world.gregs.voidps.engine.entity.character.update.visual.player.flagAppearance
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.utility.inject

val provider: CollisionStrategyProvider by inject()
val definitions: NPCDefinitions by inject()

on<EffectStart>({ effect == "transform" }) { character: Character ->
    character["old_collision"] = character.collision
    character.collision = provider.get(definitions.get(character["transform", ""]))
}

on<EffectStop>({ effect == "transform" }) { player: Player ->
    player.emote = 1426
    player.size = Size.ONE
    player.appearance.apply {
        transform = -1
        size = Size.ONE.width
        idleSound = -1
        crawlSound = -1
        walkSound = -1
        runSound = -1
        soundDistance = 0
    }
    player.clear("transform")
    player.flagAppearance()
    player.collision = player.remove("old_collision") ?: return@on
}

on<EffectStop>({ effect == "transform" }) { npc: NPC ->
    npc.transform.id = -1
    npc.clear("transform")
    npc.flagTransform()
    npc.collision = npc.remove("old_collision") ?: return@on
}