package content.skill.agility.course

import content.entity.combat.hit.damage
import content.entity.effect.clearTransform
import content.entity.gfx.areaGraphic
import content.entity.sound.playSound
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

objectOperate("Jump-to", "ape_atoll_stepping_stones") {
    if (!player.has(Skill.Agility, 35, message = true)) {
        return@objectOperate
    }
    if (!player.equipped(EquipSlot.Weapon).id.endsWith("_greegree")) {
        player.message("The rock is covered in slime and you slip into the water...")
        player.anim("stepping_stone_jump")
        player.playSound("2461")
        player.exactMoveDelay(target.tile, direction = Direction.WEST)
        player.anim("rope_walk_fall_down")
        player.playSound("stumble_loop", repeat = 10)
        delay()
        player.clearAnim()
        player.renderEmote("swim")
        player.walkOverDelay(target.tile.addY(1))
        player.playSound("2461")
        areaGraphic("68", target.tile.addY(1), delay = 3)
        player.playSound("2496")
        delay()
        player.walkOverDelay(Tile(2757, 2748), forceWalk = true)
        player.message("...you're not monkey enough to try this!")
        player.damage(7)
        player.clearRenderEmote()
        return@objectOperate
    }
    player.face(target)
    arriveDelay()
    player.agilityCourse("ape_atoll")
    player.anim("3481")
    delay(1)
    player.playSound("2461")
    player.exactMoveDelay(Tile(2754, 2742), delay = 30)
    delay(3)
    player.anim("3481")
    delay(1)
    player.playSound("2461")
    player.exactMoveDelay(Tile(2753, 2742))
//    player.exp(Skill.Agility, 40.0)
}

objectOperate("Climb", "ape_atoll_tropical_tree") {
    if (!player.equipped(EquipSlot.Weapon).id.endsWith("_greegree")) {
        player.message("Only the stealthiest and most agile monkey can use this!")
        return@objectOperate
    }
    player.anim("3487")
    player.playSound("2454", repeat = 3)
    delay(3)
    player.tele(2753, 2742, 2)
    player.exp(Skill.Agility, 40.0)
}

objectOperate("Swing Across", "ape_atoll_monkeybars") {
    if (!player.equipped(EquipSlot.Weapon).id.endsWith("_greegree")) {
        player.anim("742")
        player.renderEmote("monkey_bars")
        player.playSound("2474")
        player.walkToDelay(Tile(2751, y = 2741, 2))
        player.playSound("2466", delay = 30)
        player.walkToDelay(Tile(2750, y = 2741, 2))
        player.anim("764")
        player.tele(2753, 2742, 2)
        player.message("Your hands slip from the rung...")
        player.message("...you're not monkey enough to try this!")
        player.clearAnim()
        player.renderEmote("swim")
        player.tele(2750, y = 2742, 0)
        player.playSound("watersplash")
        player.playSound("1436", repeat = 9)
        player.walkOverDelay(Tile(2751, y = 2743))
        player.walkOverDelay(Tile(2752, y = 2744))
        player.walkOverDelay(Tile(2753, y = 2745))
        player.walkOverDelay(Tile(2754, y = 2745))
        player.walkOverDelay(Tile(2755, y = 2746))
        player.walkOverDelay(Tile(2756, y = 2747))
        player.walkOverDelay(Tile(2756, y = 2748))
        player.walkOverDelay(Tile(2757, 2748))
        player.damage(7)
        player.clearTransform()
        return@objectOperate
    }
    player.anim("3482")
    player.walkOverDelay(Tile(2747, 2741, 2))
    delay(3)
    player.anim("3484")
    player.exp(Skill.Agility, 40.0)
}