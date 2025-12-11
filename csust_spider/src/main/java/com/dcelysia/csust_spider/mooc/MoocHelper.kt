//package com.dcelysia.csust_spider.mooc
//
//import android.app.Application
//import android.os.Message
//import android.util.Log
//import android.util.Log.e
//import androidx.lifecycle.AndroidViewModel
//import com.example.changli_planet_app.feature.mooc.data.remote.dto.PendingAssignmentCourse
//import com.example.changli_planet_app.feature.mooc.data.remote.repository.MoocRepository
//import com.example.csustdataget.core.Resource
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.filter
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.flow.launchIn
//import kotlinx.coroutines.flow.onEach
//import kotlinx.coroutines.launch
//
//
//
//object MoocHelper {
//
//    private const val TAG = "MoocViewModel"
//
//
//    private val _isSuccessLogin = MutableStateFlow<Resource<Boolean>>(Resource.Loading())
//    val isSuccessLogin = _isSuccessLogin.asStateFlow()
//
//    private val _pendingCourse =
//        MutableStateFlow<Resource<List<PendingAssignmentCourse>>>(Resource.Loading())
//    val pendingCourse = _pendingCourse.asStateFlow()
//
//    private val repository by lazy { MoocRepository.instance }
//
//    private fun loginWithFlow(account: String, password: String) {
//        val loginResult = repository.login(account, password)
//        loginResult.onEach {
//            _isSuccessLogin.value = it
//        }.launchIn(CoroutineScope(Dispatchers.IO))
//    }
//
//    private fun loginAndFetchCourses(account: String, password: String) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                if (account.isEmpty() || password.isEmpty()) {
//                    _pendingCourse.value =
//                        Resource.Error("未绑定学校账号密码，无法查询")
//                }
//                _isSuccessLogin.value = Resource.Loading()
//                _pendingCourse.value = Resource.Loading()
//                val loginResult = repository.login(account, password)
//                    .filter { it !is Resource.Loading }
//                    .first()
//                _isSuccessLogin.value = loginResult
//
//                if (loginResult is Resource.Success && loginResult.data) {
//                    val courseResult = repository.getCourseNamesWithPendingHomeworks()
//                        .filter { it !is Resource.Loading }
//                        .first()
//                    _pendingCourse.value = courseResult
//                } else {
//                    _pendingCourse.value = Resource.Error((loginResult as Resource.Error).msg)
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Login and fetch courses failed", e)
//                _isSuccessLogin.value =
//                    Resource.Error(e.message ?: "未知错误")
//                _pendingCourse.value =
//                    Resource.Error(e.message ?: "未知错误")
//            }
//        }
//    }
//
//    suspend fun login(account: String, password: String): Resource<Boolean> {
//        return try {
//            val result = repository.login(account, password)
//                .filter { it !is Resource.Loading }
//                .first()
//            result
//        } catch (e: Exception) {
//            Resource.Error<Boolean>(e.message ?: "未知错误")
//        }
//    }
//
//    suspend fun loginAndFetch(account: String, password: String): Resource<List<PendingAssignmentCourse>> {
//        return try {
//            if (account.isEmpty() || password.isEmpty()){
//                Resource.Error<List<PendingAssignmentCourse>>("未绑定学校账号密码，请登录网页先~")
//            }
//            val loginResult = login(account, password)
//            if (loginResult is Resource.Success && loginResult.data) {
//                val courseResult = repository.getCourseNamesWithPendingHomeworks()
//                    .filter { it !is Resource.Loading }
//                    .first()
//                courseResult
//            } else {
//                Resource.Error<List<PendingAssignmentCourse>>(
//                    (loginResult as? Resource.Error)?.msg ?: "登录失败")
//
//            }
//        } catch (e: Exception) {
//            Resource.Error<List<PendingAssignmentCourse>>(e.message ?: "未知错误")
//        }
//    }
//
//    suspend fun LoginAndReturnMooc(account: String,password: String) : Any? {
//        return CoroutineScope(Dispatchers.IO).launch {
//            loginAndFetchCourses("account","password")
//            pendingCourse.collect { value ->
//                when(value){
//                    is Resource.Success ->{
//                        value.data
//                    }
//
//                    is Resource.Error ->{
//                        Log.d(TAG, value.msg)
//                    }
//
//                    else -> {}
//                }
//
//            }
//        } as List<PendingAssignmentCourse>
//
//    }
//
//}
//
//
//
//
//
