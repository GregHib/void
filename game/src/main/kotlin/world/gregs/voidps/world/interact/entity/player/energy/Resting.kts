import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.set
import world.gregs.voidps.engine.entity.character.update.visual.clearAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message

val animations = setOf(
    "rest_arms_back",
    "rest_arms_crossed",
    "rest_legs_out"
)

on<InterfaceOption>({ name == "energy_orb" && option == "Rest" }) { player: Player ->
    rest(player, false)
}

on<NPCOption>({ npc.def.name == "Musician" && option == "Listen-to" }) { player: Player ->
    rest(player, true)
}

fun rest(player: Player, music: Boolean) {
    println(player.action.type)
    if (player.action.type == ActionType.Resting) {
        player.message("You are already resting.")
        return
    }
    player.action(ActionType.Resting) {
        player.movement.clear()
        player["movement"] = player.getVar("movement", "walk")
        val anim = animations.random()
        try {
            player.setVar("movement", if (music) "music" else "rest")
            player.setAnimation(anim)
            player.message("You begin resting..", ChatType.GameFilter)
            await(Suspension.Infinite)
        } finally {
            player.setAnimation(anim.replace("rest", "stand"))
            val type = player["movement", "walk"]
            player.setVar("movement", type)
            player.movement.frozen = true
            world.gregs.voidps.engine.delay(player, if (type == "run") 2 else 3) {
                player.clearAnimation()
                player.movement.frozen = false
            }
        }
    }
}