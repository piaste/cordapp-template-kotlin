package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.google.common.collect.ImmutableList
import com.template.contracts.ProductContract
import com.template.states.ProductState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.flows.FlowException
import net.corda.core.contracts.StateAndRef
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import java.math.BigDecimal
import java.util.*


// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class ProductIssueInitiator(
        val productCode: String
        , val batchCode: String
        , val quantity: BigDecimal
        , private val owner: Party) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() : SignedTransaction {
        // Initiator flow logic goes here.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val command = Command(ProductContract.Commands.Issue(), listOf(owner).map { it.owningKey })
        val productState = ProductState(
                productCode = productCode
                , batchCode = batchCode
                , quantity = quantity
                , owner = owner
                , linearId = UniqueIdentifier())

        val txBuilder = TransactionBuilder(notary)
                .addOutputState(productState, ProductContract.ID)
                .addCommand(command)

        txBuilder.verify(serviceHub)
        val initialSignedTransaction = serviceHub.signInitialTransaction(txBuilder)
        val sessions = (productState.participants - ourIdentity).map { initiateFlow(it as Party) }
        val fullySignedTransaction = subFlow(CollectSignaturesFlow(initialSignedTransaction, sessions))
        return subFlow(FinalityFlow(fullySignedTransaction, sessions))

    }
}

@InitiatedBy(ProductIssueInitiator::class)
class ProductIssueResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call() : SignedTransaction {
        // Responder flow logic goes here.
        val signedTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            // Note: The checkTransaction function should be used only to model business logic.
            // A contract’s verify function should be used to define what is and is not possible within a transaction.
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "The output must be a ProductState" using (output is ProductState)
            }
        }
        val txWeJustSignedId = subFlow(signedTransactionFlow)
        return subFlow(ReceiveFinalityFlow(counterpartySession, txWeJustSignedId.id))
    }
}


@InitiatingFlow
@StartableByRPC
class ProductMoveInitiator(
        val id: UUID
        , val moveQuantity: BigDecimal
        , val sender: Party
        , val receiver: Party) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    fun getProductByLinearId(uuid: UUID) : StateAndRef<ProductState> {
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(
                participants = ImmutableList.of(sender),
                uuid = ImmutableList.of(uuid),
                status = Vault.StateStatus.UNCONSUMED,
                contractStateTypes = setOf(ProductState::class.java))

        val foundProductState = serviceHub.vaultService.queryBy(ProductState::class.java, queryCriteria).states
        if (foundProductState.size != 1) {
            System.out.println("Linear Id 1:$uuid")
            throw FlowException(String.format("Obligation with id %s not found.", uuid))
        }
        //System.out.println("Linear Id 2:"+linearId);
        return foundProductState[0]

    }

    @Suspendable
    override fun call() : SignedTransaction {
        // Initiator flow logic goes here.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val command = Command(ProductContract.Commands.Move(), listOf(sender, receiver).map { it.owningKey })

        val ownedState = getProductByLinearId(id)

        //val beforeState = ProductState(
        //        productCode = productCode
        //        , batchCode = batchCode
        //        , quantity = quantity
        //        , owner = sender
        //        , linearId = UniqueIdentifier())
        val beforeState = ownedState.state.data

        val afterStateMoved = beforeState.copy(owner = receiver, quantity = moveQuantity, linearId = UniqueIdentifier())
        val afterStateRemainder = beforeState.copy(owner = sender, quantity = beforeState.quantity - moveQuantity, linearId = UniqueIdentifier())

        val txBuilder = TransactionBuilder(notary)
                .addInputState(ownedState)
                .addOutputState(afterStateMoved, ProductContract.ID)
                .addOutputState(afterStateRemainder, ProductContract.ID)
                .addCommand(command)

        txBuilder.verify(serviceHub)
        val initialSignedTransaction = serviceHub.signInitialTransaction(txBuilder)
        val sessions = (listOf(sender, receiver) - ourIdentity).map { initiateFlow(it) }
        val fullySignedTransaction = subFlow(CollectSignaturesFlow(initialSignedTransaction, sessions))
        return subFlow(FinalityFlow(fullySignedTransaction, sessions))

    }
}

@InitiatedBy(ProductMoveInitiator::class)
class ProductMoveResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call() : SignedTransaction {
        // Responder flow logic goes here.
        val signedTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            // Note: The checkTransaction function should be used only to model business logic.
            // A contract’s verify function should be used to define what is and is not possible within a transaction.
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs[0].data
                "The output must be a ProductState" using (output is ProductState)
            }
        }
        val txWeJustSignedId = subFlow(signedTransactionFlow)
        return subFlow(ReceiveFinalityFlow(counterpartySession, txWeJustSignedId.id))
    }
}
