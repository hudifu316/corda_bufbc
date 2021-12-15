package com.github.hudifu316.applestamp.flows

import co.paralleluniverse.fibers.Suspendable
import com.github.hudifu316.applestamp.contracts.AppleStampContract
import com.github.hudifu316.applestamp.states.AppleStamp
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.stream.Collectors


// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class CreateAndIssueAppleStampInitiator(private val stampDescription: String, private val holder: Party) :
    FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {

        // Step 1. Get a reference to the notary service on our network and our key pair.
        // Note: ongoing work to support multiple notary identities is still in progress.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]


        // Step2 Compose the applestamp Output State
        val issuer = ourIdentity
        val uniqueIdentifier = UniqueIdentifier()
        val participants = listOf(issuer, holder)
        val output = AppleStamp(stampDescription, issuer, holder, participants, uniqueIdentifier)

        // Step 3. Create a new TransactionBuilder object.
        val builder = TransactionBuilder(notary)
            .addCommand(AppleStampContract.Commands.Issue(), listOf(issuer.owningKey, holder.owningKey))
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

@InitiatedBy(CreateAndIssueAppleStampInitiator::class)
class CreateAndIssueAppleStampResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
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

