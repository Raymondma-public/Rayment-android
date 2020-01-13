package com.example.rayment.model

import java.time.LocalDateTime


class Account {
    var id: Int? = null
    var phone: String? = null
    var name: String? = null
    var email: String? = null
    var password: String? = null
//    var createDate: LocalDateTime? = null

    private var currencyAccountList: MutableList<CurrencyAccount>? = null

    fun getCurrencyAccountList(): List<CurrencyAccount>? {
        return currencyAccountList
    }

    fun setCurrencyAccountList(currencyAccountList: MutableList<CurrencyAccount>) {
        this.currencyAccountList = currencyAccountList
    }


}
