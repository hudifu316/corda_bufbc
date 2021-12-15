package com.github.hudifu316.applestamp.flows

import co.paralleluniverse.fibers.Suspendable
import com.github.hudifu316.applestamp.contracts.BasketOfApplesContract
import com.github.hudifu316.applestamp.states.AppleStamp
import com.github.hudifu316.applestamp.states.BasketOfApples
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.*
import java.util.stream.Collectors

@InitiatingFlow
@StartableByRPC
class RedeemApplesInitiator(val buyer: Party, val stampId: UniqueIdentifier) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        // Step 1. Get a reference to the notary service on our network and our key pair.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        // Step2 Compose the applestamp Input and BasketOfApples Output State
        val inputCriteria = QueryCriteria.LinearStateQueryCriteria()
            .withUuid(listOf(UUID.fromString(stampId.toString())))
            .withStatus(Vault.StateStatus.UNCONSUMED)
            .withRelevancyStatus(Vault.RelevancyStatus.RELEVANT)
        val appleStampStateAndRef = serviceHub.vaultService.queryBy<AppleStamp>(inputCriteria).states.first()

        val outputCriteria = QueryCriteria.VaultQueryCriteria()
            .withStatus(Vault.StateStatus.UNCONSUMED)
            .withRelevancyStatus(Vault.RelevancyStatus.RELEVANT)
        val basketOfApplesStateAndRef = serviceHub.vaultService.queryBy<BasketOfApples>(outputCriteria).states.first()
        val originalBasketOfApple: BasketOfApples = basketOfApplesStateAndRef.state.data
        val output = originalBasketOfApple.changeOwner(buyer)

        // Step 3. Create a new TransactionBuilder object.
        val farm = ourIdentity
        val builder = TransactionBuilder(notary)
            .addCommand(BasketOfApplesContract.Commands.Redeem(), listOf(farm.owningKey, buyer.owningKey))
            .addInputState(appleStampStateAndRef)
            .addInputState(basketOfApplesStateAndRef)
            .addOutputState(output)

        // Step 4. Verify and sign it with our KeyPair.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        // Step 5. Collect the other party's signature using the SignTransactionFlow.
        val otherParties: MutableList<Party> =
            output.participants.stream().map { el: AbstractParty? -> el as Party? }.collect(Collectors.toList())
        otherParties.remove(ourIdentity)
        val sessions = otherParties.stream().map { el: Party? -> initiateFlow(el!!) }.collect(Collectors.toList())

        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        // Step 6. Assuming no exceptions, we can now finalise the transaction
        return subFlow(FinalityFlow(stx, sessions))

    }
}

@InitiatedBy(RedeemApplesInitiator::class)
class RedeemApplesResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                //Addition checks
            }
        }
        val txId = subFlow(signTransactionFlow).id
        return subFlow(ReceiveFinalityFlow(counterpartySession, txId))
    }
}
