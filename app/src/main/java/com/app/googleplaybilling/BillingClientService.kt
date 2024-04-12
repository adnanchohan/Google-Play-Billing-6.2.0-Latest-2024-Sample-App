package com.android.inputmethod.billing

import android.content.Context

interface BillingClientService {
    fun startBillingClientConnection(context: Context, billingClientSetupListener: BillingClientSetupListener)
}