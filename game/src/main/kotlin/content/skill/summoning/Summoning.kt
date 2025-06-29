package content.skill.summoning

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC

val Character?.isFamiliar: Boolean
    get() = this != null && this is NPC && id.endsWith("_familiar")
