package rs.dusk.tools.map.process

import rs.dusk.engine.entity.obj.GameObject

class ObjectCompare(private val linker: ObjectLinker) {

    fun compare(objects: List<GameObject>) {
        objects.forEach {
            if(it.def.name.contains("trapdoor", true)) {
                println("Trapdoor ${it.type} ${it.id} ${it.def.options.toList()}")
            }
        }
        val options = mutableSetOf<String>()
        val interactiveObjects = objects.filter {
            it.def.interactive > 0 && linker.getAvailableTiles(it).isNotEmpty()
        }

        interactiveObjects.forEach {
            it.def.options.forEach {
                options.add(it ?: return@forEach)
            }
        }
        println("Options $options")
        println("${objects.size} objects ${interactiveObjects.size} interactive")
        var count = 0
        /*
            opposites
            climb-up climb-down/open/jump-down
            enter exit/exit-through/exit-room/exit-cave
            open close/leave/leave tomb/

         */
    }
}