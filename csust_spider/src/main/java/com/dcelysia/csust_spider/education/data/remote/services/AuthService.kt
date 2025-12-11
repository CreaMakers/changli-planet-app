package com.dcelysia.csust_spider.education.data.remote.services

import android.util.Log
import com.dcelysia.csust_spider.core.RetrofitUtils
import com.dcelysia.csust_spider.education.data.remote.api.EduEhallApi
import com.dcelysia.csust_spider.education.data.remote.api.EduLoginApi
import com.dcelysia.csust_spider.education.data.remote.error.EduHelperError
import com.dcelysia.csust_spider.mooc.data.remote.api.SSOAuthApi
import com.dcelysia.csust_spider.mooc.data.remote.api.SSOEhallApi
import retrofit2.Response

object AuthService {

    private val Loginapi by lazy { RetrofitUtils.instanceEduLogin.create(EduLoginApi::class.java) }
    private val ssoApi by lazy { RetrofitUtils.instanceSSOAuth.create(SSOAuthApi::class.java) }
//    private val Ehallapi by lazy { RetrofitUtils.instanceEduLogin.create(EduEhallApi::class.java) }
    private val TAG = "AuthService"

    suspend fun CheckLoginStates(): Boolean{
        val reponse =Loginapi.checkLoginStates()
        if (isLogin(reponse)){
            throw EduHelperError.NotLoggedIn("登录失效，请重新登录")
        }else{
            return true
        }
    }

    suspend fun Login(account: String,password: String): Boolean{
        Loginapi.login()
        val response = ssoApi.loginToEducation()
        val html = response.body().toString()
        Log.d(TAG,html)
        if (html.contains("请输入账号")){
            return false
        }
        return true
    }

    private fun isLogin(reponse: Response<String>): Boolean {
        return reponse.body().toString().contains("请输入账号")
    }

//    private suspend fun getEhall(account: String,password: String) : String {
//
//        val body = Ehallapi.getEhall().body().toString()
//        if (body.isEmpty()) {
//            throw EduHelperError.LoginFailed("验证码响应为空")
//        }
//        val params = body.split("#")
//        if (params.size != 2) throw EduHelperError.LoginFailed("验证码响应格式无效")
//
//        var sourceCode = params[0]
//        val sequenceHint = params[1]
//        val code = "$account%%%$password"
//        val encoded = StringBuilder()
//
//        for (i in code.indices) {
//            if (i < 20) {
//                val charFromCode = code[i]
//                val hintChar = sequenceHint[i]
//                val n = hintChar.digitToIntOrNull()
//                    ?: throw EduHelperError.LoginFailed("序列提示中字符无效")
//                val extractedChars = sourceCode.take(n)
//                encoded.append(charFromCode).append(extractedChars)
//                sourceCode = sourceCode.drop(n)
//            } else {
//                encoded.append(code.substring(i))
//                break
//            }
//        }
//        return encoded.toString()
//    }

    suspend fun LoginOut(): Boolean{
        val timeStamps = System.currentTimeMillis()
        val reponse = Loginapi.loginout(timeStamps)
        if (!reponse.isSuccessful){
            throw EduHelperError.NotLoggedIn("登出失败,${reponse.body()}")
        }else{
            RetrofitUtils.ClearClient("EducationClient")
            return true
        }
    }


}