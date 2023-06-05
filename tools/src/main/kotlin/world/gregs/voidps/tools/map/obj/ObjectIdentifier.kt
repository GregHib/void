package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.engine.entity.obj.GameMapObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.equals
import world.gregs.voidps.tools.map.obj.types.*
import world.gregs.voidps.tools.map.view.graph.MutableNavigationGraph
import kotlin.system.exitProcess

class ObjectIdentifier(private val linker: ObjectLinker, private val worldMapLinks: List<Pair<Tile, Tile>>, val graph: MutableNavigationGraph) {

    val objs = get<GameObjects>()

    /**
     * Ignore re-used objects, e.g. chains on planes > 0 in stronghold of security
     */
    private fun isReused(obj: GameMapObject): Boolean {
        if (obj.tile.plane > 0) {
            if (objs[obj.tile.addPlane(1), obj.group, obj.id] != null) {
                return false
            }
            if (objs[obj.tile.minus(plane = 1), obj.group, obj.id] != null) {
                return false
            }
        }
        return true
    }

    fun compare(objects: List<GameMapObject>) {
        val interactiveObjects = objects.filter {
            it.def.options?.first() != null &&
                    linker.getAvailableTiles(it).isNotEmpty() &&
                    isReused(it)
        }
        val interactiveOptions = interactiveObjects
            .flatMap { obj -> obj.getOptions() }

        /**
         *  TODO
         *      Self-interactable
         *      Doors
         *      Shortcuts
         */
        val options = setOf(
            ObjectIdentification(
                "Ladder",
                { interactiveOptions.minus(obj.getOptions()) },
                listOf(
                    sizeDifference,
                    differenceBetweenNames,
                    interactTileDistance,
                    objectDistance,
                    ladderOptionNameOpposition,
                    differenceBetweenIds,
                    ladderType
                )
            ),
            ObjectIdentification(
                "Stairs",
                { interactiveOptions.minus(obj.getOptions()) },
                listOf(
                    sizeDifference,
                    differenceBetweenNames,
                    interactTileDistance,
                    objectDistance,
                    stairOptionNameOpposition,
                    differenceBetweenIds,
                    ladderType
                )
            ),
            ObjectIdentification(
                "World Map Link",
                { worldMapLinks },
                listOf(
                    distanceToTile
                )
            ),
            ObjectIdentification(
                "Wall",// Or one way ladder
                { linker.getAllTiles(obj) },
                listOf(
                    wallOptions,
                    isOppositeTile,
                    isPopulatedPlane
                )
            )
        )

        val climbables =
            interactiveObjects
                .filter {
                    val option = it.def.options?.firstOrNull()?.replace("-", " ")?.lowercase()
                    option == "climb up" || option == "climb down"
                }

        val score = false
        if (score) {
            val debug = climbables.firstOrNull { it.def.id == 17149 && it.tile.equals(3016, 3519, 2) }
            val target = interactiveOptions.firstOrNull { it.obj.def.id == 17148 && it.obj.tile.equals(3016, 3519, 1) }
            val target2 = interactiveOptions.firstOrNull { it.obj.def.id == 17148 && it.obj.tile.equals(3015, 3519) }
            if (debug != null && target != null && target2 != null) {
                for (opt in debug.getOptions()) {
                    val context = ObjectIdentificationContext(debug, opt.tiles, opt.option)
//                    println(DecisionMaker().decide(context, options))
                    val opt = options.first() as ObjectIdentification<GameObjectOption>
                    println(opt.getScores(context, target))
//                    println(opt.getHighestTarget(context, 0.0))
                    println(opt.getScores(context, target2))
//                    println(opt.getHighestTarget(context, 0.0))
                }
            }
            exitProcess(0)
        }

        var found = 0
        var unknown = 0
        var n = 0
        climbables.forEach { obj ->
            for (opt in obj.getOptions()) {
                val context = ObjectIdentificationContext(obj, opt.tiles, opt.option)
                val decision = decide(context, options)
                if (decision != null) {
                    val (score, target, option) = decision
                    if (score > 0.5) {
                        if (target is GameObjectOption) {
                            val link = graph.addLink(context.obj.tile, target.obj.tile)
                            link.actions = listOf("${obj.def.id} ${opt.option}")
                        } else if (target is Pair<*, *>) {
                            graph.addLink(context.obj.tile, target.second as Tile)
                        }

                        found++
                    } else {
                        unknown++
                    }
                    println("$score $obj ${opt.option} decision ${(option as ObjectIdentification<*>).name} $target")
                } else {
                    n++
                    println("No decision found $opt")
                }
            }
        }

        //2428 identified 248 unknown
        println("${objects.size} objects ${interactiveObjects.size} interactive $found identified $unknown unknown $n null")
    }

    private fun decide(context: ObjectIdentificationContext, options: Set<ObjectIdentification<*>>): Triple<Double, *, *>? {
        val decision = select(context, options) ?: return null
        context.last = decision
        return decision
    }

    private fun select(context: ObjectIdentificationContext, options: Set<ObjectIdentification<*>>): Triple<Double, *, *>? {
        return options.fold(null as Triple<Double, *, *>?) { highest, option ->
            option.getHighestTarget(context, highest?.first ?: 0.0) ?: highest
        }
    }

    val manual = mutableMapOf(
        // TODO nulls
        GameMapObject(25844, Tile(1867, 4244), 10, 0) to GameMapObject(25843, Tile(3016, 3519), 10, 0),// Black knights fortress dungeon exit
        GameMapObject(25843, Tile(3016, 3519), 10, 0) to GameMapObject(25844, Tile(1867, 4244), 10, 0),// Black knights fortress dungeon enter
        GameMapObject(27243, Tile(1636, 4835), 10, 0) to Tile(1702, 4823),// 07 hween - FIXME missing xteas
        GameMapObject(20194, Tile(1877, 5392), 10, 0) to Tile(0),// barbarian assault
        GameMapObject(20194, Tile(1877, 5456), 10, 0) to Tile(0),// barbarian assault
        GameMapObject(9138, Tile(1939, 4956), 10, 2) to GameMapObject(9084, Tile(2930, 10196), 10, 1),// blast furnace exit
        GameMapObject(40900, Tile(2004, 4396), 10, 2) to GameMapObject(40901, Tile(2097, 4406), 10, 2),// movario's base down
        GameMapObject(15746, Tile(2024, 5087), 10, 0) to GameMapObject(15753, Tile(3230, 3286), 10, 0),// death to the dorgeshuun sigmund fight exit
        GameMapObject(40901, Tile(2097, 4406), 10, 2) to GameMapObject(40900, Tile(2004, 4396), 10, 2),// movario's base up
        GameMapObject(40957, Tile(2061, 4380, 2), 10, 0) to GameMapObject(40956, Tile(2061, 4380, 1), 10, 0),// movario's office down
        GameMapObject(40956, Tile(2061, 4380, 1), 10, 0) to GameMapObject(40957, Tile(2061, 4380, 2), 10, 0),// movario's office up
        GameMapObject(37625, Tile(2334, 5737), 22, 0) to GameMapObject(37624, Tile(2522, 4999), 10, 0),// Learning the ropes enter
        GameMapObject(25429, Tile(2335, 9351), 10, 0) to GameMapObject(25434, Tile(2438, 3164), 10, 0),// Observatory dungeon exit
        GameMapObject(25429, Tile(2355, 9395), 10, 0) to GameMapObject(25432, Tile(2458, 3186), 10, 0),// Observatory dungeon ruins exit
        GameMapObject(28743, Tile(2333, 10016), 10, 0) to GameMapObject(28742, Tile(2328, 3645), 10, 0),// Piscatoris obelisk
        GameMapObject(35646, Tile(2388, 5683), 10, 0) to Tile(2388, 5686, 1),
        GameMapObject(43897, Tile(2396, 9246), 10, 0) to GameMapObject(43899, Tile(2425, 2827), 10, 0),// Mobilising armies exit
        GameMapObject(6841, Tile(2485, 3042), 10, 0) to GameMapObject(6842, Tile(2478, 9437, 2), 10, 0),// Zogre flesh eaters enter
        GameMapObject(25434, Tile(2438, 3164), 10, 0) to GameMapObject(25429, Tile(2335, 9351), 10, 0),// Observatory dungeon enter
        GameMapObject(25432, Tile(2458, 3186), 10, 0) to GameMapObject(25429, Tile(2355, 9395), 10, 0),// Observatory dungeon ruins enter
        GameMapObject(6842, Tile(2478, 9437, 2), 10, 0) to GameMapObject(6841, Tile(2485, 3042), 10, 0),// Zogre flesh eaters exit
        GameMapObject(37624, Tile(2522, 4999), 10, 0) to GameMapObject(37625, Tile(2334, 5737), 22, 0),// Learning the ropes exit
        GameMapObject(1757, Tile(2556, 9844), 10, 1) to Tile(2556, 3444),// Glarial's tomb exit
        GameMapObject(1992, Tile(2558, 3444), 10, 0) to Tile(2555, 9844),// Glarial's tomb enter TODO req glarial's stone and no combat equ
        GameMapObject(5251, Tile(2597, 4435), 10, 0) to GameMapObject(5250, Tile(2533, 3155), 10, 0),// gnome village dungeon exit
        GameMapObject(15747, Tile(2567, 5185), 10, 3) to GameMapObject(15756, Tile(3166, 9622), 10, 0),// ham storerooms exit
        GameMapObject(15756, Tile(3166, 9622), 10, 0) to GameMapObject(15747, Tile(2567, 5185), 10, 3),// ham storerooms enter
        GameMapObject(20227, Tile(2593, 5252), 10, 0) to GameMapObject(20226, Tile(2534, 3572), 10, 0),// barbarian assault waiting room exit
        GameMapObject(47776, Tile(2591, 5596), 10, 0) to Tile(2592, 5600, 1),// christmas 08? TODO interact tile(2592, 5595)
        GameMapObject(32015, Tile(2576, 9655), 10, 0) to Tile(2576, 3255),// Watch tower exit
        GameMapObject(52219, Tile(2644, 2673), 10, 3) to GameMapObject(52220, Tile(2635, 9049), 10, 1),// Conquest waiting room enter
        GameMapObject(4158, Tile(2644, 3657), 10, 3) to GameMapObject(4159, Tile(2631, 10005), 10, 0),// Swensen the navigator dungeon enter
        GameMapObject(32015, Tile(2629, 5072), 10, 3) to GameMapObject(6561, Tile(2547, 3421), 10, 0),// Shadow dungeon exit
        GameMapObject(6561, Tile(2547, 3421), 10, 0) to GameMapObject(32015, Tile(2629, 5072), 10, 3),// Shadow dungeon enter
        GameMapObject(52220, Tile(2635, 9049), 10, 1) to GameMapObject(52219, Tile(2644, 2673), 10, 3),// Conquest waiting room exit
        GameMapObject(96, Tile(2638, 9763), 10, 3) to GameMapObject(35121, Tile(2650, 9804), 10, 3),// Temple of ikov boots of lightness room light exit
        GameMapObject(33232, Tile(2638, 9740), 10, 3) to GameMapObject(35121, Tile(2650, 9804), 10, 3),// Temple of ikov boots of lightness room dark exit
        GameMapObject(32015, Tile(2682, 9849), 10, 2) to GameMapObject(100, Tile(2665, 9850), 10, 0),// Temple of ikov trap exit
        GameMapObject(4159, Tile(2631, 10005), 10, 0) to GameMapObject(4158, Tile(2644, 3657), 10, 3),// Swensen the navigator dungeon exit
        GameMapObject(4188, Tile(2672, 10099, 2), 10, 2) to GameMapObject(34286, Tile(2667, 3694), 10, 0),// Thorvald the warrior exit
        GameMapObject(3415, Tile(2710, 3497), 10, 2) to GameMapObject(3416, Tile(2714, 9887), 10, 3),// Elemental workshop entrance
        GameMapObject(44207, Tile(2812, 10263), 10, 2) to GameMapObject(44208, Tile(2839, 3855), 10, 0),// Lamistard's tunnels stair exit
        GameMapObject(44208, Tile(2839, 3855), 10, 0) to GameMapObject(44207, Tile(2812, 10263), 10, 2),// Lamistard's tunnels stair enter
        GameMapObject(44206, Tile(2755, 10258), 10, 0) to GameMapObject(44204, Tile(2808, 3860), 10, 0),// Lamistard's tunnels cave exit
        GameMapObject(44204, Tile(2808, 3860), 10, 0) to GameMapObject(44206, Tile(2755, 10258), 10, 0),// Lamistard's tunnels cave enter
        GameMapObject(37900, Tile(2834, 4317), 10, 1) to GameMapObject(37747, Tile(2962, 4287), 10, 0),// Spirit of summer basement exit
        GameMapObject(37747, Tile(2962, 4287), 10, 0) to GameMapObject(37900, Tile(2834, 4317), 10, 1),// Spirit of summer basement enter TODO "Climb-down" is from varbit, not on obj def
        GameMapObject(6499, Tile(2845, 4964, 2), 10, 2) to GameMapObject(6502, Tile(2782, 4973, 1), 10, 0),// Jaldraocht pyramid
        GameMapObject(18834, Tile(2831, 10077, 2), 10, 2) to GameMapObject(18833, Tile(2812, 3668, 0), 10, 0),// Troll stronghold exit
        GameMapObject(24687, Tile(2898, 9867), 10, 1) to GameMapObject(24842, Tile(2899, 3469, 0), 10, 0),// Witch's house dungeon exit
        GameMapObject(9084, Tile(2930, 10196), 10, 1) to GameMapObject(9138, Tile(1939, 4956), 10, 2),// blast furnace enter
        GameMapObject(10288, Tile(2914, 10188), 10, 2) to GameMapObject(6085, Tile(1942, 4704), 10, 0),// keldagrim rat pits enter
        GameMapObject(11739, Tile(2946, 3363), 10, 1) to GameMapObject(11741, Tile(2946, 3363, 1), 10, 0),// falador west bank ladder
        GameMapObject(12389, Tile(2960, 3507), 10, 1) to GameMapObject(12390, Tile(2981, 9916), 10, 0),// goblin village dungeon enter
        GameMapObject(17974, Tile(3038, 4375), 10, 2) to GameMapObject(21922, Tile(2647, 3213), 10, 0),// tower of life dungeon exit
        GameMapObject(21922, Tile(2647, 3213), 10, 0) to GameMapObject(17974, Tile(3038, 4375), 10, 2),// tower of life dungeon enter
        GameMapObject(7221, Tile(3057, 5005, 1), 10, 3) to Tile(3057, 5003, 1),// rouges den viewing gallery
        GameMapObject(29592, Tile(3084, 3452), 10, 2) to GameMapObject(29589, Tile(3086, 4244), 10, 0),// Stronghold of player safety training centre enter
        GameMapObject(31417, Tile(3079, 4339), 10, 2) to GameMapObject(31412, Tile(3338, 9368), 10, 0),// Pollnivneach slayer dungeon turoth
        GameMapObject(31417, Tile(3126, 4339), 10, 2) to GameMapObject(31412, Tile(3377, 9367), 10, 0),// Pollnivneach slayer dungeon basilisk
        GameMapObject(31417, Tile(3085, 4367), 10, 2) to GameMapObject(31412, Tile(3340, 9426), 10, 0),// Pollnivneach slayer dungeon kurask
        GameMapObject(31417, Tile(3125, 4364), 10, 2) to GameMapObject(31412, Tile(3374, 9426), 10, 0),// Pollnivneach slayer dungeon
        GameMapObject(9141, Tile(3144, 9741), 10, 2) to GameMapObject(31412, Tile(3374, 9426), 10, 0),// FIXME Unknown yellow training room
        GameMapObject(10554, Tile(3183, 9766), 10, 1) to Tile(3185, 9766),// Champion challenge exit
        GameMapObject(37683, Tile(3230, 3241), 10, 1) to GameMapObject(37684, Tile(3290, 4935), 10, 0),// Unstable foundations enter
        GameMapObject(20286, Tile(3257, 9225), 10, 1) to GameMapObject(20287, Tile(3268, 9229), 10, 0)// Sophanem dungeon
    )

    private fun GameMapObject.getOptions(): List<GameObjectOption> {
        val tiles = linker.getAvailableTiles(this).toSet()
        val openId: Int? = def.getOrNull("open")
        if (openId != null) {
            val openObj = GameMapObject(openId, tile, type, rotation)
            return openObj.options(tiles) + options(tiles)
        }
        return options(tiles)
    }

    private fun GameMapObject.options(tiles: Set<Tile>): List<GameObjectOption> {
        return def.options?.filterNotNull()?.filterNot { it == "Examine" }?.map { GameObjectOption(it, this, tiles) } ?: emptyList()
    }
}