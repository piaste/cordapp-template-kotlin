package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.ProductContract
import net.corda.core.contracts.Command
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import org.openapitools.model.Blockchain
import org.openapitools.model.BlockchainOperazioni
import org.openapitools.model.Operazione
import java.util.*

@InitiatingFlow
@StartableByRPC
class ProductWorkInitiator(
          val id: UUID
        , val operazione : Operazione
        ,  val owner : Party) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() : SignedTransaction {
        // Initiator flow logic goes here.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val command = Command(ProductContract.Commands.Work(), listOf(owner).map { it.owningKey })

        val ownedState = serviceHub.vaultService.getProductByLinearId(owner, id)

        val beforeState = ownedState.state.data
        //TODO: non parte dell'API
        val newQuantity = beforeState.quantity
        val afterState = beforeState.copy(
                quantity = newQuantity,
                operazioni = beforeState.operazioni.plus(BlockchainOperazioni(UUID.randomUUID().toString(), operazione))
        )

        val txBuilder = TransactionBuilder(notary)
                .addInputState(ownedState)
                .addOutputState(afterState, ProductContract.ID)
                .addCommand(command)

        txBuilder.verify(serviceHub)
        val initialSignedTransaction = serviceHub.signInitialTransaction(txBuilder)
        val sessions = (listOf(owner) - ourIdentity).map { initiateFlow(it) }
        val fullySignedTransaction = subFlow(CollectSignaturesFlow(initialSignedTransaction, sessions))
        return subFlow(FinalityFlow(fullySignedTransaction, sessions))

    }
}

@InitiatedBy(ProductWorkInitiator::class)
class ProductWorkResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call() : SignedTransaction {
        // Responder flow logic goes here.
        val signedTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            // Note: The checkTransaction function should be used only to model business logic.
            // A contract’s verify function should be used to define what is and is not possible within a transaction.
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "The output must be a ProductState" using (output is Blockchain)
            }
        }
        val txWeJustSignedId = subFlow(signedTransactionFlow)
        return subFlow(ReceiveFinalityFlow(counterpartySession, txWeJustSignedId.id))
    }
}