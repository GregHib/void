import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.shakeCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.update.batch.animate
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.CustomObjects
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.suspend.delay
import world.gregs.voidps.world.activity.quest.startCutscene
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

on<Moved>({ it["demon_slayer", "unstarted"] == "kill_demon" && !rect.contains(from) && rect.contains(to) }) { player: Player ->
    player.strongQueue("demon_slayer") {
        val region = Region(12852)
        val instance = startCutscene(region)
        val offset = instance.offset(region)
        player.playTrack("delrith") // TODO 239
        val delrith = npcs.add("delrith", Tile(3227, 3369)) ?: return@strongQueue
        val wizard1 = npcs.add("dark_wizard_water", Tile(3226, 3371)) ?: return@strongQueue
        val wizard2 = npcs.add("dark_wizard_water_2", Tile(3229, 3371)) ?: return@strongQueue
        val wizard3 = npcs.add("dark_wizard_earth", Tile(3226, 3368)) ?: return@strongQueue
        val denath = npcs.add("denath", Tile(3229, 3368)) ?: return@strongQueue

        if (player["demon_slayer_summoned", false]) {
            return@strongQueue
        }
//        delrith - invisible?

        val wizards = listOf(wizard1, wizard2, wizard3, denath)
        for (wizard in wizards) {
            wizard.setAnimation("4617")
            wizard.face(delrith)
        }

        delay(1)

        player.tele(offset.add(player.tile.x, player.tile.y))

        player.talkWith(denath)
        npc<Cheerful>("""
            Arise, O mighty Delrith! Bring destruction to this soft,
            weak city!
        """)
        npc<Talking>("dark_wizard_water", "Arise, Delrith!", title = "Dark wizards")
        for (wizard in wizards) {
            wizard.forceChat = "Arise, Delrith!"
        }
        statement("The wizards cast an evil spell", clickToContinue = false)
        player.playSound("summon_npc")
        player.clearCamera()
        player.turnCamera(offset.add(3227, 3369), 100, 232, 232)
        player.moveCamera(offset.add(3227, 3365), 500, 232, 232)
        player.playSound("demon_slayer_table_explosion")
        val obj = objects.spawn("demon_slayer_stone_table", offset.add(3227, 3369), 10, 0)
        obj.animate("4622")
        delay(1)
        player.clearCamera()
        player.shakeCamera(15, 0, 0, 0, 0)
        val targets = listOf(
            Tile(3224, 3366),
            Tile(3231, 3366),
            Tile(3224, 3373),
            Tile(3231, 3373)
        )
        for (target in targets) {
            delrith.shoot("782", offset.add(target))
        }
        delay(1)
        for (target in targets) {
            areaGraphic("783", offset.add(target))
        }
        player.clearCamera()
        player.shakeCamera(0, 0, 0, 0, 0)
        delay(3)
        delrith.setAnimation("4623")
//        delrith.face(player)
        player.playSound("demon_slayer_break_table", delay = 10)
        player.playSound("demon_slayer_delrith_appear")
        player.turnCamera(offset.add(3227, 3369), 400, 1, 1)
        player["demon_slayer_summoned"] = true
        delay(5)
        delay(2)
        player.clearCamera()
        player.moveCamera(offset.add(3226, 3375), 500, 232, 232)
        player.turnCamera(offset.add(3227, 3367), 300, 232, 232)
        delay(1)
        npc<Laugh>("""
            Ha ha ha! At last you are free, my demonic brother!
            Rest now, and then have your revenge on this pitiful
            city!
        """)
        for (wizard in wizards) {
            wizard.face(player)
        }
        delrith.face(player)
        npc<Surprised>("dark_wizard_earth", "Who's that?")
        npc<Afraid>("Noo! Not Silverlight! Delrith is not ready yet!")
        denath.walkTo(offset.add(3236, 3368))
        player.clearCamera()
        player.moveCamera(offset.add(3226, 3383), 1000, 1, 1)
        delay(6)
    }
}

on<Unregistered>({ it.contains("demon_slayer_instance") }) { player: Player ->
    player.clear("demon_slayer_offset")
    Instances.free(player.remove<Region>("demon_slayer_instance") ?: return@on)
}