package com.app.googleplaybilling

import android.content.Context

interface BillingClientService {
    fun startBillingClientConnection(context: Context, billingClientSetupListener: BillingClientSetupListener)
}