# 湖北联通 IPTV 远程节目单

- **M3U：** [live.m3u](./live.m3u)（支持 M3U 订阅的播放器）
- **TXT：** [live.txt](./live.txt)（支持“频道,地址”的国内直播播放器）

固定 M3U 订阅链接：

```text
https://raw.githubusercontent.com/wang0911-sys/TVBoxOS/main/iptv/live.m3u
```

固定 TXT 订阅链接：

```text
https://raw.githubusercontent.com/wang0911-sys/TVBoxOS/main/iptv/live.txt
```

## 维护

在 GitHub 编辑 `iptv/live.m3u` 或 `iptv/live.txt` 并提交即可更新远程文件；它们是两个独立文件，**更新其中一个并不会同步更改另一个**。用户需要在播放器里重新加载/刷新订阅，或等待播放器定时刷新。无需重装 APK。

## 注意

- 初始数据来自 [CHINA-IPTV 湖北联通单播列表](https://github.com/xisohi/CHINA-IPTV/blob/main/Unicast/hubei/unicom.txt)，整理日期 2026-10-12。
- 初始表共 62 条线路；保留原始公开 RTSP 地址及其查询参数。查询参数末尾带 20220106 时间，认证很可能已失效；**未实际测通**。
- HTTP/RTSP 是否可用取决于运营商 IPTV 专网、授权认证及当前线路状态。此播放列表只是地址索引，不提供代理、解锁或转发。
- 公开 GitHub 仓库中的频道链接任何人都可以查看；今后不要把自己的账号、密钥、Cookie、IPTV 认证 token 上传到此仓库。
- GitHub Raw 在部分中国大陆网络不稳定，可先在手机浏览器里验证链接能否下载，再设置到播放器。
