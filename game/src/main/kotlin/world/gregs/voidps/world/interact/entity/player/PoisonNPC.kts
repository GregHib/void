import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.update.visual.Hit
import world.gregs.voidps.engine.entity.character.update.visual.hit
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.player.cure

on<EffectStart>({ effect == "poison" }) { npc: NPC ->
    delay(0) {
        damage(npc)
    }
    npc["poison_job"] = delay(npc, 30, loop = true) {
        damage(npc)
    }
}

on<EffectStop>({ effect == "poison" }) { npc: NPC ->
    npc.clear("poison_job")
    npc.clear("poison_damage")
}

fun damage(npc: NPC) {
    val damage = npc["poison_damage", 0]
    if (damage <= 10) {
        npc.cure()
        return
    }
    npc["poison_damage"] = damage - 2
    npc.hit(npc, damage, Hit.Mark.Poison)
}