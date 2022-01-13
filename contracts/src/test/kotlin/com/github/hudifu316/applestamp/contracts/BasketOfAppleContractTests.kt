package com.github.hudifu316.applestamp.contracts

import com.github.hudifu316.applestamp.states.AppleStamp
import com.github.hudifu316.applestamp.states.BasketOfApples
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class BasketOfAppleContractTests {
    var alice = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))
    var bob = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))
    private val ledgerServices: MockServices = MockServices(listOf("com.github.hudifu316"), alice, bob)

    @Test
    fun BasketPacking() {
        val stamp1 = AppleStamp("Fuji Apple", alice.party, bob.party)
        val basketNormal = BasketOfApples("せつめいぶん", alice.party, 1000)
        val basketMaxWeight = BasketOfApples("せつめいぶん", alice.party, Int.MAX_VALUE)
        val basketNoDesc = BasketOfApples("", alice.party, 1)
        val basketWeight0 = BasketOfApples("せつめいぶん", alice.party, 0)
        val basketMinusWeight = BasketOfApples("せつめいぶん", alice.party, -1)

        ledgerServices.ledger {
            transaction {
                command(alice.publicKey, BasketOfApplesContract.Commands.PackBasket())

                tweak {
                    // Outputが２個
                    output(BasketOfApplesContract.ID, basketNormal)
                    output(BasketOfApplesContract.ID, basketMaxWeight)
                    this `fails with` "This transaction should only have one BasketOfApples state as output"
                }
                tweak {
                    // descriptionが空
                    output(BasketOfApplesContract.ID, basketNoDesc)
                    this `fails with` "The output of the BasketOfApples state should have a clear description of the apple product."
                }
                tweak {
                    // Weightがゼロ
                    output(BasketOfApplesContract.ID, basketWeight0)
                    this `fails with` "The output of the BasketOfApples state should have a non-zero weight"
                }
                tweak {
                    // Weightがマイナス
                    output(BasketOfApplesContract.ID, basketMinusWeight)
                    this `fails with` "The output of the BasketOfApples state should have a non-zero weight"
                }
                output(BasketOfApplesContract.ID, basketMaxWeight)
                verifies()
            }
            verifies()
        }
    }

    @Test
    fun BasketRedeem() {
        val stamp1 = AppleStamp("", alice.party, bob.party)
        val basketNormal = BasketOfApples("正常Basket1", alice.party, Int.MAX_VALUE)
        val basketNormal2 = BasketOfApples("正常Basket2", alice.party, Int.MAX_VALUE)
        val basketWeight0 = BasketOfApples("せつめいぶん", alice.party, 0)

        ledgerServices.ledger {
            transaction {
                command(alice.publicKey, BasketOfApplesContract.Commands.Redeem())

                tweak {
                    input(AppleStampContract.ID, stamp1)
                    input(BasketOfApplesContract.ID, basketNormal)
                    input(BasketOfApplesContract.ID, basketNormal2)
                    output(BasketOfApplesContract.ID, basketNormal2)
                    this `fails with` "The transaction should consume two states."
                }

                tweak {
                    input(AppleStampContract.ID, stamp1)
                    input(BasketOfApplesContract.ID, basketNormal)
                    output(BasketOfApplesContract.ID, basketWeight0)
                    this `fails with` "The weight of the basket of apples must be greater than zero."
                }

                input(AppleStampContract.ID, stamp1)
                input(BasketOfApplesContract.ID, basketNormal)
                val basketChangeOwner = basketNormal.changeOwner(bob.party)
                output(BasketOfApplesContract.ID, basketChangeOwner)
                verifies()
            }
            verifies()
        }
    }

}
