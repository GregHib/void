package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.tools.map.obj.types.*
import world.gregs.voidps.tools.map.view.graph.MutableNavigationGraph
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals
import kotlin.system.exitProcess

class ObjectIdentifier(private val linker: ObjectLinker, private val worldMapLinks: List<Pair<Tile, Tile>>, val graph: MutableNavigationGraph) {

    /**
     * Ignore re-used objects, e.g. chains on levels > 0 in stronghold of security
     */
    private fun isReused(obj: GameObject): Boolean {
        if (obj.tile.level > 0) {
            if (GameObjects.findOrNull(obj.tile.addLevel(1), obj.id) != null) {
                return false
            }
            if (GameObjects.findOrNull(obj.tile.minus(level = 1), obj.id) != null) {
                return false
            }
        }
        return true
    }

    @Suppress("UNCHECKED_CAST")
    fun compare(objects: List<GameObject>) {
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
                { interactiveOptions.minus(obj.getOptions().toSet()) },
                listOf(
                    sizeDifference,
                    differenceBetweenNames,
                    interactTileDistance,
                    objectDistance,
                    ladderOptionNameOpposition,
                    differenceBetweenIds,
                    ladderType,
                ),
            ),
            ObjectIdentification(
                "Stairs",
                { interactiveOptions.minus(obj.getOptions().toSet()) },
                listOf(
                    sizeDifference,
                    differenceBetweenNames,
                    interactTileDistance,
                    objectDistance,
                    stairOptionNameOpposition,
                    differenceBetweenIds,
                    ladderType,
                ),
            ),
            ObjectIdentification(
                "World Map Link",
                { worldMapLinks },
                listOf(
                    distanceToTile,
                ),
            ),
            ObjectIdentification(
                "Wall", // Or one way ladder
                { linker.getAllTiles(obj) },
                listOf(
                    wallOptions,
                    isOppositeTile,
                    isPopulatedLevel,
                ),
            ),
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

        // 2428 identified 248 unknown
        println("${objects.size} objects ${interactiveObjects.size} interactive $found identified $unknown unknown $n null")
    }

    private fun decide(context: ObjectIdentificationContext, options: Set<ObjectIdentification<*>>): Triple<Double, *, *>? {
        val decision = select(context, options) ?: return null
        context.last = decision
        return decision
    }

    private fun select(context: ObjectIdentificationContext, options: Set<ObjectIdentification<*>>): Triple<Double, *, *>? = options.fold(null as Triple<Double, *, *>?) { highest, option ->
        option.getHighestTarget(context, highest?.first ?: 0.0) ?: highest
    }

    val manual = mutableMapOf(
        // TODO nulls
        GameObject(25844, Tile(1867, 4244), 10, 0) to GameObject(25843, Tile(3016, 3519), 10, 0), // Black knights fortress dungeon exit
        GameObject(25843, Tile(3016, 3519), 10, 0) to GameObject(25844, Tile(1867, 4244), 10, 0), // Black knights fortress dungeon enter
        GameObject(27243, Tile(1636, 4835), 10, 0) to Tile(1702, 4823), // 07 hween - FIXME missing xteas
        GameObject(20194, Tile(1877, 5392), 10, 0) to Tile(0), // barbarian assault
        GameObject(20194, Tile(1877, 5456), 10, 0) to Tile(0), // barbarian assault
        GameObject(9138, Tile(1939, 4956), 10, 2) to GameObject(9084, Tile(2930, 10196), 10, 1), // blast furnace exit
        GameObject(40900, Tile(2004, 4396), 10, 2) to GameObject(40901, Tile(2097, 4406), 10, 2), // movario's base down
        GameObject(15746, Tile(2024, 5087), 10, 0) to GameObject(15753, Tile(3230, 3286), 10, 0), // death to the dorgeshuun sigmund fight exit
        GameObject(40901, Tile(2097, 4406), 10, 2) to GameObject(40900, Tile(2004, 4396), 10, 2), // movario's base up
        GameObject(40957, Tile(2061, 4380, 2), 10, 0) to GameObject(40956, Tile(2061, 4380, 1), 10, 0), // movario's office down
        GameObject(40956, Tile(2061, 4380, 1), 10, 0) to GameObject(40957, Tile(2061, 4380, 2), 10, 0), // movario's office up
        GameObject(37625, Tile(2334, 5737), 22, 0) to GameObject(37624, Tile(2522, 4999), 10, 0), // Learning the ropes enter
        GameObject(25429, Tile(2335, 9351), 10, 0) to GameObject(25434, Tile(2438, 3164), 10, 0), // Observatory dungeon exit
        GameObject(25429, Tile(2355, 9395), 10, 0) to GameObject(25432, Tile(2458, 3186), 10, 0), // Observatory dungeon ruins exit
        GameObject(28743, Tile(2333, 10016), 10, 0) to GameObject(28742, Tile(2328, 3645), 10, 0), // Piscatoris obelisk
        GameObject(35646, Tile(2388, 5683), 10, 0) to Tile(2388, 5686, 1),
        GameObject(43897, Tile(2396, 9246), 10, 0) to GameObject(43899, Tile(2425, 2827), 10, 0), // Mobilising armies exit
        GameObject(6841, Tile(2485, 3042), 10, 0) to GameObject(6842, Tile(2478, 9437, 2), 10, 0), // Zogre flesh eaters enter
        GameObject(25434, Tile(2438, 3164), 10, 0) to GameObject(25429, Tile(2335, 9351), 10, 0), // Observatory dungeon enter
        GameObject(25432, Tile(2458, 3186), 10, 0) to GameObject(25429, Tile(2355, 9395), 10, 0), // Observatory dungeon ruins enter
        GameObject(6842, Tile(2478, 9437, 2), 10, 0) to GameObject(6841, Tile(2485, 3042), 10, 0), // Zogre flesh eaters exit
        GameObject(37624, Tile(2522, 4999), 10, 0) to GameObject(37625, Tile(2334, 5737), 22, 0), // Learning the ropes exit
        GameObject(1757, Tile(2556, 9844), 10, 1) to Tile(2556, 3444), // Glarial's tomb exit
        GameObject(1992, Tile(2558, 3444), 10, 0) to Tile(2555, 9844), // Glarial's tomb enter TODO req glarial's stone and no combat equ
        GameObject(5251, Tile(2597, 4435), 10, 0) to GameObject(5250, Tile(2533, 3155), 10, 0), // gnome village dungeon exit
        GameObject(15747, Tile(2567, 5185), 10, 3) to GameObject(15756, Tile(3166, 9622), 10, 0), // ham storerooms exit
        GameObject(15756, Tile(3166, 9622), 10, 0) to GameObject(15747, Tile(2567, 5185), 10, 3), // ham storerooms enter
        GameObject(20227, Tile(2593, 5252), 10, 0) to GameObject(20226, Tile(2534, 3572), 10, 0), // barbarian assault waiting room exit
        GameObject(47776, Tile(2591, 5596), 10, 0) to Tile(2592, 5600, 1), // christmas 08? TODO interact tile(2592, 5595)
        GameObject(32015, Tile(2576, 9655), 10, 0) to Tile(2576, 3255), // Watch tower exit
        GameObject(52219, Tile(2644, 2673), 10, 3) to GameObject(52220, Tile(2635, 9049), 10, 1), // Conquest waiting room enter
        GameObject(4158, Tile(2644, 3657), 10, 3) to GameObject(4159, Tile(2631, 10005), 10, 0), // Swensen the navigator dungeon enter
        GameObject(32015, Tile(2629, 5072), 10, 3) to GameObject(6561, Tile(2547, 3421), 10, 0), // Shadow dungeon exit
        GameObject(6561, Tile(2547, 3421), 10, 0) to GameObject(32015, Tile(2629, 5072), 10, 3), // Shadow dungeon enter
        GameObject(52220, Tile(2635, 9049), 10, 1) to GameObject(52219, Tile(2644, 2673), 10, 3), // Conquest waiting room exit
        GameObject(96, Tile(2638, 9763), 10, 3) to GameObject(35121, Tile(2650, 9804), 10, 3), // Temple of ikov boots of lightness room light exit
        GameObject(33232, Tile(2638, 9740), 10, 3) to GameObject(35121, Tile(2650, 9804), 10, 3), // Temple of ikov boots of lightness room dark exit
        GameObject(32015, Tile(2682, 9849), 10, 2) to GameObject(100, Tile(2665, 9850), 10, 0), // Temple of ikov trap exit
        GameObject(4159, Tile(2631, 10005), 10, 0) to GameObject(4158, Tile(2644, 3657), 10, 3), // Swensen the navigator dungeon exit
        GameObject(4188, Tile(2672, 10099, 2), 10, 2) to GameObject(34286, Tile(2667, 3694), 10, 0), // Thorvald the warrior exit
        GameObject(3415, Tile(2710, 3497), 10, 2) to GameObject(3416, Tile(2714, 9887), 10, 3), // Elemental workshop entrance
        GameObject(44207, Tile(2812, 10263), 10, 2) to GameObject(44208, Tile(2839, 3855), 10, 0), // Lamistard's tunnels stair exit
        GameObject(44208, Tile(2839, 3855), 10, 0) to GameObject(44207, Tile(2812, 10263), 10, 2), // Lamistard's tunnels stair enter
        GameObject(44206, Tile(2755, 10258), 10, 0) to GameObject(44204, Tile(2808, 3860), 10, 0), // Lamistard's tunnels cave exit
        GameObject(44204, Tile(2808, 3860), 10, 0) to GameObject(44206, Tile(2755, 10258), 10, 0), // Lamistard's tunnels cave enter
        GameObject(37900, Tile(2834, 4317), 10, 1) to GameObject(37747, Tile(2962, 4287), 10, 0), // Spirit of summer basement exit
        GameObject(37747, Tile(2962, 4287), 10, 0) to GameObject(37900, Tile(2834, 4317), 10, 1), // Spirit of summer basement enter TODO "Climb-down" is from varbit, not on obj def
        GameObject(6499, Tile(2845, 4964, 2), 10, 2) to GameObject(6502, Tile(2782, 4973, 1), 10, 0), // Jaldraocht pyramid
        GameObject(18834, Tile(2831, 10077, 2), 10, 2) to GameObject(18833, Tile(2812, 3668, 0), 10, 0), // Troll stronghold exit
        GameObject(24687, Tile(2898, 9867), 10, 1) to GameObject(24842, Tile(2899, 3469, 0), 10, 0), // Witch's house dungeon exit
        GameObject(9084, Tile(2930, 10196), 10, 1) to GameObject(9138, Tile(1939, 4956), 10, 2), // blast furnace enter
        GameObject(10288, Tile(2914, 10188), 10, 2) to GameObject(6085, Tile(1942, 4704), 10, 0), // keldagrim rat pits enter
        GameObject(11739, Tile(2946, 3363), 10, 1) to GameObject(11741, Tile(2946, 3363, 1), 10, 0), // falador west bank ladder
        GameObject(12389, Tile(2960, 3507), 10, 1) to GameObject(12390, Tile(2981, 9916), 10, 0), // goblin village dungeon enter
        GameObject(17974, Tile(3038, 4375), 10, 2) to GameObject(21922, Tile(2647, 3213), 10, 0), // tower of life dungeon exit
        GameObject(21922, Tile(2647, 3213), 10, 0) to GameObject(17974, Tile(3038, 4375), 10, 2), // tower of life dungeon enter
        GameObject(7221, Tile(3057, 5005, 1), 10, 3) to Tile(3057, 5003, 1), // rouges den viewing gallery
        GameObject(29592, Tile(3084, 3452), 10, 2) to GameObject(29589, Tile(3086, 4244), 10, 0), // Stronghold of player safety training centre enter
        GameObject(31417, Tile(3079, 4339), 10, 2) to GameObject(31412, Tile(3338, 9368), 10, 0), // Pollnivneach slayer dungeon turoth
        GameObject(31417, Tile(3126, 4339), 10, 2) to GameObject(31412, Tile(3377, 9367), 10, 0), // Pollnivneach slayer dungeon basilisk
        GameObject(31417, Tile(3085, 4367), 10, 2) to GameObject(31412, Tile(3340, 9426), 10, 0), // Pollnivneach slayer dungeon kurask
        GameObject(31417, Tile(3125, 4364), 10, 2) to GameObject(31412, Tile(3374, 9426), 10, 0), // Pollnivneach slayer dungeon
        GameObject(9141, Tile(3144, 9741), 10, 2) to GameObject(31412, Tile(3374, 9426), 10, 0), // FIXME Unknown yellow training room
        GameObject(10554, Tile(3183, 9766), 10, 1) to Tile(3185, 9766), // Champion challenge exit
        GameObject(37683, Tile(3230, 3241), 10, 1) to GameObject(37684, Tile(3290, 4935), 10, 0), // Unstable foundations enter
        GameObject(20286, Tile(3257, 9225), 10, 1) to GameObject(20287, Tile(3268, 9229), 10, 0), // Sophanem dungeon
    )

    private fun GameObject.getOptions(): List<GameObjectOption> {
        val tiles = linker.getAvailableTiles(this).toSet()
        val openId: Int? = def.getOrNull("open")
        if (openId != null) {
            val openObj = GameObject(openId, tile, shape, rotation)
            return openObj.options(tiles) + options(tiles)
        }
        return options(tiles)
    }

    private fun GameObject.options(tiles: Set<Tile>): List<GameObjectOption> = def.options?.filterNotNull()?.filterNot { it == "Examine" }?.map { GameObjectOption(it, this, tiles) } ?: emptyList()
}
