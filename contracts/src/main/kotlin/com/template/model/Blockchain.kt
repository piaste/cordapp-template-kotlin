package org.openapitools.model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import com.template.contracts.ProductContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import org.openapitools.model.BlockchainOperazioni
import java.math.BigDecimal

/**
 * 
 * @param idArticolo 
 * @param idLotto 
 * @param operazioni 
 */
@BelongsToContract(ProductContract::class)
data class Blockchain (

        @JsonProperty("idArticolo") val idArticolo: String,

        @JsonProperty("idLotto") val idLotto: String,

        @JsonProperty("operazioni") val operazioni: List<BlockchainOperazioni>

        , @JsonIgnore val quantity: BigDecimal

        , @JsonIgnore val owner: Party

        , @JsonIgnore override val linearId : UniqueIdentifier
        , @JsonIgnore override val participants: List<AbstractParty> = listOf(owner)

) : LinearState

