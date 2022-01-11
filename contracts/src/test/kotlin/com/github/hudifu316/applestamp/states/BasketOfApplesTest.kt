package com.github.hudifu316.applestamp.states

import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.testing.core.TestIdentity
import org.junit.Test
import kotlin.test.assertEquals

class BasketOfApplesTest{
    @Test
    fun hasFieldOfCorrectType() {
        BasketOfApples::class.java.getDeclaredField("description")
        assertEquals(BasketOfApples::class.java.getDeclaredField("description").type, String::class.java)

        BasketOfApples::class.java.getDeclaredField("farm")
        assertEquals(BasketOfApples::class.java.getDeclaredField("farm").type, Party::class.java)

        BasketOfApples::class.java.getDeclaredField("owner")
        assertEquals(BasketOfApples::class.java.getDeclaredField("owner").type, Party::class.java)

        BasketOfApples::class.java.getDeclaredField("weight")
        assertEquals(BasketOfApples::class.java.getDeclaredField("weight").type, Int::class.java)

        BasketOfApples::class.java.getMethod("changeOwner", Party::class.java)
        assertEquals(BasketOfApples::class.java.getMethod("changeOwner", Party::class.java).returnType, BasketOfApples::class.java)

    }

    @Test
    fun canChangeOwner(){
        val initIdentity = TestIdentity(CordaX500Name("Test", "TestLand", "US"))
        val changedIdentity = TestIdentity(CordaX500Name("Test", "TestLand", "US"))

        val stamp = BasketOfApples("test description", initIdentity.party, 10)
        assertEquals(stamp.owner, initIdentity.party)

        val stampAfter = stamp.changeOwner(changedIdentity.party)
        assertEquals(stamp.owner, initIdentity.party)

        assertEquals(stampAfter.owner, changedIdentity.party)
        assertEquals(stampAfter.farm, initIdentity.party)
        assertEquals(stampAfter.weight, 10)
        assertEquals(stampAfter.description, "test description")

    }
}
