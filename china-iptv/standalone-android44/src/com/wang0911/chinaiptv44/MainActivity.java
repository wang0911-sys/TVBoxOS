package com.wang0911.chinaiptv44;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.Socket;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class MainActivity extends Activity {
    private static final String TXT_URL =
        "https://raw.githubusercontent.com/wang0911-sys/TVBoxOS/main/china-iptv/live.txt";
    private static final String M3U_URL =
        "https://raw.githubusercontent.com/wang0911-sys/TVBoxOS/main/china-iptv/live.m3u";
    private static final String PREFS = "china_iptv_settings";
    private static final long UPDATE_EVERY = 10L * 60L * 1000L;

    private final Handler handler = new Handler();
    private final ArrayList<Channel> channels = new ArrayList<Channel>();
    private VideoView video;
    private TextView titleView;
    private TextView statusView;
    private ListView channelList;
    private ArrayAdapter<String> channelAdapter;
    private int currentIndex = -1;
    private int cursorIndex = 0;
    private boolean showingList = true;
    private volatile boolean refreshing = false;
    private String sourceUrl = TXT_URL;

    private final Runnable periodicUpdate = new Runnable() {
        @Override public void run() {
            refreshFromInternet(false);
            handler.postDelayed(this, UPDATE_EVERY);
        }
    };

    @Override public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sourceUrl = getSharedPreferences(PREFS, MODE_PRIVATE).getString("source_url", TXT_URL);
        if (!TXT_URL.equals(sourceUrl) && !M3U_URL.equals(sourceUrl)) sourceUrl = TXT_URL;
        buildUserInterface();
        loadSavedOrBundledPlaylist();
        refreshFromInternet(true);
        handler.postDelayed(periodicUpdate, UPDATE_EVERY);
    }

    private int dp(int v) {
        float scale = getResources().getDisplayMetrics().density;
        return (int)(v * scale + 0.5f);
    }

    private void buildUserInterface() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        video = new VideoView(this);
        root.addView(video, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override public void onPrepared(MediaPlayer mp) {
                mp.setOnVideoSizeChangedListener(null);
                mp.setScreenOnWhilePlaying(true);
                video.start();
                if (currentIndex >= 0 && currentIndex < channels.size()) {
                    setStatus("正在播放：" + channels.get(currentIndex).name
                        + "  ·  菜单键刷新 / 确认键选台");
                }
            }
        });
        video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override public boolean onError(MediaPlayer mp, int what, int extra) {
                setStatus("此频道暂无法播放。旧版RTSP参数可能已过期，或需要联通IPTV专网。");
                return true;
            }
        });
        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override public void onCompletion(MediaPlayer mp) {
                setStatus("流已结束；可按上下键换台或按菜单键重新获取列表");
            }
        });

        titleView = new TextView(this);
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(23f);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        titleView.setPadding(dp(18), dp(12), dp(18), dp(12));
        titleView.setBackgroundColor(0xBF101924);
        FrameLayout.LayoutParams header = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(62), Gravity.TOP);
        root.addView(titleView, header);

        statusView = new TextView(this);
        statusView.setTextColor(0xFFF0F0F0);
        statusView.setTextSize(16f);
        statusView.setPadding(dp(14), dp(11), dp(14), dp(11));
        statusView.setBackgroundColor(0xB5101924);
        statusView.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams footer = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(54), Gravity.BOTTOM);
        root.addView(statusView, footer);

        channelList = new ListView(this);
        channelList.setBackgroundColor(0xE315202B);
        channelList.setCacheColorHint(Color.TRANSPARENT);
        channelList.setDividerHeight(dp(1));
        channelList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        channelList.setFocusable(false);
        channelList.setFocusableInTouchMode(false);
        channelAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                new ArrayList<String>()) {
            @Override public View getView(int position, View convertView, ViewGroup parent) {
                TextView row = (TextView) super.getView(position, convertView, parent);
                row.setTextColor(Color.WHITE);
                row.setTextSize(19f);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setMinHeight(dp(43));
                row.setPadding(dp(16), dp(8), dp(8), dp(8));
                row.setBackgroundColor(position == cursorIndex ? 0xFF246998 : 0x001F2431);
                return row;
            }
        };
        channelList.setAdapter(channelAdapter);
        channelList.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override public void onItemClick(android.widget.AdapterView<?> parent,
                    View view, int position, long id) {
                tuneTo(position);
                showList(false);
            }
        });
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        FrameLayout.LayoutParams listParams = new FrameLayout.LayoutParams(
            Math.min(Math.max(dp(290), screenWidth / 2), dp(540)),
            ViewGroup.LayoutParams.MATCH_PARENT,
            Gravity.RIGHT | Gravity.TOP);
        listParams.topMargin = dp(62);
        listParams.bottomMargin = dp(54);
        root.addView(channelList, listParams);

        setContentView(root);
        updateTitle();
        setStatus("加载内置节目单中……");
    }

    private void setStatus(String message) {
        if (statusView != null) statusView.setText(message);
    }

    private void updateTitle() {
        String channelName = currentIndex >= 0 && currentIndex < channels.size()
            ? channels.get(currentIndex).name : "中国 IPTV";
        titleView.setText("中国 IPTV 4.4  ·  " + channelName
            + "    (" + channels.size() + "台 · " + (M3U_URL.equals(sourceUrl) ? "M3U" : "TXT") + ")");
    }

    private void loadSavedOrBundledPlaylist() {
        String text = null;
        try {
            String filename = M3U_URL.equals(sourceUrl) ? "m3u_cache.txt" : "txt_cache.txt";
            text = readText(openFileInput(filename), 512 * 1024);
        } catch (Exception ignored) { }
        List<Channel> list = parsePlaylist(text);
        if (list.size() == 0) {
            try {
                text = readText(getAssets().open("fallback_live.txt"), 512 * 1024);
                list = parsePlaylist(text);
            } catch (Exception ignored) { }
        }
        if (!list.isEmpty()) {
            applyPlaylist(list, false);
            setStatus("已加载 " + list.size() + " 个候选频道，正在检查 GitHub 更新……");
        } else {
            setStatus("暂无内置节目单，正在联网获取频道……");
        }
    }

    private void applyPlaylist(List<Channel> incoming, boolean fromInternet) {
        String selected = currentIndex >= 0 && currentIndex < channels.size()
            ? channels.get(currentIndex).name : "";
        channels.clear();
        channels.addAll(incoming);
        int match = -1;
        for (int i = 0; i < channels.size(); i++) {
            if (channels.get(i).name.equals(selected)) { match = i; break; }
        }
        currentIndex = match;
        cursorIndex = match >= 0 ? match : 0;
        channelAdapter.clear();
        for (int i = 0; i < channels.size(); i++) {
            Channel ch = channels.get(i);
            channelAdapter.add(ch.group + "  ·  " + ch.name);
        }
        channelAdapter.notifyDataSetChanged();
        channelList.setSelection(cursorIndex);
        updateTitle();
        if (currentIndex < 0 && !channels.isEmpty()) tuneTo(0);
        if (fromInternet) setStatus("GitHub 已更新：" + channels.size()
            + " 个频道（当前源：" + (M3U_URL.equals(sourceUrl) ? "M3U" : "TXT") + "）");
    }

    private void tuneTo(int index) {
        if (index < 0 || index >= channels.size()) return;
        currentIndex = index;
        cursorIndex = index;
        channelList.setSelection(index);
        channelAdapter.notifyDataSetChanged();
        Channel item = channels.get(index);
        updateTitle();
        setStatus("连接 " + item.name + " ……");
        try {
            video.stopPlayback();
            video.setVideoURI(Uri.parse(item.url));
            video.start();
        } catch (Throwable error) {
            setStatus("启动播放失败：" + item.name + "。可尝试其他频道。");
        }
    }

    private void showList(boolean visible) {
        showingList = visible;
        channelList.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            cursorIndex = currentIndex >= 0 ? currentIndex : 0;
            channelList.setSelection(cursorIndex);
            channelAdapter.notifyDataSetChanged();
        }
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) return super.dispatchKeyEvent(event);
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_MENU:
                showOptions();
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
                if (showingList && !channels.isEmpty()) {
                    tuneTo(cursorIndex);
                    showList(false);
                } else {
                    showList(true);
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (showingList) moveCursor(-1);
                else tuneTo((currentIndex - 1 + channels.size()) % Math.max(1, channels.size()));
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (showingList) moveCursor(1);
                else tuneTo((currentIndex + 1) % Math.max(1, channels.size()));
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (!channels.isEmpty()) tuneTo((currentIndex - 1 + channels.size()) % channels.size());
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (!channels.isEmpty()) tuneTo((currentIndex + 1) % channels.size());
                return true;
            case KeyEvent.KEYCODE_BACK:
                if (showingList) { showList(false); return true; }
                showList(true);
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    private void moveCursor(int delta) {
        if (channels.isEmpty()) return;
        cursorIndex = (cursorIndex + delta + channels.size()) % channels.size();
        channelList.setSelection(cursorIndex);
        channelAdapter.notifyDataSetChanged();
    }

    private void showOptions() {
        final String[] actions = new String[] {
            "重新下载 GitHub 频道列表",
            "切换为 TXT 订阅（推荐）",
            "切换为 M3U 订阅",
            showingList ? "隐藏频道列表" : "显示频道列表",
            "关于 / 遥控器操作说明"
        };
        new AlertDialog.Builder(this).setTitle("中国 IPTV · 设置")
            .setItems(actions, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) refreshFromInternet(true);
                    if (which == 1) changeSource(TXT_URL);
                    if (which == 2) changeSource(M3U_URL);
                    if (which == 3) showList(!showingList);
                    if (which == 4) new AlertDialog.Builder(MainActivity.this)
                        .setTitle("中国 IPTV 4.4.2 独立版")
                        .setMessage("默认使用你的 GitHub 中国 IPTV 远程订阅。\n\n"
                            + "确定键：显示节目单 / 播放选中频道\n"
                            + "方向键上下：频道选择或切台\n"
                            + "方向键左右：换台\n"
                            + "菜单键：手动刷新或切换 TXT/M3U\n"
                            + "返回键：隐藏节目单\n\n"
                            + "使用 Android 系统原生解码器，RTSP 播放能力取决于机顶盒及联通网络。")
                        .setPositiveButton("确定", null).show();
                }
            }).setNegativeButton("取消", null).show();
    }

    private void changeSource(String nextUrl) {
        if (sourceUrl.equals(nextUrl)) {
            refreshFromInternet(true);
            return;
        }
        sourceUrl = nextUrl;
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString("source_url", sourceUrl).apply();
        loadSavedOrBundledPlaylist();
        refreshFromInternet(true);
    }

    private void refreshFromInternet(final boolean manual) {
        if (refreshing) {
            if (manual) setStatus("正在更新，请稍候……");
            return;
        }
        refreshing = true;
        final String requested = sourceUrl;
        if (manual) setStatus("正在下载远程频道：" + (M3U_URL.equals(requested) ? "M3U" : "TXT") + " ……");
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    String downloaded = downloadPlaylist(requested);
                    final List<Channel> list = parsePlaylist(downloaded);
                    if (list.size() == 0) throw new Exception("频道列表为空或格式不受支持");
                    final String fullText = downloaded;
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            if (requested.equals(sourceUrl)) {
                                try {
                                    String name = M3U_URL.equals(requested)
                                        ? "m3u_cache.txt" : "txt_cache.txt";
                                    FileOutputStream fos = openFileOutput(name, Context.MODE_PRIVATE);
                                    fos.write(fullText.getBytes("UTF-8"));
                                    fos.close();
                                } catch (Exception ignored) { }
                                applyPlaylist(list, true);
                            }
                            refreshing = false;
                        }
                    });
                } catch (final Exception error) {
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            if (requested.equals(sourceUrl)) {
                                setStatus("远程更新失败：已保留本地节目单。请检查 GitHub 访问权限。");
                            }
                            refreshing = false;
                        }
                    });
                }
            }
        }, "ChinaIptvRefresh").start();
    }

    private static String downloadPlaylist(String url) throws Exception {
        String withNonce = url + "?_update=" + System.currentTimeMillis();
        HttpURLConnection conn = (HttpURLConnection) new URL(withNonce).openConnection();
        if (conn instanceof HttpsURLConnection && android.os.Build.VERSION.SDK_INT <= 19) {
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(null, null, new SecureRandom());
            ((HttpsURLConnection) conn).setSSLSocketFactory(
                new LegacyTlsFactory(context.getSocketFactory()));
        }
        conn.setConnectTimeout(9000);
        conn.setReadTimeout(13000);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestProperty("User-Agent", "ChinaIptv44/1.0");
        try {
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
                throw new Exception("HTTP " + conn.getResponseCode());
            return readText(conn.getInputStream(), 512 * 1024);
        } finally {
            conn.disconnect();
        }
    }

    private static String readText(InputStream input, int maxBytes) throws Exception {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] tmp = new byte[8192];
            int total = 0;
            int count;
            while ((count = input.read(tmp)) != -1) {
                total += count;
                if (total > maxBytes) throw new Exception("节目单超过安全长度");
                buffer.write(tmp, 0, count);
            }
            return buffer.toString("UTF-8");
        } finally {
            input.close();
        }
    }

    private static List<Channel> parsePlaylist(String content) {
        List<Channel> list = new ArrayList<Channel>();
        if (content == null) return list;
        String group = "直播";
        String m3uName = null;
        String[] lines = content.replace("\r", "").split("\n");
        for (String raw : lines) {
            String line = raw.trim();
            if (line.length() == 0 || line.equals("#EXTM3U")) continue;
            if (line.startsWith("#EXTINF:")) {
                int comma = line.lastIndexOf(',');
                m3uName = comma >= 0 ? line.substring(comma + 1).trim() : null;
                int g = line.indexOf("group-title=");
                if (g >= 0) {
                    int start = g + "group-title=".length();
                    if (start < line.length() && line.charAt(start) == '"') {
                        int end = line.indexOf('"', start + 1);
                        if (end > start) group = line.substring(start + 1, end);
                    }
                }
                continue;
            }
            if (line.startsWith("#")) continue;
            if (line.contains(",#genre#")) {
                group = line.substring(0, line.indexOf(",#genre#")).trim();
                m3uName = null;
                continue;
            }
            String name;
            String address;
            int comma = line.indexOf(',');
            if (comma > 0 && !(line.startsWith("http://") || line.startsWith("https://")
                    || line.startsWith("rtsp://"))) {
                name = line.substring(0, comma).trim();
                address = line.substring(comma + 1).trim();
            } else {
                address = line;
                name = m3uName == null ? "频道 " + (list.size() + 1) : m3uName;
            }
            m3uName = null;
            if (!(address.startsWith("rtsp://") || address.startsWith("http://")
                    || address.startsWith("https://"))) continue;
            if (name.length() == 0) name = "频道 " + (list.size() + 1);
            if (address.length() > 8192) continue;
            list.add(new Channel(group, name, address));
            if (list.size() >= 1000) break;
        }
        return list;
    }

    @Override protected void onDestroy() {
        handler.removeCallbacks(periodicUpdate);
        if (video != null) video.stopPlayback();
        super.onDestroy();
    }

    private static final class Channel {
        final String group;
        final String name;
        final String url;
        Channel(String group, String name, String url) {
            this.group = group;
            this.name = name;
            this.url = url;
        }
    }

    private static final class LegacyTlsFactory extends SSLSocketFactory {
        private final SSLSocketFactory delegate;
        LegacyTlsFactory(SSLSocketFactory delegate) { this.delegate = delegate; }

        private Socket enable(Socket socket) {
            if (socket instanceof SSLSocket) {
                SSLSocket ssl = (SSLSocket) socket;
                for (String p : ssl.getSupportedProtocols()) {
                    if ("TLSv1.2".equals(p)) {
                        ssl.setEnabledProtocols(new String[] {"TLSv1.2"});
                        break;
                    }
                }
            }
            return socket;
        }
        @Override public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }
        @Override public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }
        @Override public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
                throws java.io.IOException {
            return enable(delegate.createSocket(socket, host, port, autoClose));
        }
        @Override public Socket createSocket(String host, int port)
                throws java.io.IOException {
            return enable(delegate.createSocket(host, port));
        }
        @Override public Socket createSocket(String host, int port, InetAddress local, int localPort)
                throws java.io.IOException {
            return enable(delegate.createSocket(host, port, local, localPort));
        }
        @Override public Socket createSocket(InetAddress host, int port)
                throws java.io.IOException {
            return enable(delegate.createSocket(host, port));
        }
        @Override public Socket createSocket(InetAddress host, int port, InetAddress local,
                int localPort) throws java.io.IOException {
            return enable(delegate.createSocket(host, port, local, localPort));
        }
    }
}
