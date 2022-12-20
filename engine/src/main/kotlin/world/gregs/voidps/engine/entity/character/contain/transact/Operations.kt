package world.gregs.voidps.engine.entity.character.contain.transact

import world.gregs.voidps.engine.entity.character.contain.Container

fun Container.replace(id: String, with: String) = transaction { replace(id, with) }

fun Container.replace(index: Int, id: String, with: String) = transaction { replace(index, id, with) }