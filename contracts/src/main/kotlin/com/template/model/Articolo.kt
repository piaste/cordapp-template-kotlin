package org.openapitools.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.serialization.CordaSerializable

/**
 * 
 * @param idArticolo 
 * @param descrizione 
 * @param lottiEsistenti 
 */
@CordaSerializable
data class Articolo (

        @JsonProperty("idArticolo") val idArticolo: kotlin.String,

        @JsonProperty("lottiEsistenti") val lottiEsistenti: kotlin.collections.List<kotlin.String>,

        @JsonProperty("descrizione") val descrizione: kotlin.String? = null
) {

}

