package com.github.hudifu316.applestamp.contracts

import com.github.hudifu316.applestamp.states.AppleStamp
import com.github.hudifu316.applestamp.states.BasketOfApples
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.Requirements.using
import net.corda.core.transactions.LedgerTransaction

class BasketOfApplesContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.github.hudifu316.applestamp.contracts.BasketOfApplesContract"
    }

    override fun verify(tx: LedgerTransaction) {
        when (tx.commands.first().value) {
            is Commands.PackBasket -> {
                val output = tx.outputsOfType<BasketOfApples>().first()
                "This transaction should only have one BasketOfApples state as output".using(tx.outputStates.size == 1)
                "The output of the BasketOfApples state should have a clear description of the apple product.".using(
                    output.description != ""
                )
                "The output of the BasketOfApples state should have a non-zero weight".using(output.weight > 0)
            }

            is Commands.Redeem -> {
                val appleStampInput = tx.inputsOfType<AppleStamp>().first()
                val basketOfApplesOutput = tx.outputsOfType<BasketOfApples>().first()
                "The transaction should consume two states.".using(tx.inputs.size == 2)
                "The issuer of the applestamp should be the producing farm of this basket of apples.".using(
                    appleStampInput.issuer == basketOfApplesOutput.farm
                )
                "The weight of the basket of apples must be greater than zero.".using(basketOfApplesOutput.weight > 0)

            }

            else -> {
                //Unrecognized Command type
                throw IllegalArgumentException("Incorrect type of applestamp Commands");
            }
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Redeem : Commands
        class PackBasket : Commands
    }
}
