import com.example.changli_planet_app.Core.PlanetApplication
import okhttp3.Interceptor
import okhttp3.Response
class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 添加自定义的 header
        val newRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer ${PlanetApplication.accessToken}")
            .build()
        // 继续处理请求并返回响应
        return chain.proceed(newRequest)
    }
}
