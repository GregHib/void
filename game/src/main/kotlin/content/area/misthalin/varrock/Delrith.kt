package content.area.misthalin.varrock

import content.entity.effect.transform
import content.entity.gfx.areaGfx
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.proj.shoot
import content.entity.world.music.playTrack
import content.quest.Cutscene
import content.quest.clearInstance
import content.quest.exitInstance
import content.quest.free.demon_slayer.DemonSlayerSpell
import content.quest.instanceOffset
import content.quest.quest
import content.quest.questComplete
import content.quest.questCompleted
import content.quest.smallInstance
import content.quest.startCutscene
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.shakeCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.Death
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

class Delrith : Script {

    val area = Areas["demon_slayer_stone_circle"]
    val targets = listOf(
        Tile(3227, 3369) to Tile(3224, 3366),
        Tile(3227, 3370) to Tile(3231, 3366),
        Tile(3228, 3369) to Tile(3224, 3373),
        Tile(3228, 3370) to Tile(3231, 3373),
    )

    val words = listOf("Carlem", "Aber", "Camerinthum", "Purchai", "Gabindo")

    init {
        moved {
            if (!exitArea(this, it, tile)) {
                return@moved
            }
            start("demon_slayer_instance_exit", 2)
            tele(tile.minus(instanceOffset()))
            clearInstance()
            val cutscene: Cutscene = remove("demon_slayer_cutscene") ?: return@moved
            Script.launch {
                cutscene.end(destroyInstance = false)
            }
        }

        entered("demon_slayer_stone_circle") {
            // Second area check to make sure we're not triggering a second time when entering the instanced area
            if (!questCompleted("demon_slayer") && get("demon_slayer_silverlight", false) && tile in it.area && !hasClock("demon_slayer_instance_exit")) {
                Script.launch {
                    cutscene()
                }
            }
        }

        combatPrepare("melee") { target ->
            if (target is NPC && target.id == "delrith" && target.transform == "delrith_weakened") {
                strongQueue("banish_delrith", 1) {
                    interactNpc(target, "Banish")
                }
                false
            } else {
                true
            }
        }

        npcOperate("Banish", "delrith_weakened") { (target) ->
            weakQueue("banish_delrith") {
                player<Angry>("Now what was that incantation again?")
                var correct = true
                repeat(5) { index ->
                    val choice = choice(words)
                    val selected = words[choice - 1]
                    val suffix = if (index == 4) "!" else "..."
                    val text = "$selected$suffix"
                    say(text)
                    player<Neutral>(text, largeHead = true, clickToContinue = false)
                    val expected = DemonSlayerSpell.getWord(player, index + 1)
                    if (selected != expected) {
                        correct = false
                        target.anim("delrith_continue")
                        delay(2)
                        NPCs.remove(target)
                        delay(1)
                    } else {
                        delay(3)
                    }
                }
                if (!correct) {
                    statement("The vortex collapses. That was the wrong incantation.")
                    return@weakQueue
                }
                target.anim("delrith_death")
                sound("demon_slayer_delrith_banished")
                statement("Delrith is sucked into the vortex...", clickToContinue = false)
                delay(14)
                NPCs.remove(target)
                statement("...back into the dark dimension from which he came.")
                start("demon_slayer_instance_exit", 2)
                exitInstance()
                remove<Cutscene>("demon_slayer_cutscene")?.end(destroyInstance = false)
                questComplete()
            }
        }

        npcCombatPrepare("delrith") {
            levels.get(Skill.Constitution) > 0
        }

        npcLevelChanged(Skill.Constitution, "delrith", ::weaken)
    }

    fun weaken(npc: NPC, skill: Skill, from: Int, to: Int) {
        if (to > 0) {
            return
        }
        if (npc.queue.contains("death")) {
            npc.queue.clear("death")
        }
        npc.strongQueue("death", TimeUnit.MINUTES.toTicks(5)) {
            Death.killed(npc)
        }
        //    player.playSound("demon_slayer_portal_open")
        npc.transform("delrith_weakened")
        npc.mode = PauseMode
    }

    fun exitArea(player: Player, from: Tile, to: Tile): Boolean {
        if (!player.contains("instance") || player.questCompleted("demon_slayer") || player.quest("demon_slayer") == "unstarted") {
            return false
        }
        val offset = player.instanceOffset()
        if (!area.contains(from.minus(offset))) {
            return false
        }
        val original = to.minus(offset)
        return !area.contains(original) && !player.hasClock("demon_slayer_instance_exit")
    }

    suspend fun Player.cutscene() {
        val region = Region(12852)
        val instance = smallInstance(region)
        val offset = instanceOffset()
        steps.clear()
        mode = EmptyMode
        val wizard1 = NPCs.add("dark_wizard_water", offset.tile(3226, 3371), Direction.SOUTH_EAST)
        val wizard2 = NPCs.add("dark_wizard_water_2", offset.tile(3229, 3371), Direction.SOUTH_WEST)
        val wizard3 = NPCs.add("dark_wizard_earth", offset.tile(3226, 3368), Direction.NORTH_EAST)
        val denath = NPCs.add("denath", offset.tile(3229, 3368), Direction.NORTH_WEST)
        val delrith = NPCs.add("delrith", offset.tile(3227, 3369), Direction.SOUTH)
        delrith.hide = true
        delrith.mode = PauseMode
        val wizards = listOf(wizard1, wizard2, wizard3, denath)
        for (wizard in wizards) {
            wizard.mode = PauseMode
            wizard.steps.clear()
        }
        spawnEnergyBarrier(offset)
        delay(1)
        face(Direction.NORTH_EAST)
        playTrack("delrith")
        if (get("demon_slayer_summoned", false)) {
            delrith.tele(offset.tile(3227, 3367))
            denath.tele(offset.tile(3236, 3368))
            tele(tile.add(offset)) // TODO could be improved by getting nearest tile in inner circle area
            delrith.hide = false
            delrith.mode = EmptyMode
            return
        }
        val cutscene = startCutscene("demon_slayer_cutscene", instance, offset)
        set("demon_slayer_cutscene", cutscene)
        cutscene.onEnd(destroyInstance = false) {
            clearCamera()
        }
        tele(cutscene.tile(3222, 3367))
        delay(1)
        for (wizard in wizards) {
            wizard.anim("summon_demon")
        }

        clearCamera()
        moveCamera(cutscene.tile(3224, 3376), 475, 232, 232)
        turnCamera(cutscene.tile(3227, 3369), 300, 232, 232)
        moveCamera(cutscene.tile(3231, 3376), 475, 1, 1)
        npc<Happy>("denath", "Arise, O mighty Delrith! Bring destruction to this soft, weak city!")
        for (wizard in wizards) {
            wizard.say("Arise, Delrith!")
        }
        npc<Idle>("dark_wizard_water", "Arise, Delrith!", title = "Dark wizards")

        statement("The wizards cast an evil spell", clickToContinue = false)
        val regular = GameObjects.find(cutscene.tile(3227, 3369), "demon_slayer_stone_table")
        val table = GameObjects.replace(regular, "demon_slayer_stone_table_summoning", ticks = 8)
        clearCamera()
        turnCamera(cutscene.tile(3227, 3369), 100, 232, 232)
        moveCamera(cutscene.tile(3227, 3365), 500, 232, 232)
        sound("summon_npc")
        sound("demon_slayer_table_explosion")
        delay(1)
        table.anim("demon_slayer_table_light")
        delay(1)
        shakeCamera(15, 0, 0, 0, 0)
        for ((source, target) in targets) {
            cutscene.convert(source).shoot("demon_slayer_spell", cutscene.convert(target))
        }
        delay(1)
        shakeCamera(0, 0, 0, 0, 0)
        for ((_, target) in targets) {
            areaGfx("demon_slayer_spell_impact", cutscene.convert(target))
        }
        delay(2)
        delrith.hide = false
        delrith.anim("delrith_appear")
        delay(2)
        sound("demon_slayer_break_table", delay = 10)
        sound("demon_slayer_delrith_appear")
        turnCamera(cutscene.tile(3227, 3369), 400, 1, 1)
        set("demon_slayer_summoned", true)
        delay(5)
        delrith.walkOverDelay(cutscene.tile(3227, 3367))
        delay(2)
        clearCamera()
        moveCamera(cutscene.tile(3226, 3375), 500, 232, 232)
        turnCamera(cutscene.tile(3227, 3367), 300, 232, 232)
        delay(1)
        delrith.face(denath)
        for (wizard in wizards) {
            wizard.clearAnim()
            wizard.face(delrith)
        }
        npc<Laugh>(
            "denath",
            """
            Ha ha ha! At last you are free, my demonic brother!
            Rest now, and then have your revenge on this pitiful
            city!
        """,
        )
        for (wizard in wizards) {
            wizard.face(this)
        }
        delrith.face(this)
        npc<Shock>("dark_wizard_earth", "Who's that?")
        npc<Scared>("denath", "Noo! Not Silverlight! Delrith is not ready yet!")
        denath.walkToDelay(cutscene.tile(3236, 3368))
        clearCamera()
        moveCamera(cutscene.tile(3226, 3383), 1000, 1, 1)
        npc<Shifty>("denath", "I've got to get out of here...")
        queue.clear("demon_slayer_delrith_cutscene_end")
        cutscene.end(destroyInstance = false)
        for (wizard in wizards) {
            wizard.mode = EmptyMode
        }
    }

    fun Player.questComplete() {
        AuditLog.event(this, "quest_completed", "demon_slayer")
        anim("silverlight_showoff")
        gfx("silverlight_sparkle")
        sound("equip_silverlight")
        jingle("quest_complete_1")
        set("demon_slayer", "completed")
        inc("quest_points", 3)
        DemonSlayerSpell.clear(this)
        softQueue("quest_complete", 1) {
            player.questComplete(
                "Demon Slayer",
                "3 Quest Points",
                "Silverlight",
                item = "silverlight",
            )
        }
    }

    /**
     * Spawns energy barriers in a clockwise ring
     */
    fun spawnEnergyBarrier(offset: Delta) {
        var tile = offset.tile(3221, 3367)
        var rotation = 0
        var direction = Direction.NORTH
        while (rotation < 4) {
            repeat(6) {
                GameObjects.add("demon_slayer_energy_barrier", tile, ObjectShape.WALL_STRAIGHT, rotation)
                tile = tile.add(direction)
            }
            direction = direction.rotate(1)
            repeat(3) {
                GameObjects.add("demon_slayer_energy_barrier", tile, ObjectShape.WALL_DIAGONAL, rotation)
                GameObjects.add("demon_slayer_energy_barrier", tile.add(direction.rotate(1)), ObjectShape.WALL_DIAGONAL_CORNER, rotation)
                tile = tile.add(direction)
            }
            GameObjects.add("demon_slayer_energy_barrier", tile, ObjectShape.WALL_DIAGONAL, rotation)
            rotation++
            direction = direction.rotate(1)
            tile = tile.add(direction)
        }
    }
}
