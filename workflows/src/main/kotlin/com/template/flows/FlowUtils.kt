package com.template.flows

import com.google.common.collect.ImmutableList
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowException
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.VaultService
import net.corda.core.node.services.vault.QueryCriteria
import org.openapitools.model.Blockchain
import java.util.*



fun VaultService.getProductByLinearId(owner: Party, uuid: UUID) : StateAndRef<Blockchain> {

    val queryCriteria = QueryCriteria.LinearStateQueryCriteria(
            participants = ImmutableList.of(owner),
            uuid = ImmutableList.of(uuid),
            status = Vault.StateStatus.UNCONSUMED,
            contractStateTypes = setOf(Blockchain::class.java)
    )

    val foundProductState = this.queryBy(Blockchain::class.java, queryCriteria).states
    if (foundProductState.size != 1) {
        System.out.println("Linear Id 1:$uuid")
        throw FlowException(String.format("Product state with id %s not found.", uuid))
    }
    //System.out.println("Linear Id 2:"+linearId);
    return foundProductState[0]

}

