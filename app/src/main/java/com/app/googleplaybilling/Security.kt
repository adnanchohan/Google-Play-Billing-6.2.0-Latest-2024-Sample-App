package com.android.inputmethod.billing


import android.content.Context
import android.text.TextUtils
import android.util.Base64
import java.io.IOException
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.Signature
import java.security.SignatureException
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec

class Security {

    private val KEY_FACTORY_ALGORITHM = "RSA"
    private val SIGNATURE_ALGORITHM = "SHA1withRSA"
    private val TAG = "Security"

    private var mContext: Context? = null

    @Throws(IOException::class)
    fun verifyPurchase(base64PublicKey: String?, signedData: String, signature: String?, context: Context) : Boolean {

        mContext = context

        if (TextUtils.isEmpty(signature) || TextUtils.isEmpty(signedData) || TextUtils.isEmpty(base64PublicKey)) {
                return false
        }

        val key = generatePublicKey(base64PublicKey)
        return verify(key, signedData, signature)


    }
    @Throws(IOException::class)
    private fun generatePublicKey(base64PublicKey: String?): PublicKey {

        return try {
            val decodeKey = Base64.decode(base64PublicKey, Base64.DEFAULT)
            val keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
            keyFactory.generatePublic(X509EncodedKeySpec(decodeKey))
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidKeySpecException) {
            throw IOException("Invalid Key Specs: $e")
        }
    }

    private fun verify (publicKey: PublicKey?, signedData: String, signature: String?) : Boolean {
        val signatureBytes: ByteArray = try {
            Base64.decode(signature, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {
            return false
        }
        try {
            val signatureAlgorithm = Signature.getInstance(SIGNATURE_ALGORITHM)
            signatureAlgorithm.initVerify(publicKey)
            signatureAlgorithm.update(signedData.toByteArray())
            return signatureAlgorithm.verify(signatureBytes)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidKeyException) {
            //Invalid Key Exception
        } catch (e: SignatureException) {
            //Invalid Signature Exception
        }
        return false
    }

//    private fun isNetworkConnected(context: Context) : Boolean {
//        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        var networkInfo : NetworkInfo? = null
//
//        try {
//            networkInfo = connectivityManager.activeNetworkInfo
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        if (networkInfo != null){
//            return true
//        }
//
//        return false
//    }

}