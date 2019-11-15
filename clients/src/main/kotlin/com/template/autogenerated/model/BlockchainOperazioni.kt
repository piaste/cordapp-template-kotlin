package org.openapitools.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import org.openapitools.model.Operazione
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

/**
 * 
 * @param idOperazione Identificativo univoco (hash?) dell'operazione nella blockchain
 * @param operazione 
 */
data class BlockchainOperazioni (

        @JsonProperty("idOperazione") val idOperazione: kotlin.String? = null,

        @JsonProperty("operazione") val operazione: Operazione? = null
) {

}

