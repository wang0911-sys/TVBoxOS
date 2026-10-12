# 中国国内 IPTV · 湖北联通专用

这是中国国内网络的节目订阅文件，与马来西亚 Unifi 直播源分开维护。

## 固定订阅链接

M3U（推荐用于电视直播 TV、VLC 等）：

```text
https://raw.githubusercontent.com/wang0911-sys/TVBoxOS/main/china-iptv/live.m3u
```

TXT（推荐用于支持「频道,URL」及「分类,#genre#」格式的播放器）：

```text
https://raw.githubusercontent.com/wang0911-sys/TVBoxOS/main/china-iptv/live.txt
```

## 与马来西亚源的区别

- 中国国内（本目录）：`wang0911-sys/TVBoxOS/china-iptv/`
- 马来西亚 Unifi（独立仓库）：`wang0911-sys/unifi-tvbox/live.txt`

## 管理

编辑本目录中的直播列表文件并提交即可改变对应的订阅内容。M3U 与 TXT 是两个独立文件，编辑一种不会自动同步另一种。播放器需刷新订阅或等待其自动更新。

源列表来自公开的湖北联通单播列表，整理于 2026-10-12，保留原始 RTSP 地址和参数。原始认证参数包含历史日期且可能过期，所有线路**均未实测可播放**。不保证在普通宽带 Wi-Fi 上能访问运营商 IPTV 专网服务。

该仓库是公开的，请不要上传自己真实使用的 IPTV 账号、密码、设备识别信息或未过期的授权 token。
