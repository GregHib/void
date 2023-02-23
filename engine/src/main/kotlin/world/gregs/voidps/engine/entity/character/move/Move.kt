package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.map.Tile

var Character.followTile: Tile
    get() = get("follow_tile", tile)
    set(value) = set("follow_tile", value)

var Character.previousTile: Tile
    get() = get("previous_tile", tile)
    set(value) = set("previous_tile", value)

var Character.running: Boolean
    get() = if (this is Player) getVar("movement", "walk") == "run" else get("running", false)
    set(value) = if (this is Player) setVar("movement", if (value) "run" else "walk") else set("running", value)