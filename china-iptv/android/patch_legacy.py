from pathlib import Path
root=Path.cwd()
p=root/"app/src/main/java/com/github/tvbox/osc/base/App.java"
s=p.read_text(encoding="utf-8")
before="https://raw.githubusercontent.com/wang0911-sys/unifi-tvbox/main/live.txt"
after="https://raw.githubusercontent.com/wang0911-sys/TVBoxOS/main/china-iptv/live.txt"
assert before in s
p.write_text(s.replace(before,after),encoding="utf-8")
p=root/"app/src/main/res/values/strings.xml"
s=p.read_text(encoding="utf-8"); assert "<string name=\"app_name\">TVBox</string>" in s
p.write_text(s.replace("<string name=\"app_name\">TVBox</string>","<string name=\"app_name\">中国IPTV 旧盒兼容版</string>"),encoding="utf-8")
p=root/"app/build.gradle"
s=p.read_text(encoding="utf-8")
assert "applicationId 'com.github.tvbox.osc.jun'" in s
p.write_text(s.replace("applicationId 'com.github.tvbox.osc.jun'","applicationId 'com.wang0911.chinaiptv.legacy'"),encoding="utf-8")
print("Applied China IPTV preset to legacy Android 4.4-capable TVBoxOS variant.")
