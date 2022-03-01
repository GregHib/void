package world.gregs.voidps.world.interact.entity.effect

import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.flagTransform
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.path.traverse.*
import world.gregs.voidps.engine.utility.inject

val collision: CollisionStrategyProvider by inject()
val definitions: NPCDefinitions by inject()

on<EffectStart>({ effect == "transform" }) { character: Character ->
    val def = definitions.get(character["transform", ""])
    character["old_collision"] = character.collision
    character.collision = collision.get(def)
    if (def["swim", false] || def.size != character.size.width) {
        character["old_traversal"] = character.traversal
        character.traversal = when {
            def["swim", false] -> SwimTraversal
            def.size == 1 -> SmallTraversal
            def.size == 2 -> MediumTraversal
            else -> LargeTraversal
        }
    }
}

on<EffectStop>({ effect == "transform" }) { player: Player ->
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
    player.clear("transform")
    player.flagAppearance()
    player.remove<CollisionStrategy>("old_collision")?.let {
        player.collision = it
    }
    player.remove<TileTraversalStrategy>("old_traversal")?.let {
        player.traversal = it
    }
}

on<EffectStop>({ effect == "transform" }) { npc: NPC ->
    npc.visuals.transform.reset()
    npc.clear("transform")
    npc.flagTransform()
    npc.remove<CollisionStrategy>("old_collision")?.let {
        npc.collision = it
    }
    npc.remove<TileTraversalStrategy>("old_traversal")?.let {
        npc.traversal = it
    }
}