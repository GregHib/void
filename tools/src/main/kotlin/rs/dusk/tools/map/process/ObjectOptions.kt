package rs.dusk.tools.map.process

import rs.dusk.engine.entity.obj.GameObject

class ObjectOptions {

    val options = mutableMapOf<Int, List<Pair<String, ObjectCategory>>>()

    fun get(objectId: Int): List<Pair<String, ObjectCategory>> {
        return options[objectId] ?: emptyList()
    }

    fun loadAll(objects: List<GameObject>) {
        objects.forEach {
            options[it.id] = validOptions(it)
        }
    }

    private fun validOptions(obj: GameObject): List<Pair<String, ObjectCategory>> {
        val name = obj.def.name
        return obj.def.options.filterNotNull().mapNotNull { option ->
            val action = option.replace("-", " ")
            if (action.equals("climb up", true)) {
                option to when {
                    obj.def.sizeX == 1 && obj.def.sizeY == 1 -> {
                        ObjectCategory.Ladder_Bottom
                    }
                    else -> ObjectCategory.Stairs_Bottom
                }
            } else if (action.equals("climb down", true) || (isTrapDoor(name) && action.equals("open", true))) {
                option to when {
                    obj.def.sizeX == 1 && obj.def.sizeY == 1 -> {
                        if(!name.contains("ladder", true)) {
                            println("Unknown obj type $name $obj")
                        }
                        ObjectCategory.Ladder_Top
                    }
                    name.contains("stair", true) -> ObjectCategory.Stairs_Top
                    else -> {
//                        println("Unknown obj type $name $obj")
                        return@mapNotNull null
                    }
                }
            } else {
                null
            }
        }
    }

    private fun isTrapDoor(name: String) = name.contains("trapdoor", true) || name.contains("manhole", true)

    private fun isLadderTop(name: String) = isLadder(name) || name.contains("trapdoor", true) || name.contains("manhole", true)

    private fun isLadder(name: String) = name.contains("ladder", true) || name.contains("rope", true) || name.contains("chain", true) || name.contains("vine", true)
}