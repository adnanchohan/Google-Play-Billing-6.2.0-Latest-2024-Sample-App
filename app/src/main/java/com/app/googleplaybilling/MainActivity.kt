package com.app.googleplaybilling

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType


class MainActivity : AppCompatActivity() {

    private var billingClientLocal: BillingClient? = null
    private var purchase_btn: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        purchase_btn = findViewById(R.id.purchase_btn)
        purchase_btn!!.setOnClickListener {
            startActivity(Intent(this, SubscriptionActivity::class.java))
        }

        //querying to check if user has full version or not
        queryPurchase()
    }

    fun queryPurchase() {
        val googleBillingClient = GoogleBillingClient()
        googleBillingClient.startBillingClientConnection(
            this,
            object : BillingClientSetupListener {
                override fun onBillingClientQueryComplete(
                    billingClient: BillingClient,
                    isPurchased: Boolean,
                    productType: String
                ) {
                    billingClientLocal = billingClient
                    if (productType == ProductType.SUBS) {
                        if (isPurchased) {
                            // Do Upgrade to full version
                        } else {
                            // Do Downgrade
                        }
                    } else if (isPurchased && productType == ProductType.INAPP) {
                        // Do Upgrade to full version
                    }
                }
            })
    }

}