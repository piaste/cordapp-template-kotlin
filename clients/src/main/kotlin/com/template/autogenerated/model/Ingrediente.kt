package org.openapitools.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

/**
 * 
 * @param codice Codice dell'ingrediente utilizzato
 * @param descrizione Lotto dell'ingrediente utilizzato
 * @param lotto Lotto dell'ingrediente utilizzato
 */
data class Ingrediente (

        @get:NotNull 
        @JsonProperty("codice") val codice: kotlin.String,

        @get:NotNull 
        @JsonProperty("descrizione") val descrizione: kotlin.String,

        @get:NotNull 
        @JsonProperty("lotto") val lotto: kotlin.String
) {

}

