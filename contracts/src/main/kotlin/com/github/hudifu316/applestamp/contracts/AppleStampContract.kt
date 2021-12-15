package com.github.hudifu316.applestamp.contracts

import com.github.hudifu316.applestamp.states.AppleStamp
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class AppleStampContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.github.hudifu316.applestamp.contracts.AppleStampContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        when (tx.commands.first().value) {
            is Commands.Issue -> requireThat {
                val output = tx.outputsOfType<AppleStamp>().first()
                "This transaction should only have one applestamp state as output".using(tx.outputStates.size == 1)
                "The output applestamp state should have clear description of the type of redeemable goods".using(output.stampDesc != "")
            }
            is BasketOfApplesContract.Commands.Redeem -> {
                //Transaction verification will happen in BasketOfApples Contract
            }
            else -> {
                //Unrecognized Command type
                throw IllegalArgumentException("Incorrect type of applestamp Commands");
            }
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Issue : Commands
    }
}
