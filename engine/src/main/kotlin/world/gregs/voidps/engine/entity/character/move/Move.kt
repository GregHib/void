package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player

var Character.running: Boolean
    get() = if (this is Player) get("movement", "walk") == "run" else get("running", false)
    set(value) = if (this is Player) set("movement", if (value) "run" else "walk") else set("running", value)
