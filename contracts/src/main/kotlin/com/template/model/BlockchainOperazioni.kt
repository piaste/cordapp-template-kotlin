package org.openapitools.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.serialization.CordaSerializable
import org.openapitools.model.Operazione

/**
 * 
 * @param idOperazione Identificativo univoco (hash?) dell'operazione nella blockchain
 * @param operazione 
 */
@CordaSerializable
data class BlockchainOperazioni (

        @JsonProperty("idOperazione") val idOperazione: String? = null,

        @JsonProperty("operazione") val operazione: Operazione? = null
) {

}

