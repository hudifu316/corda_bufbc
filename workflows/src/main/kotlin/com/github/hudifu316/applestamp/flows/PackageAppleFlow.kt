package com.github.hudifu316.applestamp.flows

import co.paralleluniverse.fibers.Suspendable
import com.github.hudifu316.applestamp.contracts.BasketOfApplesContract
import com.github.hudifu316.applestamp.states.BasketOfApples
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class PackageAppleFlow(private val appleDescription: String, private val weight: Int) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        // Step 1. Get a reference to the notary service on our network and our key pair.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        // Step2 Compose the BasketOfApple Output State
        val farm = ourIdentity
        val output = BasketOfApples(appleDescription, farm, weight)

        // Step 3. Create a new TransactionBuilder object.
        val builder = TransactionBuilder(notary)
            .addCommand(BasketOfApplesContract.Commands.PackBasket(), listOf(farm.owningKey))
            .addOutputState(output)
        // Step 4. Verify and sign it with our KeyPair.
        builder.verify(serviceHub)
        val stx = serviceHub.signInitialTransaction(builder)

        // Step 5. Collect the other party's signature using the SignTransactionFlow.
        // Single-party flow doesn't need collect signatures.

        // Step 6. Assuming no exceptions, we can now finalise the transaction
        return subFlow(FinalityFlow(stx, emptyList()))
    }
}
