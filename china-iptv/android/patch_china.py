from pathlib import Path
root = Path.cwd()
constants = root / "core/data/src/main/java/top/yogiczy/mytv/core/data/utils/Constants.kt"
content = constants.read_text(encoding="utf-8")
start = content.index("    val IPTV_SOURCE_LIST = IptvSourceList(")
end = content.index("    /*  历史列表大小 */", start)
playlist = """    val IPTV_SOURCE_LIST = IptvSourceList(
        listOf(
            IptvSource(
                name = "武汉联通 IPTV",
                url = "https://raw.githubusercontent.com/wang0911-sys/TVBoxOS/main/china-iptv/live.txt",
            )
        )
    )
"""
content = content[:start] + playlist + content[end:]
content = content.replace('const val APP_TITLE = "电视直播"', 'const val APP_TITLE = "中国 IPTV"')
content = content.replace("const val IPTV_SOURCE_CACHE_TIME = 1000 * 60 * 60L // 24小时", "const val IPTV_SOURCE_CACHE_TIME = 1000 * 60 * 10L // 10 minutes")
constants.write_text(content, encoding="utf-8")
strings = root / "tv/src/main/res/values/strings.xml"
txt = strings.read_text(encoding="utf-8")
assert '<string name="app_name">电视直播</string>' in txt
strings.write_text(txt.replace('<string name="app_name">电视直播</string>', '<string name="app_name">中国 IPTV</string>'), encoding="utf-8")
gradle = root / "tv/build.gradle.kts"
txt = gradle.read_text(encoding="utf-8")
assert 'applicationId = "com.github.mytv.android"' in txt
gradle.write_text(txt.replace('applicationId = "com.github.mytv.android"', 'applicationId = "com.wang0911.chinaiptv"'), encoding="utf-8")
print("China IPTV customization applied")
