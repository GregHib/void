package content.area.misthalin.varrock

import content.entity.combat.combatPrepare
import content.entity.combat.npcCombatPrepare
import content.entity.death.Death
import content.entity.effect.transform
import content.entity.gfx.areaGfx
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.proj.shoot
import content.entity.sound.jingle
import content.entity.sound.sound
import content.entity.world.music.playTrack
import content.quest.Cutscene
import content.quest.free.demon_slayer.DemonSlayerSpell
import content.quest.questComplete
import content.quest.questCompleted
import content.quest.startCutscene
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.shakeCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.character.mode.move.move
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.npcLevelChange
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.entity.playerDespawn
import world.gregs.voidps.engine.event.Context
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

val objects: GameObjects by inject()
val npcs: NPCs by inject()
val areas: AreaDefinitions by inject()

val area = areas["demon_slayer_stone_circle"]
val defaultTile = Tile(3220, 3367)
val targets = listOf(
    Tile(3227, 3369) to Tile(3224, 3366),
    Tile(3227, 3370) to Tile(3231, 3366),
    Tile(3228, 3369) to Tile(3224, 3373),
    Tile(3228, 3370) to Tile(3231, 3373)
)

enterArea("demon_slayer_stone_circle") {
    if (!player.questCompleted("demon_slayer") && player["demon_slayer_silverlight", false] && !player.hasClock("demon_slayer_instance_exit")) {
        cutscene()
    }
}

move({ exitArea(it, to) }) { player ->
    val cutscene: Cutscene = player.remove("demon_slayer_cutscene") ?: return@move
    cutscene.end(this)
}

playerDespawn { player ->
    val cutscene: Cutscene = player.remove("demon_slayer_cutscene") ?: return@playerDespawn
    cutscene.destroy()
}

fun exitArea(player: Player, to: Tile): Boolean {
    val cutscene: Cutscene = player["demon_slayer_cutscene"] ?: return false
    val actual = cutscene.original(to)
    return !area.contains(actual) && !player.hasClock("demon_slayer_instance_exit")
}

suspend fun SuspendableContext<Player>.cutscene() {
    val region = Region(12852)
    val cutscene = startCutscene("demon_slayer_delrith", region)
    player["demon_slayer_cutscene"] = cutscene
    player.steps.clear()
    player.mode = EmptyMode
    val wizard1 = npcs.add("dark_wizard_water", cutscene.tile(3226, 3371), Direction.SOUTH_EAST)
    val wizard2 = npcs.add("dark_wizard_water_2", cutscene.tile(3229, 3371), Direction.SOUTH_WEST)
    val wizard3 = npcs.add("dark_wizard_earth", cutscene.tile(3226, 3368), Direction.NORTH_EAST)
    val denath = npcs.add("denath", cutscene.tile(3229, 3368), Direction.NORTH_WEST)
    val delrith = npcs.add("delrith", cutscene.tile(3227, 3369), Direction.SOUTH)
    delrith.hide = true
    val wizards = listOf(wizard1, wizard2, wizard3, denath)
    for (wizard in wizards) {
        wizard.mode = PauseMode
        wizard.steps.clear()
    }
    spawnEnergyBarrier(cutscene)
    delay(1)
    cutscene.onEnd {
        player.start("demon_slayer_instance_exit", 2)
        if (player.tile.region == cutscene.instance) {
            player.tele(cutscene.original(player.tile))
        } else {
            player.tele(defaultTile)
        }
        player.clearCamera()
    }
    player.face(Direction.NORTH_EAST)
    player.playTrack("delrith")
    if (player["demon_slayer_summoned", false]) {
        player.queue.clear("demon_slayer_delrith_cutscene_end")
        delrith.tele(cutscene.tile(3227, 3367))
        denath.tele(cutscene.tile(3236, 3368))
        player.tele(cutscene.convert(player.tile)) // TODO could be improved by getting nearest tile in inner circle area
        delrith.hide = false
        cutscene.showTabs()
        return
    }
    player.tele(cutscene.tile(3222, 3367))
    delay(1)
    for (wizard in wizards) {
        wizard.anim("summon_demon")
    }

    player.clearCamera()
    player.moveCamera(cutscene.tile(3224, 3376), 475, 232, 232)
    player.turnCamera(cutscene.tile(3227, 3369), 300, 232, 232)
    player.moveCamera(cutscene.tile(3231, 3376), 475, 1, 1)
    npc<Happy>("denath", "Arise, O mighty Delrith! Bring destruction to this soft, weak city!")
    for (wizard in wizards) {
        wizard.say("Arise, Delrith!")
    }
    npc<Neutral>("dark_wizard_water", "Arise, Delrith!", title = "Dark wizards")

    statement("The wizards cast an evil spell", clickToContinue = false)
    val regular = objects[cutscene.tile(3227, 3369), "demon_slayer_stone_table"]!!
    val table = objects.replace(regular, "demon_slayer_stone_table_summoning", ticks = 8)
    player.clearCamera()
    player.turnCamera(cutscene.tile(3227, 3369), 100, 232, 232)
    player.moveCamera(cutscene.tile(3227, 3365), 500, 232, 232)
    player.sound("summon_npc")
    player.sound("demon_slayer_table_explosion")
    delay(1)
    table.anim("demon_slayer_table_light")
    delay(1)
    player.shakeCamera(15, 0, 0, 0, 0)
    for ((source, target) in targets) {
        cutscene.convert(source).shoot("demon_slayer_spell", cutscene.convert(target))
    }
    delay(1)
    player.shakeCamera(0, 0, 0, 0, 0)
    for ((_, target) in targets) {
        areaGfx("demon_slayer_spell_impact", cutscene.convert(target))
    }
    delay(2)
    delrith.hide = false
    delrith.anim("delrith_appear")
    delay(2)
    player.sound("demon_slayer_break_table", delay = 10)
    player.sound("demon_slayer_delrith_appear")
    player.turnCamera(cutscene.tile(3227, 3369), 400, 1, 1)
    player["demon_slayer_summoned"] = true
    delay(5)
    delrith.walkOverDelay(cutscene.tile(3227, 3367))
    delay(2)
    player.clearCamera()
    player.moveCamera(cutscene.tile(3226, 3375), 500, 232, 232)
    player.turnCamera(cutscene.tile(3227, 3367), 300, 232, 232)
    delay(1)
    delrith.face(denath)
    for (wizard in wizards) {
        wizard.clearAnim()
        wizard.face(delrith)
    }
    npc<Chuckle>(
        "denath", """
        Ha ha ha! At last you are free, my demonic brother!
        Rest now, and then have your revenge on this pitiful
        city!
    """
    )
    for (wizard in wizards) {
        wizard.face(player)
    }
    delrith.face(player)
    npc<Surprised>("dark_wizard_earth", "Who's that?")
    npc<Afraid>("denath", "Noo! Not Silverlight! Delrith is not ready yet!")
    denath.walkToDelay(cutscene.tile(3236, 3368))
    player.clearCamera()
    player.moveCamera(cutscene.tile(3226, 3383), 1000, 1, 1)
    npc<Shifty>("denath", "I've got to get out of here...")
    player.queue.clear("demon_slayer_delrith_cutscene_end")
    cutscene.end(this)
    for (wizard in wizards) {
        wizard.mode = EmptyMode
    }
}

combatPrepare("melee") { player ->
    if (target is NPC && target.id == "delrith" && target.transform == "delrith_weakened") {
        cancel()
        player.strongQueue("banish_delrith", 1) {
            player.mode = Interact(player, target, NPCOption(player, target, target.def, "Banish"))
        }
    }
}

val words = listOf("Carlem", "Aber", "Camerinthum", "Purchai", "Gabindo")

npcOperate("Banish", "delrith") {
    if (target.transform != "delrith_weakened") {
        return@npcOperate
    }
    player.weakQueue("banish_delrith") {
        player<Angry>("Now what was that incantation again?")
        var correct = true
        repeat(5) { index ->
            val choice = choice(listOf("Carlem", "Aber", "Camerinthum", "Purchai", "Gabindo"))
            val selected = words[choice - 1]
            val suffix = if (index == 4) "!" else "..."
            val text = "$selected$suffix"
            player.say(text)
            player<Talk>(text, largeHead = true, clickToContinue = false)
            val expected = DemonSlayerSpell.getWord(player, index + 1)
            if (selected != expected) {
                correct = false
                target.anim("delrith_continue")
                delay(2)
                npcs.remove(target)
                delay(1)
            } else {
                delay(3)
            }
        }
        if (correct) {
            target.anim("delrith_death")
            player.sound("demon_slayer_delrith_banished")
            statement("Delrith is sucked into the vortex...", clickToContinue = false)
            delay(14)
            npcs.remove(target)
            statement("...back into the dark dimension from which he came.")
            val cutscene: Cutscene? = player.remove("demon_slayer_cutscene")
            if (cutscene != null) {
                cutscene.end(this)
            } else {
                player.tele(defaultTile)
            }
            questComplete()
        } else {
            statement("The vortex collapses. That was the wrong incantation.")
        }
    }
}

npcCombatPrepare("delrith") {
    if (it.levels.get(Skill.Constitution) <= 0) {
        cancel()
    }
}

npcLevelChange("delrith", Skill.Constitution) { npc ->
    if (to > 0) {
        return@npcLevelChange
    }
    if (npc.queue.contains("death")) {
        npc.queue.clear("death")
    }
    npc.strongQueue("death", TimeUnit.MINUTES.toTicks(5)) {
        npc.emit(Death)
    }
//    player.playSound("demon_slayer_portal_open")
    npc.transform("delrith_weakened")
    npc.mode = PauseMode
}

fun Context<Player>.questComplete() {
    player.anim("silverlight_showoff")
    player.gfx("silverlight_sparkle")
    player.sound("equip_silverlight")
    player.jingle("quest_complete_1")
    player["demon_slayer"] = "completed"
    player.inc("quest_points", 3)
    DemonSlayerSpell.clear(player)
    player.softQueue("quest_complete", 1) {
        player.questComplete(
            "Demon Slayer",
            "3 Quest Points",
            "Silverlight",
            item = "silverlight"
        )
    }
}

/**
 * Spawns energy barriers in a clockwise ring
 */
fun spawnEnergyBarrier(cutscene: Cutscene) {
    var tile = cutscene.tile(3221, 3367)
    var rotation = 0
    var direction = Direction.NORTH
    while (rotation < 4) {
        repeat(6) {
            objects.add("demon_slayer_energy_barrier", tile, ObjectShape.WALL_STRAIGHT, rotation)
            tile = tile.add(direction)
        }
        direction = direction.rotate(1)
        repeat(3) {
            objects.add("demon_slayer_energy_barrier", tile, ObjectShape.WALL_DIAGONAL, rotation)
            objects.add("demon_slayer_energy_barrier", tile.add(direction.rotate(1)), ObjectShape.WALL_DIAGONAL_CORNER, rotation)
            tile = tile.add(direction)
        }
        objects.add("demon_slayer_energy_barrier", tile, ObjectShape.WALL_DIAGONAL, rotation)
        rotation++
        direction = direction.rotate(1)
        tile = tile.add(direction)
    }
}
