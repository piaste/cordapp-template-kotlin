package org.openapitools.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import org.openapitools.model.BlockchainOperazioni
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

/**
 * 
 * @param idArticolo 
 * @param idLotto 
 * @param operazioni 
 */
data class Blockchain (

        @get:NotNull 
        @JsonProperty("idArticolo") val idArticolo: kotlin.String,

        @get:NotNull 
        @JsonProperty("idLotto") val idLotto: kotlin.String,

        @get:NotNull 
        @JsonProperty("operazioni") val operazioni: kotlin.collections.List<BlockchainOperazioni>
) {

}

