package com.app.googleplaybilling

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.app.googleplaybilling.Constant.Companion.LIFETIME_PRODUCT_ID
import com.app.googleplaybilling.Constant.Companion.MONTHLY_PRODUCT_ID
import com.app.googleplaybilling.Constant.Companion.YEARLY_PRODUCT_ID
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableList
import java.util.concurrent.Executors
import kotlin.math.abs

class SubscriptionActivity : AppCompatActivity(), OnClickListener {


    private var price_tv_yearly : TextView? = null
    private var price_tv_monthly : TextView? = null
    private var price_tv_full : TextView? = null

    private var trialTv: TextView? = null
    private var regularPriceTv:TextView? = null
    private var thanksTv:TextView? = null

    private var monthlyLayout: RelativeLayout? = null
    private var yearlyLayout:RelativeLayout? = null
    private var lifetimeLayout:RelativeLayout? = null

    private var selectedProductId:String? = null
    private var monthlySubscriptionPrice: String? = null
    private var yearlySubscriptionPrice: String? = null
    private var lifetimeProductPrice: String? = null
    private var monthlyPriceMacros: Long = 280000000
    private var yearlyPriceMacros: Long = 1100000000

    private var buyNowBtn : Button? = null
    private var subsName: String? = null
    private var isSuccess = false
    private var billingClient: BillingClient? = null
    private val TAG : String = "SubscriptionActivity"
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)

        initLayout()
        initBillingClient()
        getProductPrices()
        setYearlySaveText()
    }

    private fun initLayout(){
        trialTv = findViewById(R.id.trial_premium_tv)
        regularPriceTv = findViewById(R.id.regular_price_tv)

        price_tv_full = findViewById(R.id.lifetime_price)
        price_tv_yearly = findViewById(R.id.one_year_price)
        price_tv_monthly = findViewById(R.id.one_month_price)

        buyNowBtn = findViewById(R.id.btn_subscribe_now)
        thanksTv = findViewById(R.id.no_thanks_tv)
        monthlyLayout = findViewById(R.id.monthly_premium)
        yearlyLayout = findViewById(R.id.yearly_premium)
        lifetimeLayout = findViewById(R.id.lifetime_premium)

        buyNowBtn!!.setOnClickListener(this)
        thanksTv!!.setOnClickListener(this)
        monthlyLayout!!.setOnClickListener(this)
        yearlyLayout!!.setOnClickListener(this)
        lifetimeLayout!!.setOnClickListener(this)

        price_tv_monthly!!.text = resources.getString(R.string.loading_price)
        price_tv_yearly!!.text = resources.getString(R.string.loading_price)
        price_tv_full!!.text = resources.getString(R.string.loading_price)

        yearlyLayout()
        Handler(Looper.getMainLooper()).postDelayed({
            //Do something after 100ms
            val regularString = resources.getString(R.string.regular_price, yearlySubscriptionPrice, "year")
            regularPriceTv!!.text = regularString
        }, 1200)

    }

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->

            if(billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                purchases != null){
                for (purchase in purchases){
                    handlePurchase(purchase)
                }
            }
            else if (billingResult.responseCode ==
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                Toast.makeText(this, "Already Subscribed" ,Toast.LENGTH_SHORT).show()
                isSuccess = true
            }
            else if (billingResult.responseCode ==
                BillingClient.BillingResponseCode.USER_CANCELED) {
                Toast.makeText(this, "User Cancelled" ,Toast.LENGTH_SHORT).show()
            }
            else if (billingResult.responseCode ==
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
                Toast.makeText(this, "Billing Unavailable" ,Toast.LENGTH_SHORT).show()
            }
            else if (billingResult.responseCode ==
                BillingClient.BillingResponseCode.NETWORK_ERROR) {
                Toast.makeText(this, "Network Error" ,Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Error" + billingResult.debugMessage  ,Toast.LENGTH_SHORT).show()
            }
        }


    private fun setYearlySaveText() {
        val yearlySaveTv = findViewById<TextView>(R.id.save_yearly_tv)
        val monthWiseYearlyPrice: Long = monthlyPriceMacros * 12
        val savePercent: Double =
            (abs((yearlyPriceMacros - monthWiseYearlyPrice) / monthWiseYearlyPrice.toFloat()) * 100f).toDouble()
        yearlySaveTv.text = resources.getString(R.string.save_rs, "" + Math.round(savePercent))
    }

    private fun initBillingClient(){
        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
    }

    private fun getProductPrices(){
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Log.d(TAG, "onBillingSetupFinished: subscription")
                    val executeService = Executors.newSingleThreadExecutor()
                    executeService.execute {
                        val queryProductDetailsParams =
                            QueryProductDetailsParams.newBuilder()
                                .setProductList(
                                    ImmutableList.of(
                                        QueryProductDetailsParams.Product.newBuilder()
                                            .setProductId(Constant.MONTHLY_PRODUCT_ID)
                                            .setProductType(BillingClient.ProductType.SUBS)
                                            .build(),
                                        QueryProductDetailsParams.Product.newBuilder()
                                            .setProductId(Constant.YEARLY_PRODUCT_ID)
                                            .setProductType(BillingClient.ProductType.SUBS)
                                            .build()
                                    )).build()

                        billingClient!!.queryProductDetailsAsync(queryProductDetailsParams) {
                                billingResult,
                                productDetailsList ->
                            // check billingResult
                            // process returned productDetailsList
                            for (productDetails in productDetailsList) {

                                var productPrice: String? = null
                                var description: String? = null
                                var duration: String? = null
                                var freeTrialDays: String? = null
                                var pricingPhase: ProductDetails.PricingPhase? = null

                                subsName = productDetails.name

                                Log.d(TAG, "onBillingSetupFinished: subsName: $subsName" )

                                val subscriptionOfferDetails = productDetails.subscriptionOfferDetails?.get(0)

                                if (subscriptionOfferDetails != null) {
                                    pricingPhase = subscriptionOfferDetails.pricingPhases.pricingPhaseList[0]

                                    description = productDetails.description
                                    val formattedPrice = pricingPhase.formattedPrice
                                    val billingPeriod = pricingPhase.billingPeriod
                                    val recurrenceMode = pricingPhase.recurrenceMode

                                    val n: String = billingPeriod.substring(1,2)
                                    val dur: String = billingPeriod.substring(2,3)
                                    if(recurrenceMode == 2) {
                                        when(dur) {
                                            "M" -> duration = " For $n Month, "
                                            "Y" -> duration = " For $n Year, "
                                            "W" -> duration = " For $n Week, "
                                            "D" -> duration = " For $n Days, "
                                        }
                                    }

                                    freeTrialDays = " $n"

                                    productPrice = subscriptionOfferDetails.pricingPhases.pricingPhaseList
                                        .map { it.formattedPrice }
                                        .getOrElse(1) {
                                            subscriptionOfferDetails.pricingPhases.pricingPhaseList[0].formattedPrice
                                        } // Getting the formattedPrice at index 1, else get index 0 if found index 1 is out of bounds

                                }

                                if (productDetails.productId == MONTHLY_PRODUCT_ID) {
                                    Log.d(TAG, "onBillingSetupFinished: Product Name: $subsName" )
                                    Log.d(TAG, "onBillingSetupFinished: Description: $description" )
                                    Log.d(TAG, "onBillingSetupFinished: Free Trial Days: $freeTrialDays" )
                                    Log.d(TAG, "onBillingSetupFinished: Price: $productPrice")

                                    // UI Update
                                    price_tv_monthly!!.text = productPrice
                                    monthlySubscriptionPrice = productPrice
                                    monthlyPriceMacros = productDetails.subscriptionOfferDetails!![0].pricingPhases
                                        .pricingPhaseList
                                        .map { it.priceAmountMicros }
                                        .getOrElse(1) {
                                            productDetails.subscriptionOfferDetails!![0].pricingPhases.pricingPhaseList[0].priceAmountMicros
                                        } // Getting the priceAmountMicros at index 1, else get index 0 if found index 1 is out of bounds

                                    Log.d(TAG, "onBillingSetupFinished: monthlyPriceMacros: $monthlyPriceMacros" )

                                }

                                if (productDetails.productId == YEARLY_PRODUCT_ID) {
                                    Log.d(TAG, "onBillingSetupFinished: Product Name: $subsName" )
                                    Log.d(TAG, "onBillingSetupFinished: Description: $description" )
                                    Log.d(TAG, "onBillingSetupFinished: Free Trial Days: $freeTrialDays" )
                                    Log.d(TAG, "onBillingSetupFinished: Price: $productPrice")

                                    // UI Update
                                    price_tv_yearly!!.text = productPrice
                                    yearlySubscriptionPrice = productPrice
                                    yearlyPriceMacros = productDetails.subscriptionOfferDetails!![0].pricingPhases
                                        .pricingPhaseList
                                        .map { it.priceAmountMicros }
                                        .getOrElse(1) {
                                            productDetails.subscriptionOfferDetails!![0].pricingPhases.pricingPhaseList[0].priceAmountMicros
                                        } // Getting the priceAmountMicros at index 1, else get index 0 if found index 1 is out of bounds

                                    Log.d(TAG, "onBillingSetupFinished: yearlyPriceMacros: $yearlyPriceMacros" )

                                    trialTv!!.text = "$freeTrialDays days free trial"
                                }

                            }

                        }
                    }

                    val executeServiceLifetime = Executors.newSingleThreadExecutor()
                    executeServiceLifetime.execute {
                        val queryProductDetailsParams =
                            QueryProductDetailsParams.newBuilder()
                                .setProductList(ImmutableList.of(
                                    QueryProductDetailsParams.Product.newBuilder()
                                        .setProductId(LIFETIME_PRODUCT_ID)
                                        .setProductType(BillingClient.ProductType.INAPP)
                                        .build()))
                                .build()

                        billingClient!!.queryProductDetailsAsync(queryProductDetailsParams) {
                                billingResult,
                                productDetailsList ->
                            // check billingResult
                            // process returned productDetailsList

                            Log.d(TAG, "onBillingSetupFinished: One Time Purchase")
                            for (productDetails in productDetailsList) {
                                var productPrice: String? = null
                                var description: String? = null
                                subsName = productDetails.name
                                val oneTimeOfferDetails = productDetails.oneTimePurchaseOfferDetails

                                if (oneTimeOfferDetails != null) {
                                    description = productDetails.description
                                    productPrice = oneTimeOfferDetails.formattedPrice
                                }

                                Log.d(TAG, "onBillingSetupFinished: Product Name: $subsName" )
                                Log.d(TAG, "onBillingSetupFinished: Description: $description" )
                                Log.d(TAG, "onBillingSetupFinished: Price: $productPrice")

                                // UI Update
                                price_tv_full!!.text = productPrice

                            }


                        }
                    }
                }
            }
        })
    }

    private fun launchPurchaseFlow(productId: String, productType: String){
            billingClient!!.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {

                }

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if(billingResult.responseCode == BillingClient.BillingResponseCode.OK){
                        val productList = listOf(
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(productId)
                                .setProductType(productType)
                                .build()
                        )
                        val params = QueryProductDetailsParams.newBuilder()
                            .setProductList(productList)

                        billingClient!!.queryProductDetailsAsync(params.build()) {
                            _, productDetailsList ->
                            // For One-time product, "setOfferToken" method shouldn't be called.
                            // For subscriptions, to get an offer token, call ProductDetails.subscriptionOfferDetails()
                            // for a list of offers that are available to the user
                            for (productDetails in productDetailsList) {
                                if(productDetails.productType == BillingClient.ProductType.INAPP) {
                                    val productDetailsParamsList = listOf(
                                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .build())
                                    val billingFlowParams = BillingFlowParams.newBuilder()
                                        .setProductDetailsParamsList(productDetailsParamsList)
                                        .build()
                                    billingClient!!.launchBillingFlow(this@SubscriptionActivity, billingFlowParams)
                                }
                                else {
                                    val offerToken = productDetails.subscriptionOfferDetails?.get(0)?.offerToken
                                    val productDetailsParamsList = listOf(
                                        offerToken?.let {
                                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                                .setProductDetails(productDetails)
                                                .setOfferToken(it)
                                                .build()
                                        })
                                    val billingFlowParams = BillingFlowParams.newBuilder()
                                        .setProductDetailsParamsList(productDetailsParamsList)
                                        .build()
                                    billingClient!!.launchBillingFlow(this@SubscriptionActivity, billingFlowParams)
                                }
                            }
                        }
                    }
                }

            })
    }

    private fun verifyValidSignature(originalJson: String, signature: String): Boolean {
        val security = Security()
        try {
            return security.verifyPurchase(Constant.BASE64_PublicKey, originalJson, signature,this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private var acknowledgePurchaseResponseListener =
        AcknowledgePurchaseResponseListener{
            if (it.responseCode == BillingClient.BillingResponseCode.OK){
                isSuccess = true
            }
        }

    private fun handlePurchase(purchase: Purchase){
        if(purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!verifyValidSignature(purchase.originalJson, purchase.signature)) {
                Toast.makeText(this, "Invalid Purchase", Toast.LENGTH_SHORT).show()
                return
            }

            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient!!.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener)
                isSuccess = true

                finish()
            } else {
                Toast.makeText(this, "Already Subscribe", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (billingClient != null) {
            billingClient!!.endConnection()
        }
    }

    private fun continueWithAds(){
        finish()
    }

    override fun onClick(v: View?) {

        when (v!!.id) {
            R.id.monthly_premium -> monthlyLayout()
            R.id.yearly_premium -> yearlyLayout()
            R.id.lifetime_premium -> lifeTimeLayout()
            R.id.btn_subscribe_now -> subscribeProductNow()
            R.id.no_thanks_tv -> continueWithAds()
        }
    }

    private fun subscribeProductNow() {

        when(selectedProductId){
            LIFETIME_PRODUCT_ID -> launchPurchaseFlow(LIFETIME_PRODUCT_ID, BillingClient.ProductType.INAPP)
            MONTHLY_PRODUCT_ID -> launchPurchaseFlow(MONTHLY_PRODUCT_ID, BillingClient.ProductType.SUBS)
            YEARLY_PRODUCT_ID -> launchPurchaseFlow(YEARLY_PRODUCT_ID, BillingClient.ProductType.SUBS)
        }
    }

    private fun monthlyLayout() {
        monthlyLayout!!.isSelected = true
        yearlyLayout!!.isSelected = false
        lifetimeLayout!!.isSelected = false
        selectedProductId = MONTHLY_PRODUCT_ID
        buyNowBtn!!.text = resources.getString(R.string.subscribe_now)
        trialTv!!.visibility = View.VISIBLE
        regularPriceTv!!.visibility = View.VISIBLE
        val regularString = resources.getString(R.string.regular_price, monthlySubscriptionPrice, "month")
        regularPriceTv!!.text = regularString
    }

    private fun yearlyLayout() {
        monthlyLayout!!.isSelected = false
        yearlyLayout!!.isSelected = true
        lifetimeLayout!!.isSelected = false
        selectedProductId = YEARLY_PRODUCT_ID
        buyNowBtn!!.text = resources.getString(R.string.subscribe_now)
        trialTv!!.visibility = View.VISIBLE
        regularPriceTv!!.visibility = View.VISIBLE
        val regularString = resources.getString(R.string.regular_price, yearlySubscriptionPrice, "year")
        regularPriceTv!!.text = regularString
    }

    private fun lifeTimeLayout() {
        monthlyLayout!!.isSelected = false
        yearlyLayout!!.isSelected = false
        lifetimeLayout!!.isSelected = true
        selectedProductId = LIFETIME_PRODUCT_ID
        buyNowBtn!!.setText(R.string.purchase_now)
        trialTv!!.visibility = View.INVISIBLE
        regularPriceTv!!.visibility = View.INVISIBLE
    }

}
