# 中国 IPTV · 武汉联通版

这个目录独立管理中国国内 IPTV 频道，与马来西亚 Unifi 配置互不干扰。

## 直播源远程订阅

- TXT: https://raw.githubusercontent.com/wang0911-sys/TVBoxOS/main/china-iptv/live.txt
- M3U: https://raw.githubusercontent.com/wang0911-sys/TVBoxOS/main/china-iptv/live.m3u

根据用户提供的《湖北联通.txt》整理为 60 条 RTSP 单播候选线路。分为武汉本地、央视、湖北、卫视、教育、特色，共 6 组。含 CCTV-5 与 CCTV-5+。

**重要：原始列表中的认证参数带有 2021—2022 年日期。公开文件已删除认证参数，仅保留 RTSP 基础路径。因此直播是否可播放尚未验证；联通 IPTV 专网、有效授权等限制依然存在。** 远程订阅更新只更新频道地址，不会绕过联通的认证与网络隔离。

## 中国 IPTV 安卓电视专用 APK

- 应用显示名称：中国 IPTV
- 默认订阅：本目录 `live.txt`
- 代码补丁：[`android/patch_china.py`](./android/patch_china.py)
- 构建流程：[`.github/workflows/build-china-iptv.yml`](../.github/workflows/build-china-iptv.yml)
- APK 下载：[GitHub Releases](https://github.com/wang0911-sys/TVBoxOS/releases/tag/china-iptv-wuhan-v1)（只在编译成功后才存在）

基于 [sakana164/mytv_android](https://github.com/sakana164/mytv_android) 历史 GPL-3.0 源码（提交 `fc78a7f392f1552826c0bb2b06c8a817a4dd16ef`）二次配置，非联通或原软件开发者官方产品。二次分享 APK 时应遵守开源许可证。

## 远程维护

在 GitHub 上直接编辑 `china-iptv/live.txt`，机顶盒的预设列表可通过手动刷新或缓存到期重新读取更新内容，**不需重新编译 APK**。M3U 和 TXT 仍是独立文件，更新一个不会自动同步另一个；本应用预设使用 TXT。

更新 Android 代码才会触发 GitHub Actions 自动编译；修改直播源不会触发 APK 重编译。

## 和马来西亚版分开

- 中国：此目录 `TVBoxOS/china-iptv/`
- 马来西亚：独立仓库 [unifi-tvbox](https://github.com/wang0911-sys/unifi-tvbox)

应用采用临时签名的构建方式。未来若重新编译安装包，可能需要先卸载旧 APK；若需无缝升级，应在仓库 Secrets 中设置稳定签名密钥而非上传到公开仓库。
