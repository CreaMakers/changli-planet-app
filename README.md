# okHttpHelper和EventBusHelper使用
```
# OkHttpHelper

`OkHttpHelper` 是一个基于 `OkHttp` 和 `Gson` 封装的网络请求库，支持常见的 HTTP 请求类型，如 `GET`、`POST`、`PUT` 和 `DELETE` 请求，提供了简单易用的异步请求接口。

## 功能特性

- **GET 请求**：支持基础 GET 请求和带查询参数的 GET 请求。
- **POST 请求**：支持发送 JSON 格式的 POST 请求。
- **PUT 请求**：支持发送 JSON 格式的 PUT 请求。
- **DELETE 请求**：支持 DELETE 请求。
- **自动解析 JSON 响应**：基于 `Gson` 自动将 JSON 响应转换为指定的对象。

## 依赖

你需要在项目的 `build.gradle` 文件中添加以下依赖：

```gradle
dependencies {
    implementation 'com.squareup.okhttp3:okhttp:4.9.3'
    implementation 'com.google.code.gson:gson:2.8.8'
}
```

## 使用方法

### 1. GET 请求

发起一个基础的 GET 请求并获取 JSON 响应：

```kotlin
OkHttpHelper.get(
    url = "https://api.example.com/data",
    responseType = object : TypeToken<MyResponseType>() {}.type,
    onSuccess = { response：MyResponse ->
        // 成功处理响应
        println("Response: $response")
    },
    onFailure = { error ->
        // 处理错误
        println("Error: $error")
    }
)
```

#### 带查询参数的 GET 请求

```kotlin
OkHttpHelper.get(
    url = "https://api.example.com/data",
    pathParams = "123",
    queryParams = mapOf("key1" to "value1", "key2" to "value2"),
    responseType = object : TypeToken<MyResponseType>() {}.type,
    onSuccess = { response:MyResponse ->
        // 处理成功响应
        println("Response: $response")
    },
    onFailure = { error ->
        // 处理错误
        println("Error: $error")
    }
)
```

### 2. POST 请求

发起一个 POST 请求并发送 JSON 数据：

```kotlin
val requestBody = MyRequestObject()

OkHttpHelper.post(
    url = "https://api.example.com/submit",
    requestBodyObj = requestBody,
    responseType = object : TypeToken<MyResponseType>() {}.type,
    onSuccess = { response:MyResponse ->
        // 处理成功响应
        println("Response: $response")
    },
    onFailure = { error ->
        // 处理错误
        println("Error: $error")
    }
)
```

### 3. PUT 请求

发起一个 PUT 请求：

```kotlin
val requestBody = MyRequestObject()

OkHttpHelper.put(
    url = "https://api.example.com/update",
    requestBodyObj = requestBody,
    responseType = object : TypeToken<MyResponseType>() {}.type,
    onSuccess = { response:MyResponse ->
        // 处理成功响应
        println("Response: $response")
    },
    onFailure = { error ->
        // 处理错误
        println("Error: $error")
    }
)
```

### 4. DELETE 请求

发起一个 DELETE 请求：

```kotlin
OkHttpHelper.delete(
    url = "https://api.example.com/delete",
    responseType = object : TypeToken<MyResponseType>() {}.type,
    onSuccess = { response:MyResponse ->
        // 处理成功响应
        println("Response: $response")
    },
    onFailure = { error ->
        // 处理错误
        println("Error: $error")
    }
)
```
### 5. 上传文件
```
// 调用 OkHttpHelper 中的 uploadGzipFile 方法
    OkHttpHelper.uploadGzipFile(
        description = description,
        url = uploadUrl,
        file = file,
        responseType = responseType,
        onSuccess = { response ->
            // 上传成功的处理逻辑
            println("Upload Successful: $response")
        },
        onFailure = { error ->
            // 上传失败的处理逻辑
            println("Upload Failed: $error")
        }
    )
```
### 连接WebSocket
```
// 连接 WebSocket
    OkHttpHelper.connectWebSocket(
        url,
        onOpen = { webSocket ->
            println("WebSocket connected")
            // 可以通过 webSocket.send() 发送消息
            webSocket.send("Hello Server!")
        },
        onMessage = { message ->
            println("Received message: $message")
        },
        onFailure = { error ->
            println("WebSocket error: $error")
        },
        onClosing = { code, reason ->
            println("WebSocket is closing: Code: $code, Reason: $reason")
        },
        onClosed = { code, reason ->
            println("WebSocket closed: Code: $code, Reason: $reason")
        }
    )
```
## 常见问题

### 1. 如何处理 JSON 响应？

在请求中，`OkHttpHelper` 使用 `Gson` 自动将返回的 JSON 数据解析为指定的对象类型。你需要确保 `responseType` 的类型与服务器返回的 JSON 结构匹配。例如，使用 `TypeToken` 处理复杂的泛型对象：

```kotlin
val responseType = object : TypeToken<List<MyObject>>() {}.type
```

### 2. 如何传递查询参数？

在 GET 请求中，你可以通过 `Map<String, String>` 传递查询参数，库会自动将其附加到 URL 中：

```kotlin
OkHttpHelper.get(
    url = "https://api.example.com/data",
    queryParams = mapOf("key" to "value"),
    responseType = object : TypeToken<MyResponseType>() {}.type,
    onSuccess = { response -> 
        // 处理响应
        println("Response: $response")
    },
    onFailure = { error -> 
        // 处理错误
        println("Error: $error")
    }
)
```

### 3. 如何发送复杂对象作为请求体？

在 POST 或 PUT 请求中，你可以直接传递对象，`OkHttpHelper` 会使用 `Gson` 自动将对象序列化为 JSON 并发送：

```kotlin
val requestBody = MyRequestObject(/* 参数 */)

OkHttpHelper.post(
    url = "https://api.example.com/submit",
    requestBodyObj = requestBody,
    responseType = object : TypeToken<MyResponseType>() {}.type,
    onSuccess = { response ->
        // 处理响应
        println("Response: $response")
    },
    onFailure = { error ->
        // 处理错误
        println("Error: $error")
    }
)
```
# EventBusHelper
```
//这一步之后不必再使用register和unregister
//自动监听onStar和onStop进行注册和解注册
lifecycle.addObserver(EventBusLifecycleObserver(this))
        EventBusHelper.register(this)
        EventBusHelper.unregister(this)
        EventBusHelper.post("此处应是自定义的Event类")
        EventBusHelper.postSticky("此处应是自定义的Event类")
        EventBusHelper.removeStickyEvent()
```
