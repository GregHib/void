package world.gregs.voidps.engine.data.exchange

import java.util.*

class Offers(
    val sellByItem: MutableMap<String, TreeMap<Int, MutableList<OpenOffer>>> = mutableMapOf(),
    val buyByItem: MutableMap<String, TreeMap<Int, MutableList<OpenOffer>>> = mutableMapOf(),
    val offers: MutableMap<Int, OpenOffer> = mutableMapOf(),
    var counter: Int = 0,
)