package com.github.hudifu316.applestamp.contracts

import com.github.hudifu316.applestamp.states.AppleStamp
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class ContractTests {
    private val ledgerServices: MockServices = MockServices(listOf("com.github.hudifu316"))
    var alice = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))
    var bob = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))

    @Test
    fun StampIssuanceCanOnlyHaveOneOutput() {
        val stamp1 = AppleStamp("Fuji Apple", alice.party, bob.party)
        val stamp2 = AppleStamp("ときりんご", alice.party, bob.party)

        ledgerServices.ledger {
            transaction {
                //fails because of two output
                output(AppleStampContract.ID, stamp1)
                output(AppleStampContract.ID, stamp2)
                command(alice.publicKey, AppleStampContract.Commands.Issue())
                fails()
            }
            //pass
            transaction {
                //passing transaction
                output(AppleStampContract.ID, stamp2)
                command(alice.publicKey, AppleStampContract.Commands.Issue())
                verifies()
            }
        }
    }

    @Test
    fun StampMustHaveDescription(){
        val stamp1 = AppleStamp("", alice.party, bob.party)
        val stamp2 = AppleStamp("ときりんご", alice.party, bob.party)

        ledgerServices.ledger {
            transaction {
                //fails because of no description
                output(AppleStampContract.ID, stamp1)
                command(alice.publicKey, AppleStampContract.Commands.Issue())
                fails()
            }
            //pass
            transaction {
                //passing transaction
                output(AppleStampContract.ID, stamp2)
                command(alice.publicKey, AppleStampContract.Commands.Issue())
                verifies()
            }
        }
    }
}
