package world.gregs.voidps.tools

import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.cache.definition.data.ObjectDefinitionFull
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.ObjectDefinitions

object ObjectDefinitions {

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache = CacheDelegate(Settings["storage.cache.path"])
        val definitions = ObjectDefinitions(ObjectDecoder(member = true, lowDetail = false).load(cache))
            .load(configFiles().getValue(Settings["definitions.objects"]))
        /*
            718
            Unwatered
            0 - grass full
            1 - grass -1
            2 - grass -2
            3 - mud (grass -2)

            4-7 - hammerstone growing - 3
            8 - hammerstone grown hig
            9 - hammerstone grown 2
            10 - hammerstone grown 3

            11-18 - asg - 7
            19-27 - yan
            28-37 - krand
            38-48 - wild
            49-55 - barley
            56-63 - jute
            # Watered
            64-67 watered grass
            68-71 watered hammer
            72-74 blank?
            74-79 watered asg
            80-82 blank?
            83-88 watered yan
            89-91 blank
            92-98 watered kran
            99-101 blank
            102-109 watered wild
            110-112 blank
            113-116 watered barley
            117-119 blank
            120-124 watered jute
            125-132 blank

            133-135 - diseased hammer
            136-139 - blank
            140-143 - diseased asg
            144-147 - blank
            148-152 - diseased yan
            153-156 - blank
            157-162 - diseased kran
            163-166 - blank
            167-173 - Diseased Wildblood
            174-177 - blank
            178-180 - Diseased Barley
            181 - blank
            182 - null
            183-184 - blank
            185-188 - Diseased Jute
            189-196 - blank
            197-199 - Dead Hammerstone
            200-203 - blank
            204-207 - Dead Asgarnian
            208-211 - blank
            212-216 - Dead Yanillian
            217-220 - blank
            221-226 - Dead Krandorian
            227-230 - blank
            231-237 - Dead Wildblood
            238-241 - blank
            242-244 - Dead Barley
            245-248 - blank
            249-252 - Dead Jute
            253-255 - blank

            4
                7
            11
                8
            19
                9
            28
                10
            38
                11
            49
                7
            56

         */
//        definitions.definitions.findTransforms(7576)
        for (def in definitions.definitions) {
            if (def.id in 7557..8686) {
//                println("${def.stringId} ${def.name} ${def.id}")
            }
//            if (def.id in 8173 ..8176) { // hops
//                println(
//                    "${def.id} ${def.varbit} ${
//                        def.transforms?.mapIndexed { index, i ->
//                            val name = definitions.get(i).name
//                            "$i ($name ${definitions.get(i).options.contentToString()})"
//                        }
////                        def.transforms?.mapIndexed { index, i ->
////                            val name = definitions.get(i).name
////                            if (index < 4) {
////                                "grass_${index}"
////                            } else {
////                                if (name == "Hops Patch" || name == "null") "blank-$i" else "${name.toSnakeCase().replace("_hops", "")}_${(index - 4).rem(64)}-$i"
////                            }
////                        }
//                    }"
//                )
//            } else if (def.id in 8150..8152) { // Herbs
//                println("${def.id} ${def.varbit} ${def.transforms?.mapIndexed { index, i -> "$index-$i-${definitions.get(i).name}" }}")
            if (def.id in 8550..8555) { // Allotment
                val counter = mutableMapOf<String, Int>()
                println(
                    "${def.id} ${def.varbit} ${
                        def.transforms?.map {
                            val produce = definitions.get(it)
                            val name = produce.stringId.substringBeforeLast("_").toSnakeCase()
                            if (produce.stringId.contains("weeds")) {
                                val index = counter.getOrDefault(name, -1) + 1
                                counter[name] = index
                                if (index > 5) {
                                    null
                                } else {
                                    "weeds_${index}"
                                }
                            } else if (produce.stringId.contains("weeded")) {
                                "weeded"
                            } else if (produce.stringId.endsWith("diseased")) {
                                val product = name.substringBeforeLast("_")
                                val index = counter.getOrDefault("${product}_diseased", 0) + 1
                                counter["${product}_diseased"] = index
                                "${product}_diseased_${index}"
                            } else if (produce.stringId.endsWith("watered")) {
                                val product = name.substringBeforeLast("_")
                                val index = counter.getOrDefault("${product}_watered", -1) + 1
                                counter["${product}_watered"] = index
                                "${product}_watered_${index}"
                            } else if (produce.stringId.endsWith("dead")) {
                                val product = name.substringBeforeLast("_")
                                val index = counter.getOrDefault("${product}_dead", 0) + 1
                                counter["${product}_dead"] = index
                                "${product}_dead_${index}"
                            } else if (produce.stringId.endsWith("fullygrown")) {
                                val index = counter.getOrDefault(produce.stringId, -1) + 1
                                counter[produce.stringId] = index
                                "${name}_${
                                    when (index) {
                                        0 -> "none"
                                        1 -> "compost"
                                        2 -> "super"
                                        else -> "null"
                                    }
                                }"
                            } else {
                                val index = counter.getOrDefault(name, -1) + 1
                                counter[name] = index
                                "${name}_$index"
                            }
                        }?.withIndex()?.filter { it.value != null }?.map { "${it.value} = ${it.index}" }?.joinToString(", ", "{", "}")
                    }"
                )
            }
//            } else if (def.id in 7577..7580) { // bushes
//                println("${def.id} ${def.varbit} ${def.transforms?.mapIndexed { index, i -> "$index-$i-${definitions.get(i).name}" }}")
//            }
        }
        /*

            start = 19
            watered += 64
            diseased += 64
            dead += 64
            (19,  Yanillian Hops), (20,           Yanillian Hops), (21,           Yanillian Hops), (22,           Yanillian Hops), (23,           Yanillian Hops), (24,           Yanillian Hops), (25, Yanillian Hops), (26, Yanillian Hops), (27, Yanillian Hops)
            (83,  Yanillian Hops), (84,           Yanillian Hops), (85,           Yanillian Hops), (86,           Yanillian Hops), (87,           Yanillian Hops), (88,           Yanillian Hops), (89, Hops     Patch), (90, Hops     Patch), (91, Hops     Patch)
            (147, Hops     Patch), (148, Diseased Yanillian Hops), (149, Diseased Yanillian Hops), (150, Diseased Yanillian Hops), (151, Diseased Yanillian Hops), (152, Diseased Yanillian Hops), (153, Hops    Patch), (154, Hops    Patch), (155, Hops    Patch)
            (211, Hops     Patch), (212, Dead     Yanillian Hops), (213, Dead     Yanillian Hops), (214, Dead     Yanillian Hops), (215, Dead     Yanillian Hops), (216, Dead     Yanillian Hops), (217, Hops    Patch), (218, Hops    Patch), (219, Hops    Patch)
         */
    }

    fun Array<ObjectDefinitionFull>.findMatchingName(name: String): List<ObjectDefinitionFull> {
        return indices.mapNotNull {
            val def = getOrNull(it) ?: return@mapNotNull null
            if (def.modelIds != null && def.name.contains(name, true)) {
                println("Found $it ${def.options?.get(0)} ${def.modelIds?.contentDeepToString()}")
                return@mapNotNull def
            } else {
                return@mapNotNull null
            }
        }
    }

    fun Array<ObjectDefinitionFull>.findMatchingSize(width: Int, height: Int) {
        for (i in indices) {
            val def = getOrNull(i) ?: continue
            if (def.modelIds != null && def.sizeX == width && def.sizeY == height) {
                println("Found $i ${def.options?.get(0)} ${def.modelIds?.contentDeepToString()}")
            }
        }
    }

    fun Array<ObjectDefinitionFull>.findMatchingModels(id: Int) {
        val original = getOrNull(id)!!
        val models = original.modelIds!!.map { it.toSet() }.flatten().toSet()
        for (i in indices) {
            val def = getOrNull(i) ?: continue
            if (def.modelIds != null && def.modelIds!!.any { arr -> arr.any { models.contains(it) } }) {
                println("Found $i ${def.options?.get(0)}")
            }
        }
    }

    fun Array<ObjectDefinition>.findTransforms(id: Int): List<ObjectDefinition> {
        return indices.mapNotNull {
            val def = getOrNull(it) ?: return@mapNotNull null
            if (def.transforms?.contains(id) == true) {
                println("Found $it ${def.transforms?.contentToString()}")
                return@mapNotNull def
            }
            return@mapNotNull null
        }
    }

    fun Array<ObjectDefinitionFull>.findVarbit(id: Int): List<ObjectDefinitionFull> {
        return indices.mapNotNull {
            val def = getOrNull(it) ?: return@mapNotNull null
            if (def.varbit == id) {
                println("Found $it ${def.varbit}")
                return@mapNotNull def
            }
            return@mapNotNull null
        }
    }
}
