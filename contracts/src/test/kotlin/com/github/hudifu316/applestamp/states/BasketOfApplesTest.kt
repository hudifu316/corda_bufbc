package com.github.hudifu316.applestamp.states

import net.corda.core.identity.Party
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

    }
}
