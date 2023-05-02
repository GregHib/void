import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.replace
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.world.interact.dialogue.Cheerful
import world.gregs.voidps.world.interact.dialogue.Suspicious
import world.gregs.voidps.world.interact.dialogue.Talking
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.sound.playSound

on<ObjectOption>({ obj.id == "varrock_palace_drain" && option == "Search" }) { player: Player ->
    player.setAnimation("climb_down")
    if (player["demon_slayer_drain_dislodged", false]) {
        player.message("Nothing interesting seems to have been dropped down here today.")
    } else {
        player<Talking>("That must be the key Sir Prysin dropped.")
        player<Suspicious>("""
            I don't seem to be able to reach it. I wonder if I can
            dislodge it somehow. That way it may go down into the
            sewers.
        """)
    }
}

on<InterfaceOnObject>({ obj.id == "varrock_palace_drain" && id.endsWith("of_water") }) { player: Player ->
    val replacement = when {
        item.id.startsWith("bucket_of") -> "bucket"
        item.id.startsWith("jug_of") -> "jug"
        item.id.startsWith("pot_of") -> "empty_pot"
        item.id.startsWith("bowl_of") -> "bowl"
        else -> return@on
    }
    if (player.inventory.replace(id, replacement)) {
        player["demon_slayer_drain_dislodged"] = true
        player.message("You pour the liquid down the drain.")
        player.setAnimation("climb_down") // TODO there's a new anim for this - gfx 779?
        player.playSound("demon_slayer_drain")
        player.playSound("demon_slayer_key_fall")
        player.weakQueue("demon_slayer_dislodge_key") {
            player<Cheerful>("""
                OK, I think I've washed the key down into the sewer.
                I'd better go down and get it!
            """)
        }
    }
}