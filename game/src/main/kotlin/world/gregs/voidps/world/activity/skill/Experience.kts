import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.GrantExp
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on

on<Registered> { player: Player ->
    player.sendVar("xp_counter")
}

on<InterfaceOption>({ id == it.gameFrame.name && component == "xp_orb" && option == "Reset XP Total" }) { player: Player ->
    player.setVar("xp_counter", 0.0)
}

on<GrantExp> { player: Player ->
    val current = player.getVar<Double>("xp_counter")
    val increase = to - from
    player.setVar("xp_counter", current + increase)
    player["lifetime_xp", true] = player["lifetime_xp", 0.0] + increase
}