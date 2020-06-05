package rs.dusk.world.entity.npc

import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.engine.Tick
import rs.dusk.engine.model.entity.index.npc.NPCs
import rs.dusk.engine.model.entity.index.update.visual.forceChat
import rs.dusk.engine.model.entity.index.update.visual.setAnimation
import rs.dusk.utility.inject
import kotlin.random.Random

val npcs: NPCs by inject()

val eatGrassAnimation = 5854
var mooCounter = 0

Tick where { mooCounter-- <= 0 } then {
    val cow = npcs.indexed.filterNotNull().filter { it.def.name == "Cow" }.random()
    cow.movement.clear()
    cow.forceChat = "Moo"
    cow.setAnimation(eatGrassAnimation)
    mooCounter = Random.nextInt(8, 16)
}