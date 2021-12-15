package com.github.hudifu316.applestamp.states

import net.corda.core.identity.Party
import org.junit.Test
import kotlin.test.assertEquals

class AppleStampStateTests {
    @Test
    fun hasFieldOfCorrectType() {
        // Does the field exist?
        AppleStamp::class.java.getDeclaredField("stampDesc")
        // Is the field of the correct type?
        assertEquals(AppleStamp::class.java.getDeclaredField("stampDesc").type, String::class.java)

        AppleStamp::class.java.getDeclaredField("issuer")
        assertEquals(AppleStamp::class.java.getDeclaredField("issuer").type, Party::class.java)

        AppleStamp::class.java.getDeclaredField("holder")
        assertEquals(AppleStamp::class.java.getDeclaredField("holder").type, Party::class.java)
    }
}
