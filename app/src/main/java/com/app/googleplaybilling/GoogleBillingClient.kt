package com.android.inputmethod.billing

import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchasesParams
import com.app.googleplaybilling.Constant
import java.util.concurrent.Executors


class GoogleBillingClient : BillingClientService {

    val TAG = "GoogleBillingClient"
    override fun startBillingClientConnection(context: Context, billingClientSetupListener: BillingClientSetupListener) {

        val purchasesUpdatedListener = PurchasesUpdatedListener { _, _ ->}
        val billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                TODO("Not yet implemented")
            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                val executorServiceSubs = Executors.newSingleThreadExecutor()
                executorServiceSubs.execute {
                    val params = QueryPurchasesParams.newBuilder()
                        .setProductType(ProductType.SUBS)
                        .build()
                    billingClient.queryPurchasesAsync(params) { _, purchasesList ->

                        if (purchasesList.isNotEmpty()) {
                            for (purchase in purchasesList) {
                                if (purchase != null && purchase.isAcknowledged) {
                                    for (i in purchase.products.indices) {
                                        Log.d(TAG, "onBillingSetupFinished: productId from Play Console: " + purchase.products[i].toString())
                                        if (purchase.products[i].toString() == Constant.MONTHLY_PRODUCT_ID ||
                                            purchase.products[i].toString() == Constant.YEARLY_PRODUCT_ID) {
                                            // UpgradeNow
                                            billingClientSetupListener.onBillingClientQueryComplete(billingClient,true, ProductType.SUBS)
                                        }
                                    }
                                }
                            }
                        } else {
                            // Downgrade
                            billingClientSetupListener.onBillingClientQueryComplete(billingClient,false, ProductType.SUBS)
                        }
                    }
                }

                val executorServiceInApp = Executors.newSingleThreadExecutor()
                executorServiceInApp.execute {
                    val params = QueryPurchasesParams.newBuilder()
                        .setProductType(ProductType.INAPP)
                        .build()
                    billingClient.queryPurchasesAsync(params) { _, purchasesList ->
                        if (purchasesList.isNotEmpty()) {
                            for (purchase in purchasesList) {
                                if (purchase != null && purchase.isAcknowledged) {
                                    for (i in purchase.products.indices) {
                                        Log.d(TAG, "onBillingSetupFinished: productId from Play Console: " + purchase.products[i].toString())
                                        if (purchase.products[i].toString() == Constant.LIFETIME_PRODUCT_ID) {
                                            // UpgradeNow
                                            billingClientSetupListener.onBillingClientQueryComplete(billingClient,true, ProductType.INAPP)
                                        }
                                    }
                                }
                            }
                        } else {
                            // Downgrade
                            billingClientSetupListener.onBillingClientQueryComplete(billingClient,false, ProductType.INAPP)
                        }
                    }
                }
            }
        })
    }
}