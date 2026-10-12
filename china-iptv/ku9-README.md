# 酷9 Lite · 中国 IPTV 远程订阅

## 安装版本
选择明确标注 lite 的酷9版本，如 KU9_1.7.7.1 Lite。该软件说明称 Lite 最低兼容 Android 4.3，而普通版最低 Android 5。
酷9播放器未找到公开完整源代码，以下仅提供官方文档所支持的独立订阅配置，**不是修改后的酷9 APK**。

## 单独订阅（推荐）

TXT：
https://raw.githubusercontent.com/wang0911-sys/TVBoxOS/main/china-iptv/live.txt

M3U：
https://raw.githubusercontent.com/wang0911-sys/TVBoxOS/main/china-iptv/live.m3u

## 酷9 JSON 订阅接口（集中分发）

https://raw.githubusercontent.com/wang0911-sys/TVBoxOS/main/china-iptv/ku9-subscribe.json

在酷9 Lite 中使用遥控器菜单 → 设置 → 列表配置 → 列表订阅，输入上面的 TXT 地址即可；也可试用 JSON 订阅接口。保存地址后，记得 **选中该订阅** 并刷新。
官方说明采用 `lives` → `name` + `url` 数组作为订阅接口格式。不同 Lite 版本界面可能有差异。

## 远程更新

以后只编辑 `china-iptv/live.txt`，电视端重新刷新该订阅，就会更新频道；无需重新安装酷9 Lite。若直接订阅 M3U，则更新 `live.m3u`。

当前 TXT/M3U 各有60条湖北联通 RTSP 旧版候选地址。原始 `accountinfo` 等查询参数来自历史文件，可能过期。服务器可能要求联通专网/VLAN及有效授权。GitHub 不等于直播服务器。

## 网络限制

中国内地部分网络无法直接访问 raw.githubusercontent.com；若酷9提示列表下载失败，应先检查盒子浏览器能否打开 GitHub 原始订阅地址，而非误判播放器不支持。

## 安装说明与第三方来源

https://www.wmdz.com/shell.html
https://github.com/fuxinyi0505/Ku9-IPTV-source/blob/main/%E9%85%B79%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E.txt

不要从不可信 APK 下载站安装不明改版；优先核对软件最低 Android 版本。
