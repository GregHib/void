package world.gregs.voidps.engine.client.variable

import com.fasterxml.jackson.annotation.JsonIgnore

class Variables(
    val variables: MutableMap<String, Any> = mutableMapOf()
) {
    @JsonIgnore
    val temporaryVariables: MutableMap<String, Any> = mutableMapOf()


}