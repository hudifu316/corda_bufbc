package com.github.hudifu316.applestamp.states

import com.github.hudifu316.applestamp.contracts.AppleStampContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

// *********
// * State *
// *********
@BelongsToContract(AppleStampContract::class)
class AppleStamp(
    val stampDesc: String,
    val issuer: Party,
    val holder: Party,
    override val participants: List<AbstractParty> = mutableListOf(issuer, holder),
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState
