package com.template.contracts

import com.template.states.ProductState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.math.BigDecimal

// ************
// * Contract *
// ************
class ProductContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        val ID = ProductContract::class.qualifiedName!!
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        val command = tx.commands.requireSingleCommand<Commands>().value
        when(command) {
            is Commands.Issue -> requireThat {
                "There should be no input state" using (tx.inputs.isEmpty())
                "There should be one output state" using (tx.outputs.size == 1)
                "The output state must be of type ProductState" using (tx.outputs[0].data is ProductState)
                val outputState = tx.outputs[0].data as ProductState
                "The product code must not be blank" using (outputState.productCode.isNotBlank())
                "The batch code must not be blank" using (outputState.batchCode.isNotBlank())
            }
            is Commands.Move -> requireThat {
                "There should be one input state" using (tx.inputs.size == 1)
                "The input state must be of type ProductState" using (tx.inputs[0].state.data    is ProductState)
                val inputState = tx.inputs[0].state.data as ProductState

                "There should be at least one output state" using (tx.outputs.isNotEmpty())
                "The output states must be of type ProductState" using (tx.outputs.all { it.data  is ProductState })

                val newStates = tx.outputs.map { it.data as ProductState }
                "The product code must not change" using (newStates.all { it.productCode == inputState.productCode})
                "The batch code must not change" using (newStates.all { it.batchCode == inputState.batchCode })

                "The total quantity must not increase" using (newStates.map {it.quantity}
                                                                       .fold(BigDecimal.ZERO, BigDecimal::add) <= inputState.quantity )
            }
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Issue : Commands
        class Move : Commands
    }
}