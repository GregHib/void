package world.gregs.voidps.world.interact.entity.effect

import world.gregs.voidps.engine.entity.EffectStop
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.npc.flagTransform
import world.gregs.voidps.engine.entity.character.update.visual.npc.transform
import world.gregs.voidps.engine.entity.character.update.visual.player.appearance
import world.gregs.voidps.engine.entity.character.update.visual.player.emote
import world.gregs.voidps.engine.entity.character.update.visual.player.flagAppearance
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.TraversalType
import world.gregs.voidps.engine.path.traverse.SmallTraversal
import world.gregs.voidps.engine.utility.get

on<EffectStop>({ effect == "transform" }) { player: Player ->
    player.emote = 1426
    player.size = Size.ONE
    val collisions: Collisions = get()
    player.movement.traversal = SmallTraversal(TraversalType.Land, false, collisions)
    player.appearance.apply {
        transform = -1
        size = Size.ONE.width
        idleSound = -1
        crawlSound = -1
        walkSound = -1
        runSound = -1
        soundDistance = 0
    }
    player.flagAppearance()
}

on<EffectStop>({ effect == "transform" }) { npc: NPC ->
    npc.transform.id = -1
    npc.flagTransform()
}