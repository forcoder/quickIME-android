# 如果需要生成签名密钥，运行以下命令：
#
# 1. 生成 keystore（在本地 Android 项目目录）
#    keytool -genkeypair -v -storetype PKCS12 -keystore release-keystore.jks \
#    -alias quickime -keyalg RSA -keysize 2048 -validity 10000 \
#    -storepass <password> -keypass <password>
#
# 2. 将 keystore 转为 base64（上传到 GitHub Secrets）
#    base64 -i release-keystore.jks -o keystore.b64
#
# 3. 上传到 GitHub Secrets:
#    - KEYSTORE_BASE64: cat keystore.b64 的内容
#    - KEYSTORE_PASSWORD: <上面设置的 storepass>
#    - KEY_ALIAS: quickime
#    - KEY_PASSWORD: <上面设置的 keypass>

# Debug 构建不需要签名，可以直接安装测试
