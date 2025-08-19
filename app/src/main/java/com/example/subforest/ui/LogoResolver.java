package com.example.subforest.ui;

import android.content.Context;
import android.net.Uri;
import java.io.File;

public final class LogoResolver {
    private LogoResolver() {}

    public static Object toGlideModel(Context ctx, String logoUrl) {
        if (logoUrl == null || logoUrl.trim().isEmpty()) return null;

        // http(s)는 그대로
        if (logoUrl.startsWith("http://") || logoUrl.startsWith("https://")) {
            return logoUrl;
        }

        // "/static/..."
        if (logoUrl.startsWith("/static/")) {
            // 1) 내부 파일 (앱이 저장해둔 경우)
            File f = new File(ctx.getFilesDir(), logoUrl); // filesDir + "/static/logo/xxx.png"
            if (f.exists()) return Uri.fromFile(f);

            // 2) assets
            String assetPath = "file:///android_asset" + logoUrl; // file:///android_asset/static/logo/xxx.png
            return assetPath;
        }

        // file://, content:// 은 그대로
        if (logoUrl.startsWith("file://") || logoUrl.startsWith("content://")) {
            return logoUrl;
        }

        return logoUrl;
    }
}