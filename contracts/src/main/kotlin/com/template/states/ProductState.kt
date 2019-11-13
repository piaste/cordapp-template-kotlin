package com.template.states


import com.template.contracts.*
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import java.math.BigDecimal

// *********
// * State *
// *********
@BelongsToContract(ProductContract::class)
data class ProductState
    ( val productCode: String
    , val batchCode: String
    , val quantity: BigDecimal
    , val owner: Party
    , override val linearId : UniqueIdentifier
    , override val participants: List<AbstractParty> = listOf(owner)
    ) : LinearState
