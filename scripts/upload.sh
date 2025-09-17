#!/usr/bin/env bash
#
# =================================================================================
# Shell脚本：使用华为云OBS的V1签名授权上传文件
#
# 描述:
#   此脚本通过计算HMAC-SHA1签名来授权一个PUT请求，并将本地文件上传到华为云OBS。
#   所有配置参数均从环境变量中读取，非常适合在CI/CD环境（如GitHub Actions）中使用。
#
# 依赖工具:
#   - curl
#   - openssl
#   - date
#
# 需要设置的环境变量:
#   - HUAWEICLOUD_SDK_AK: 您的Access Key ID
#   - HUAWEICLOUD_SDK_SK: 您的Secret Access Key
#   - OBS_BUCKET_NAME:    目标桶名称
#   - OBS_OBJECT_KEY:     在桶中存储的对象键 (例如: 'builds/app.apk')
#   - OBS_FILE_PATH:      要上传的本地文件的路径 (例如: './app/build/outputs/apk/release/app-release.apk')
#   - OBS_ENDPOINT:       OBS终端节点区域 (例如: 'cn-south-1')
# =================================================================================

# --- 脚本设置 ---
# -e: 如果任何命令失败，立即退出脚本
# -u: 如果使用未定义的变量，则报错
# -o pipefail: 如果管道中的任何命令失败，则整个管道的返回值为失败
set -euo pipefail

# --- 1. 检查环境变量和文件 ---
echo "--- 检查环境变量和文件 ---"

# 检查必要的环境变量是否已设置
if [ -z "${HUAWEICLOUD_SDK_AK}" ]; then
  echo "❌ 错误: 环境变量 'HUAWEICLOUD_SDK_AK' 未设置。" >&2
  exit 1
fi
if [ -z "${HUAWEICLOUD_SDK_SK}" ]; then
  echo "❌ 错误: 环境变量 'HUAWEICLOUD_SDK_SK' 未设置。" >&2
  exit 1
fi
if [ -z "${OBS_BUCKET_NAME}" ]; then
  echo "❌ 错误: 环境变量 'OBS_BUCKET_NAME' 未设置。" >&2
  exit 1
fi
if [ -z "${OBS_OBJECT_KEY}" ]; then
  echo "❌ 错误: 环境变量 'OBS_OBJECT_KEY' 未设置。" >&2
  exit 1
fi
if [ -z "${OBS_FILE_PATH}" ]; then
  echo "❌ 错误: 环境变量 'OBS_FILE_PATH' 未设置。" >&2
  exit 1
fi
if [ -z "${OBS_ENDPOINT}" ]; then
  echo "❌ 错误: 环境变量 'OBS_ENDPOINT' 未设置。" >&2
  exit 1
fi

# 检查本地文件是否存在
if [ ! -f "${OBS_FILE_PATH}" ]; then
  echo "❌ 错误: 文件不存在于路径: ${OBS_FILE_PATH}" >&2
  exit 1
fi

echo "✅ 所有环境变量和文件均已找到。"
echo ""
echo "--- 上传详情 ---"
echo "  - 桶名称:   ${OBS_BUCKET_NAME}"
echo "  - 对象键:   ${OBS_OBJECT_KEY}"
echo "  - 本地文件: ${OBS_FILE_PATH}"
echo "  - Endpoint: ${OBS_ENDPOINT}"
echo "--------------------"
echo ""

# --- 2. 准备签名所需的数据 ---

# 获取GMT格式的日期 (例如: Mon, 09 Nov 2025 12:35:30 GMT)
# 这是OBS V1签名授权的强制要求
request_time=$(date -u +"%a, %d %b %Y %H:%M:%S GMT")

# 定义请求类型和其他头部 (在PUT请求中，Content-MD5和Content-Type是可选的)
http_verb="PUT"
content_md5=""
content_type=""
canonicalized_resource="/${OBS_BUCKET_NAME}/${OBS_OBJECT_KEY}"

# 构建规范化的字符串 (StringToSign)
# 格式: HTTP-Verb\nContent-MD5\nContent-Type\nDate\nCanonicalizedResource
string_to_sign="${http_verb}\n${content_md5}\n${content_type}\n${request_time}\n${canonicalized_resource}"

echo "--- 生成签名 ---"
echo "StringToSign (用于签名的规范字符串):"
echo -e "------------\n${string_to_sign}\n------------" # 使用-e来解析换行符

# --- 3. 生成签名 ---
# 使用openssl进行HMAC-SHA1加密，然后进行Base64编码
# -n 在echo中阻止添加尾随的换行符，这对于正确的签名至关重要
signature=$(echo -en "${string_to_sign}" | openssl dgst -sha1 -hmac "${HUAWEICLOUD_SDK_SK}" -binary | base64)

echo "✅ 签名生成成功。"
echo ""

# --- 4. 执行上传 ---

# 构建请求URL
url="http://${OBS_BUCKET_NAME}.obs.${OBS_ENDPOINT}.myhuaweicloud.com/${OBS_OBJECT_KEY}"

echo "--- 开始上传 ---"
echo "正在向以下URL发送PUT请求:"
echo "${url}"
echo ""

# 使用curl执行上传
# -X PUT: 指定请求方法为PUT
# -T "${OBS_FILE_PATH}": 指定要上传的本地文件，curl会自动处理文件内容作为请求体
# -H "Date: ...": 添加日期头
# -H "Authorization: ...": 添加授权头
# --fail: 如果HTTP状态码是4xx或5xx，则使curl以错误码退出
# -v: (可选) 添加此项可以打印详细的请求和响应日志，用于调试
response_code=$(curl -s -o /dev/null -w "%{http_code}" \
  -X PUT \
  -T "${OBS_FILE_PATH}" \
  -H "Date: ${request_time}" \
  -H "Authorization: OBS ${HUAWEICLOUD_SDK_AK}:${signature}" \
  "${url}")

# --- 5. 检查结果 ---
echo ""
echo "--- 上传结果 ---"
if [ "${response_code}" -eq 200 ]; then
  echo "✅ 文件上传成功! (状态码: ${response_code})"
  exit 0
else
  echo "❌ 文件上传失败! (状态码: ${response_code})" >&2
  echo "请检查您的AK/SK、桶名称、Endpoint以及桶的权限策略。" >&2
  exit 1
fi
