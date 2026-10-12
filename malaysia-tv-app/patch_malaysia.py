#!/usr/bin/env python3
"""Customize the historical GPL-3.0 TV Live fork for Malaysia Unifi IPTV.

Usage: cd mytv_source && python3 ../malaysia-tv-app/patch_malaysia.py
"""
from pathlib import Path

ROOT = Path.cwd()
URL = "https://raw.githubusercontent.com/wang0911-sys/unifi-tvbox/main/live.txt"

def replace_one(path, old, new):
    p = ROOT / path
    contents = p.read_text(encoding="utf-8")
    if contents.count(old) != 1:
        raise RuntimeError(f"Expected one match in {path!s}, found {contents.count(old)} for {old[:70]!r}")
    p.write_text(contents.replace(old, new), encoding="utf-8")
    print("Patched", path)

constants_path = ROOT / "core/data/src/main/java/top/yogiczy/mytv/core/data/utils/Constants.kt"
constants = constants_path.read_text(encoding="utf-8")
start = constants.index("    val IPTV_SOURCE_LIST = IptvSourceList(")
stop = constants.index("    /*  历史列表大小 */", start)
new_source = """    val IPTV_SOURCE_LIST = IptvSourceList(
        listOf(
            IptvSource(
                name = "Malaysia Unifi IPTV (Community)",
                url = "https://raw.githubusercontent.com/wang0911-sys/unifi-tvbox/main/live.txt",
            )
        )
    )
"""
constants = constants[:start] + new_source + constants[stop:]
constants = constants.replace('const val APP_TITLE = "电视直播"', 'const val APP_TITLE = "Malaysia Live TV"')
# Faster recheck of the remote playlist; use previous downloaded cache when offline.
constants = constants.replace(
    "const val IPTV_SOURCE_CACHE_TIME = 1000 * 60 * 60L // 24小时",
    "const val IPTV_SOURCE_CACHE_TIME = 1000 * 60 * 10L // 10 minutes",
)
assert URL in constants
constants_path.write_text(constants, encoding="utf-8")
print("Patched", constants_path)

replace_one(
    "tv/src/main/res/values/strings.xml",
    '<string name="app_name">电视直播</string>',
    '<string name="app_name">Malaysia Live TV</string>',
)
replace_one(
    "tv/build.gradle.kts",
    'applicationId = "com.github.mytv.android"',
    'applicationId = "com.wang0911.malaysialivetv"',
)
# Do not alter package namespaces: app ID can differ from code namespace.
print("Configured Malaysia-only IPTV source", URL)
