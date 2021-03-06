package com.blockchain.koin.modules

import com.blockchain.network.modules.MoshiBuilderInterceptorList
import org.koin.dsl.module.applicationContext

val moshiModule = applicationContext {

    bean {
        MoshiBuilderInterceptorList(
            listOf(
                get("BigDecimal"),
                get("BigInteger"),
                get("nabu"),
                get("buySell"),
                get("kyc")
            )
        )
    }
}
