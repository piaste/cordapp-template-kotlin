package org.openapitools.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.serialization.CordaSerializable

/**
 * 
 * @param codice Codice dell'ingrediente utilizzato
 * @param descrizione Lotto dell'ingrediente utilizzato
 * @param lotto Lotto dell'ingrediente utilizzato
 */
@CordaSerializable
data class Ingrediente (


        @JsonProperty("codice") val codice: kotlin.String,

        @JsonProperty("descrizione") val descrizione: kotlin.String,

        @JsonProperty("lotto") val lotto: kotlin.String
) {

}

