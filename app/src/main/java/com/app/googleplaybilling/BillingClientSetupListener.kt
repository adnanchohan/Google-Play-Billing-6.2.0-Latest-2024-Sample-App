package com.app.googleplaybilling

import com.android.billingclient.api.BillingClient

interface BillingClientSetupListener {
        fun onBillingClientQueryComplete(billingClient: BillingClient, isPurchased: Boolean, productType: String)
}