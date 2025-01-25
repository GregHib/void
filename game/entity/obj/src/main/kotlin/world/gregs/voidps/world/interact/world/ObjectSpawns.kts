package world.gregs.voidps.world.interact.world

import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.world.spawn.loadObjectSpawns

val objs: GameObjects by inject()
val definitions: ObjectDefinitions by inject()

worldSpawn {
    loadObjectSpawns(objs)
}

adminCommand("reload") {
    if (content == "objects" || content == "objs" || content == "object" || content == "obj") {
        definitions.load()
        loadObjectSpawns(objs, definitions = definitions)
    }
}