package content.area.misthalin.varrock

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
import world.gregs.voidps.engine.entity.playerDespawn
import world.gregs.voidps.engine.event.Context
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.clear
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.engine.queue.*
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.combat.combatPrepare
import content.entity.effect.transform
import content.entity.gfx.areaGraphic
import content.entity.world.music.playTrack
import content.entity.proj.shoot
import content.entity.sound.playJingle
import content.entity.sound.playSound
import content.quest.*
import content.quest.free.demon_slayer.DemonSlayerSpell

val objects: GameObjects by inject()
val collisions: Collisions by inject()
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

fun Context<Player>.setCutsceneEnd(instance: Region) {
    player.queue("demon_slayer_delrith_cutscene_end", 1, LogoutBehaviour.Accelerate) {
        endCutscene(instance, defaultTile)
    }
}

fun Context<Player>.endCutscene(instance: Region, tile: Tile? = null) {
    val offset: Delta = player["demon_slayer_offset"] ?: return
    player.tele(tile ?: player.tile.minus(offset))
    stopCutscene(instance)
    player.clearCamera()
    destroyInstance(player)
}

move({ exitArea(it, to) }) { player ->
    destroyInstance(player)
}

playerDespawn { player ->
    if (player.contains("demon_slayer_instance")) {
        destroyInstance(player)
    }
}

fun exitArea(player: Player, to: Tile): Boolean {
    val offset: Delta = player["demon_slayer_offset"] ?: return false
    val actual = to.minus(offset)
    return !area.contains(actual) && !player.hasClock("demon_slayer_instance_exit")
}

fun destroyInstance(player: Player) {
    val offset: Delta? = player.remove("demon_slayer_offset")
    val target = if (offset != null && player.tile.minus(offset) in area) player.tile.minus(offset) else defaultTile
    player.start("demon_slayer_instance_exit", 2)
    player.tele(target)
    val instance: Region = player.remove("demon_slayer_instance") ?: return
    Instances.free(instance)
    val regionLevel = instance.toLevel(0)
    npcs.clear(regionLevel)
    for (zone in regionLevel.toCuboid().toZones()) {
        objects.clear(zone)
        collisions.clear(zone)
    }
}

suspend fun SuspendableContext<Player>.cutscene() {
    val region = Region(12852)
    val instance = startCutscene(region)
    val offset = instance.offset(region)
    player["demon_slayer_instance"] = instance
    player["demon_slayer_offset"] = offset
    player.steps.clear()
    player.mode = EmptyMode
    val wizard1 = npcs.add("dark_wizard_water", Tile(3226, 3371).add(offset), Direction.SOUTH_EAST) ?: return
    val wizard2 = npcs.add("dark_wizard_water_2", Tile(3229, 3371).add(offset), Direction.SOUTH_WEST) ?: return
    val wizard3 = npcs.add("dark_wizard_earth", Tile(3226, 3368).add(offset), Direction.NORTH_EAST) ?: return
    val denath = npcs.add("denath", Tile(3229, 3368).add(offset), Direction.NORTH_WEST) ?: return
    val delrith = npcs.add("delrith", Tile(3227, 3369).add(offset), Direction.SOUTH) ?: return
    npcs.removeIndex(delrith)
    val wizards = listOf(wizard1, wizard2, wizard3, denath)
    for (wizard in wizards) {
        wizard.mode = PauseMode
        wizard.steps.clear()
    }
    spawnEnergyBarrier(offset)
    delay(1)
    setCutsceneEnd(instance)
    player.tele(Tile(3222, 3367).add(offset))
    player.face(Direction.NORTH_EAST)
    player.playTrack("delrith")

    if (player["demon_slayer_summoned", false]) {
        player.queue.clear("demon_slayer_delrith_cutscene_end")
        delrith.tele(Tile(3227, 3367).add(offset))
        denath.tele(Tile(3236, 3368).add(offset))
        npcs.index(delrith)
        showTabs()
        return
    }
    delay(1)
    for (wizard in wizards) {
        wizard.anim("summon_demon")
    }

    player.clearCamera()
    player.moveCamera(Tile(3224, 3376).add(offset), 475, 232, 232)
    player.turnCamera(Tile(3227, 3369).add(offset), 300, 232, 232)
    player.moveCamera(Tile(3231, 3376).add(offset), 475, 1, 1)
    npc<Happy>("denath", "Arise, O mighty Delrith! Bring destruction to this soft, weak city!")
    for (wizard in wizards) {
        wizard.say("Arise, Delrith!")
    }
    npc<Neutral>("dark_wizard_water", "Arise, Delrith!", title = "Dark wizards")

    statement("The wizards cast an evil spell", clickToContinue = false)
    val regular = objects[Tile(3227, 3369).add(offset), "demon_slayer_stone_table"]!!
    val table = objects.replace(regular, "demon_slayer_stone_table_summoning", ticks = 8)
    player.clearCamera()
    player.turnCamera(Tile(3227, 3369).add(offset), 100, 232, 232)
    player.moveCamera(Tile(3227, 3365).add(offset), 500, 232, 232)
    player.playSound("summon_npc")
    player.playSound("demon_slayer_table_explosion")
    delay(1)
    table.animate("demon_slayer_table_light")
    delay(1)
    player.shakeCamera(15, 0, 0, 0, 0)
    for ((source, target) in targets) {
        source.add(offset).shoot("demon_slayer_spell", target.add(offset))
    }
    delay(1)
    player.shakeCamera(0, 0, 0, 0, 0)
    for ((_, target) in targets) {
        areaGraphic("demon_slayer_spell_hit", target.add(offset))
    }
    delay(2)
    npcs.index(delrith)
    delrith.anim("delrith_appear")
    delay(2)
    player.playSound("demon_slayer_break_table", delay = 10)
    player.playSound("demon_slayer_delrith_appear")
    player.turnCamera(Tile(3227, 3369).add(offset), 400, 1, 1)
    player["demon_slayer_summoned"] = true
    delay(5)
    delrith.walkToDelay(Tile(3227, 3367).add(offset))
    delay(2)
    player.clearCamera()
    player.moveCamera(Tile(3226, 3375).add(offset), 500, 232, 232)
    player.turnCamera(Tile(3227, 3367).add(offset), 300, 232, 232)
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
    denath.walkToDelay(Tile(3236, 3368).add(offset))
    player.clearCamera()
    player.moveCamera(Tile(3226, 3383).add(offset), 1000, 1, 1)
    npc<Shifty>("denath", "I've got to get out of here...")
    player.queue.clear("demon_slayer_delrith_cutscene_end")
    showTabs()
    player.clearCamera()
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

npcOperate("*", "delrith") {
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
                target.anim("delrith_continue")
                delay(1)
                correct = false
                npcs.remove(target)
                npcs.removeIndex(target)
                delay(2)
            } else {
                delay(3)
            }
        }
        if (correct) {
            target.anim("delrith_death")
            player.playSound("demon_slayer_delrith_banished")
            statement("Delrith is sucked into the vortex...", clickToContinue = false)
            delay(14)
            npcs.remove(target)
            npcs.removeIndex(target)
            statement("...back into the dark dimension from which he came.")
            destroyInstance(player)
            questComplete()
        } else {
            statement("The vortex collapses. That was the wrong incantation.")
        }
    }
}


npcLevelChange("delrith", Skill.Constitution) { npc ->
    if (to > 0) {
        return@npcLevelChange
    }
//    player.playSound("demon_slayer_portal_open")
    npc.transform = "delrith_weakened"
    npc.mode = PauseMode
}

fun Context<Player>.questComplete() {
    player.anim("silverlight_showoff")
    player.gfx("silverlight_sparkle")
    player.playSound("equip_silverlight")
    player.playJingle("quest_complete_1")
    player["demon_slayer"] = "completed"
    player.inc("quest_points", 3)
    DemonSlayerSpell.clear(player)
    player.softQueue("quest_complete", 1) {
        player.sendQuestComplete(
            "Demon Slayer",
            "3 Quest Points",
            "Silverlight",
            item = "silverlight"
        )
    }
}

fun spawnEnergyBarrier(offset: Delta) {
    var tile = Tile(3221, 3367).add(offset)
    var rotation = 0
    var direction = Direction.NORTH
    while (rotation < 4) {
        repeat(6) {
            objects.add("demon_slayer_energy_barrier", tile, 0, rotation)
            tile = tile.add(direction)
        }
        direction = direction.rotate(1)
        repeat(3) {
            objects.add("demon_slayer_energy_barrier", tile, 9, rotation)
            objects.add("demon_slayer_energy_barrier", tile.add(direction.rotate(1)), 1, rotation)
            tile = tile.add(direction)
        }
        objects.add("demon_slayer_energy_barrier", tile, 9, rotation)
        rotation++
        direction = direction.rotate(1)
        tile = tile.add(direction)
    }
}
