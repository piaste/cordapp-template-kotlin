package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.ProductContract
import net.corda.core.contracts.Command
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import org.openapitools.model.Blockchain
import java.math.BigDecimal
import java.util.*

@InitiatingFlow
@StartableByRPC
class ProductMoveInitiator(
          val id: UUID
        , val moveQuantity: BigDecimal
        , val sender: Party
        , val receiver: Party) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() : SignedTransaction {
        // Initiator flow logic goes here.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val command = Command(ProductContract.Commands.Move(), listOf(sender, receiver).map { it.owningKey })

        val ownedState = serviceHub.vaultService.getProductByLinearId(sender, id)

        val beforeState = ownedState.state.data

        val afterStateMoved = beforeState.copy(owner = receiver,
                                                         quantity = moveQuantity,
                                                        linearId = UniqueIdentifier())

        val afterStateRemainder = beforeState.copy(
                owner = sender,
                quantity = beforeState.quantity - moveQuantity,
                linearId = ownedState.state.data.linearId)

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
                "The output must be a ProductState" using (output is Blockchain)
            }
        }
        val txWeJustSignedId = subFlow(signedTransactionFlow)
        return subFlow(ReceiveFinalityFlow(counterpartySession, txWeJustSignedId.id))
    }
}