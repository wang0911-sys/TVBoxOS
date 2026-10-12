# 中国 IPTV Android 4.4.2 独立直播 APK

**独立原创轻量 Android Java 程序**，不是酷9 Lite，也不是 TVBox 或天光云影衍生代码。只在此目录创建源码，**不修改 TVBoxOS 的 app 或其他 TVBox 文件**。

- 最低 Android 4.4 (API 19)，Android 4.4.2 可安装（仍须实机测试）
- 原生安卓 Java Activity / VideoView / MediaPlayer，不使用 Jetpack Compose/Media3
- 支持 TV 遥控器方向键、OK、返回键；菜单键打开节目单管理
- 首次安装内置当前中国 IPTV 列表，开机立即显示频道；联网时从 GitHub 拉取最新列表
- 同时解析 TXT 的 `分类,#genre#` 和 M3U 的 `#EXTINF` 格式
- 本地保存最近一次成功拉取的列表，启动后尝试自动更新，每十分钟检查一次
- 使用 Android 4.4 上 TLSv1.2 兼容连接 GitHub Raw
- 默认 TXT：
  https://raw.githubusercontent.com/wang0911-sys/TVBoxOS/main/china-iptv/live.txt
- 备用 M3U：
  https://raw.githubusercontent.com/wang0911-sys/TVBoxOS/main/china-iptv/live.m3u

## 使用
打开直接显示频道列表。按方向键选择频道，按 OK 播放。隐藏列表后方向键上下可切台；左右换台；菜单键可以刷新远程频道或切换 TXT/M3U；返回键隐藏列表。

## 重要技术限制

**不是酷9改版 APK。** 酷9完整源码未公开，本 APP 不使用酷9闭源代码。播放器是 Android 4.4 系统自带 `VideoView`/`MediaPlayer`，播放 RTSP 的能力取决于盒子固件、底层解码器以及具体 RTSP 服务器。无法保证播放你现在的 `rtsp://122.191.50.135` 旧授权地址。安装成功不等于 IPTV 源有效。

由于中国内地一些网络无法直接连接 `raw.githubusercontent.com`，APP 保留内置/本地缓存节目单供离线显示，但远程更新需要 GitHub Raw 可达。公开源含历史 `accountinfo` 参数，请勿在公开仓库放置有效私人凭据。

## 构建
参见独立的 GitHub Actions Workflow：`.github/workflows/build-china44-standalone.yml`，使用 Android SDK Build Tools 编译签名，验证 APK `minSdkVersion=19`、V1/V2 签名和文件对齐。首次生成的 APK 使用临时密钥，后续重新打包如要覆盖更新需要保留签名密钥。

