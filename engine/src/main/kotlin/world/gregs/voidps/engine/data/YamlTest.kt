package world.gregs.voidps.engine.data

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.readValue

object YamlTest {

    /*

        List item
        `- `
        Map key
        <key>

        Map value
        <value>

        Map entry/key-value pair
        <key>: <value>

        <key>:
        <key>: <value>

        - <value>
        - <key>: <value>
        - <key>:
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val loader = FileStorage.yamlMapper(false)
            .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
            .disable(MapperFeature.ALLOW_IS_GETTERS_FOR_NON_BOOLEAN)
        println(loader.readValue<Map<String, Any>>("""
            fishing_spot_lure_bait:
              id: 329
              fishing:
                Lure:
                  items:
                  - fly_fishing_rod
                  bait:
                    feather:
                    - raw_trout
                    - raw_salmon
                    stripy_feather:
                    - raw_rainbow_fish
                Bait:
                  items:
                  - fishing_rod
                  bait:
                    fishing_bait:
                    - pike
        """.trimIndent()))
//        Expected :{fishing_spot_lure_bait=>{fishing=>{Bait=>{bait=>{fishing_bait=>[pike]}, items=>[fishing_rod]}, Lure=>{bait=>{stripy_feather=>raw_rainbow_fish, feather=>[raw_trout, raw_salmon]}, items=>[fly_fishing_rod]}}, id=>329}}
//        Actual   :{fishing_spot_lure_bait=>{fishing=>{Bait=>{bait=>{fishing_bait=>[pike]}, items=>[fishing_rod]}, Lure=>{bait=>{feather=>[raw_rainbow_fish], stripy_feather=>}, items=>[fly_fishing_rod]}}, id=>329}}
    }

}