package com.github.hudifu316.applestamp.states

import com.github.hudifu316.applestamp.contracts.BasketOfApplesContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

@BelongsToContract(BasketOfApplesContract::class)
class BasketOfApples(
    val description: String,
    val farm: Party,
    val owner: Party,
    val weight: Int,
    override val participants: List<AbstractParty> = mutableListOf(farm, owner)
) : ContractState {
    fun changeOwner(buyer: Party): BasketOfApples {
        val newOwnerState = BasketOfApples(this.description, this.farm, buyer, this.weight);
        return newOwnerState
    }

    constructor(description: String, farm: Party, weight: Int) : this(
        description,
        farm,
        farm,
        weight,
        mutableListOf(farm)
    )
}
