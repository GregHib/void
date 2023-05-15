import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.shakeCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.update.batch.animate
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.CustomObjects
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.queue.LogoutBehaviour
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.suspend.delay
import world.gregs.voidps.world.activity.quest.showTabs
import world.gregs.voidps.world.activity.quest.startCutscene
import world.gregs.voidps.world.activity.quest.stopCutscene
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.gfx.areaGraphic
import world.gregs.voidps.world.interact.entity.player.music.playTrack
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound

val rect = Rectangle(3221, 3363, 3234, 3376)
val objects: CustomObjects by inject()
val npcs: NPCs by inject()
val defaultTile = Tile(3220, 3367)
val targets = listOf(
    Tile(3224, 3366),
    Tile(3231, 3366),
    Tile(3224, 3373),
    Tile(3231, 3373)
)

on<Moved>({ it["demon_slayer", "unstarted"] == "kill_demon" && enterArea(it, from, to) }) { player: Player ->
    val context = object : PlayerContext {
        override val player: Player = player
        override var onCancel: (() -> Unit)? = null
    }
    cancel()
    println("Cancel $this $cancelled")
    with(context) {
        val region = Region(12852)
        val instance = startCutscene(region)
        val offset = instance.offset(region)
        player["demon_slayer_instance"] = instance
        player["demon_slayer_offset"] = offset
        player.steps.clear()
        player.mode = EmptyMode
        val wizard1 = npcs.add("dark_wizard_water", offset.add(3226, 3371), Direction.SOUTH_EAST) ?: return@with
        val wizard2 = npcs.add("dark_wizard_water_2", offset.add(3229, 3371), Direction.SOUTH_WEST) ?: return@with
        val wizard3 = npcs.add("dark_wizard_earth", offset.add(3226, 3368), Direction.NORTH_EAST) ?: return@with
        val denath = npcs.add("denath", offset.add(3229, 3368), Direction.NORTH_WEST) ?: return@with
        val delrith = npcs.add("delrith", offset.add(3227, 3369), Direction.SOUTH) ?: return@with
        npcs.removeIndex(delrith)
        val wizards = listOf(wizard1, wizard2, wizard3, denath)
        for (wizard in wizards) {
            wizard.mode = PauseMode
            wizard.steps.clear()
        }
        delay(1)
        setCutsceneEnd(instance)
        player.tele(offset.add(3222, 3367))
        player.face(Direction.NORTH_EAST)
        player.playTrack("delrith") // TODO 239

        if (player["demon_slayer_summoned", false]) {
            player.queue.clear("demon_slayer_delrith_cutscene_end")
            delrith.tele(offset.add(3227, 3367))
            npcs.index(delrith)
            return@with
        }
        delay(1)
        for (wizard in wizards) {
            wizard.setAnimation("summon_demon")
        }

        player.clearCamera()
        player.moveCamera(offset.add(3224, 3376), 475, 232, 232)
        player.turnCamera(offset.add(3227, 3369), 300, 232, 232)
        player.moveCamera(offset.add(3231, 3376), 475, 1, 1)
        npc<Cheerful>("denath", """
            Arise, O mighty Delrith! Bring destruction to this soft,
            weak city!
        """)
        for (wizard in wizards) {
            wizard.forceChat = "Arise, Delrith!"
        }
        npc<Talking>("dark_wizard_water", "Arise, Delrith!", title = "Dark wizards")

        statement("The wizards cast an evil spell", clickToContinue = false)
        val obj = objects.spawn("demon_slayer_stone_table", offset.add(3227, 3369), 10, 0)
        player.clearCamera()
        player.turnCamera(offset.add(3227, 3369), 100, 232, 232)
        player.moveCamera(offset.add(3227, 3365), 500, 232, 232)
        delay(1)
        obj.animate("4622")
        player.playSound("summon_npc")
        player.playSound("demon_slayer_table_explosion")
        player.shakeCamera(15, 0, 0, 0, 0)
        for (target in targets) {
            delrith.shoot("782", offset.add(target))
        }
        delay(1)
        player.shakeCamera(0, 0, 0, 0, 0)
        for (target in targets) {
            areaGraphic("783", offset.add(target))
        }
        delay(2)
        npcs.index(delrith)
        delay(1)
        delrith.setAnimation("delrith_rise")
        player.playSound("demon_slayer_break_table", delay = 10)
        player.playSound("demon_slayer_delrith_appear")
        player.turnCamera(offset.add(3227, 3369), 400, 1, 1)
        player["demon_slayer_summoned"] = true
        delay(5)
        delrith.walkTo(offset.add(3227, 3367))
        delay(2)
        player.clearCamera()
        player.moveCamera(offset.add(3226, 3375), 500, 232, 232)
        player.turnCamera(offset.add(3227, 3367), 300, 232, 232)
        delay(1)
        delrith.face(denath)
        for (wizard in wizards) {
            wizard.clearAnimation()
            wizard.face(delrith)
        }
        npc<Laugh>("denath", """
            Ha ha ha! At last you are free, my demonic brother!
            Rest now, and then have your revenge on this pitiful
            city!
        """)
        for (wizard in wizards) {
            wizard.face(player)
        }
        delrith.face(player)
        npc<Surprised>("dark_wizard_earth", "Who's that?")
        npc<Afraid>("denath", "Noo! Not Silverlight! Delrith is not ready yet!")
        denath.walkTo(offset.add(3236, 3368))
        player.clearCamera()
        player.moveCamera(offset.add(3226, 3383), 1000, 1, 1)
        npc<Suspicious>("denath", "I've got to get out of here...")
        player.queue.clear("demon_slayer_delrith_cutscene_end")
        showTabs()
        player.clearCamera()
        for (wizard in wizards) {
            wizard.mode = EmptyMode
        }
    }
}

fun PlayerContext.setCutsceneEnd(instance: Region) {
    player.queue("demon_slayer_delrith_cutscene_end", 1, LogoutBehaviour.Accelerate) {
        endCutscene(instance, defaultTile)
    }
}

fun PlayerContext.endCutscene(instance: Region, tile: Tile? = null) {
    val offset: Tile = player.getOrNull("demon_slayer_offset") ?: return
    player.tele(tile ?: player.tile.minus(offset))
    stopCutscene(instance)
    player.clearCamera()
    destroyInstance(player)
}

on<Moved>({ exitArea(it, to) }) { player: Player ->
    destroyInstance(player)
}

on<Unregistered>({ it.contains("demon_slayer_instance") }) { player: Player ->
    destroyInstance(player)
}

fun enterArea(player: Player, from: Tile, to: Tile): Boolean {
    return !rect.contains(from) && rect.contains(to) && !player.hasClock("demon_slayer_instance_exit")
}

fun exitArea(player: Player, to: Tile): Boolean {
    val offset: Tile = player.getOrNull("demon_slayer_offset") ?: return false
    val actual = to.minus(offset)
    return !rect.contains(actual) && !player.hasClock("demon_slayer_instance_exit")
}

fun destroyInstance(player: Player) {
    val offset: Tile? = player.remove("demon_slayer_offset")
    val target = if (offset == null) defaultTile else player.tile.minus(offset)
    player.start("demon_slayer_instance_exit", 2)
    player.tele(target)
    val instance: Region = player.remove("demon_slayer_instance") ?: return
    Instances.free(instance)
    // TODO clear npcs and objects
}