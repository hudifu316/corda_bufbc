package com.github.hudifu316.applestamp

import com.github.hudifu316.applestamp.flows.CreateAndIssueAppleStampInitiator
import com.github.hudifu316.applestamp.flows.PackageAppleFlow
import com.github.hudifu316.applestamp.states.AppleStamp
import com.github.hudifu316.applestamp.states.BasketOfApples
import net.corda.core.node.services.Vault
import net.corda.core.node.services.Vault.StateStatus
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Future


class FlowTests {
    private lateinit var network: MockNetwork
    private lateinit var a: StartedMockNode
    private lateinit var b: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(
            MockNetworkParameters(
                cordappsForAllNodes = listOf(
                    TestCordapp.findCordapp("com.github.hudifu316.applestamp.contracts"),
                    TestCordapp.findCordapp("com.github.hudifu316.applestamp.states"),
                    TestCordapp.findCordapp("com.github.hudifu316.applestamp.flows")
                )
            )
        )
        a = network.createPartyNode()
        b = network.createPartyNode()
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun CreateAndIssueAppleStampInitiatorSuccess() {
        val flow = CreateAndIssueAppleStampInitiator(
            "take this stamp in exchange of basket of apples",
            b.info.legalIdentities[0]
        )
        val future: Future<SignedTransaction> = a.startFlow(flow)
        network.runNetwork()

        //successful query means the state is stored at node b's vault. Flow went through.
        val inputCriteria: QueryCriteria = QueryCriteria.VaultQueryCriteria().withStatus(StateStatus.UNCONSUMED)
        val state = b.services.vaultService.queryBy(AppleStamp::class.java, inputCriteria).states[0].state.data
        assert(state.stampDesc == "take this stamp in exchange of basket of apples")
        assert(state.holder == b.info.legalIdentities[0])
        assert(state.issuer == a.info.legalIdentities[0])
    }

    @Test
    fun PackageAppleFlowSuccess() {
        val flow = PackageAppleFlow("take this stamp in exchange of basket of apples", 10)
        val future: Future<SignedTransaction> = a.startFlow(flow)
        network.runNetwork()

        //successful query means the state is stored at node b's vault. Flow went through.
        val inputCriteria: QueryCriteria =
            QueryCriteria.VaultQueryCriteria()
                .withStatus(StateStatus.UNCONSUMED)
        val state = a.services.vaultService.queryBy(BasketOfApples::class.java, inputCriteria).states[0].state.data
        assert(state.description == "take this stamp in exchange of basket of apples")
        assert(state.farm == a.info.legalIdentities[0])
        assert(state.owner == a.info.legalIdentities[0])
        assert(state.weight == 10)
    }
}
