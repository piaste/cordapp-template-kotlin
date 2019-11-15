package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.ProductContract
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import org.openapitools.model.Blockchain
import org.openapitools.model.BlockchainOperazioni
import org.openapitools.model.Operazione
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
        , val operation: Operazione
        , private val owner: Party) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() : SignedTransaction {
        // Initiator flow logic goes here.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val command = Command(ProductContract.Commands.Issue(), listOf(owner).map { it.owningKey })
        val productState = Blockchain(
                  idArticolo = productCode
                , idLotto = batchCode
                , quantity = quantity
                , owner = owner
                , operazioni = listOf(BlockchainOperazioni(UUID.randomUUID().toString(), operation))
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
                "The output must be a ProductState" using (output is Blockchain)
            }
        }
        val txWeJustSignedId = subFlow(signedTransactionFlow)
        return subFlow(ReceiveFinalityFlow(counterpartySession, txWeJustSignedId.id))
    }
}


