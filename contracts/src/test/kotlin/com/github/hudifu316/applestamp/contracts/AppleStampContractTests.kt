package com.github.hudifu316.applestamp.contracts

import com.github.hudifu316.applestamp.states.AppleStamp
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class AppleStampContractTests {
    var alice = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))
    var bob = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))
    private val ledgerServices: MockServices = MockServices(listOf("com.github.hudifu316"), alice, bob)

    @Test
    fun StampIssuance() {
        val stamp1 = AppleStamp("Fuji Apple", alice.party, bob.party)
        val stamp2 = AppleStamp("ときりんご", alice.party, bob.party)
        val stamp3 = AppleStamp("", alice.party, bob.party)

        ledgerServices.ledger {
            transaction {
                command(alice.publicKey, AppleStampContract.Commands.Issue())

                tweak {
                    this `fails with` "A transaction must contain at least one input or output state"
                }

                tweak {
                    //passing transaction
                    output(AppleStampContract.ID, stamp1)
                    verifies()
                }
                tweak {
                    output(AppleStampContract.ID, stamp1)
                    output(AppleStampContract.ID, stamp2)
                    this `fails with` "This transaction should only have one applestamp state as output"
                }
                tweak {
                    command(alice.publicKey, AppleStampContract.Commands.Issue())
                    output(AppleStampContract.ID, stamp1)
                    verifies()
                }
                tweak {
                    //fails because of no description
                    output(AppleStampContract.ID, stamp3)
                    this `fails with` "The output applestamp state should have clear description of the type of redeemable goods"
                }
                input(AppleStampContract.ID, stamp1)
                output(AppleStampContract.ID, "output", stamp2)
                verifies()
            }
            verifies()
        }
    }

    @Test
    fun RedeemBasketOfApples() {
        val stamp1 = AppleStamp("", alice.party, bob.party)
        val stamp2 = AppleStamp("ときりんご", alice.party, bob.party)

        ledgerServices.ledger {
            transaction {
                tweak {
                    output(AppleStampContract.ID, stamp2)
                    command(alice.publicKey, BasketOfApplesContract.Commands.PackBasket())
                    this `fails with` "Incorrect type of applestamp Commands"
                }
                input(AppleStampContract.ID, stamp1)
                output(AppleStampContract.ID, stamp2)
                command(alice.publicKey, BasketOfApplesContract.Commands.Redeem())
                verifies()
            }
        }
    }
}
