package com.example.subforest.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import androidx.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

public final class ImageUtils {
    private ImageUtils() {}

    /**
     * 갤러리 등에서 고른 이미지(content://…)를 앱 내부 filesDir/static/logo/ 에 복사하고
     * DB에 넣을 상대 경로("/static/logo/xxx.png")를 반환
     */
    @Nullable
    public static String saveLogoAndReturnDbUrl(Context ctx, @Nullable Uri pickedImage, String serviceName) {
        if (pickedImage == null) return null;
        try {
            // 확장자 추론
            String ext = guessExtension(ctx.getContentResolver(), pickedImage);
            if (ext == null) ext = "png";

            // 파일명: 서비스명-uuid.ext (공백/특수문자 정리)
            String safeName = sanitize(serviceName);
            String fileName = safeName + "-" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;

            // 저장 위치
            File dest = new File(new File(ctx.getFilesDir(), "static/logo"), fileName);
            File parent = dest.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            try (InputStream in = ctx.getContentResolver().openInputStream(pickedImage);
                 FileOutputStream out = new FileOutputStream(dest)) {
                if (in == null) return null;
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) >= 0) out.write(buf, 0, n);
            }

            // DB에 저장할 상대 경로
            return "/static/logo/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String sanitize(String s) {
        if (s == null) return "logo";
        // 한글/영문/숫자/공백/대시/언더스코어만 허용, 나머지는 -
        String t = s.trim().replaceAll("[^\\p{IsHangul}A-Za-z0-9 _-]", "-");
        t = t.replaceAll("\\s+", "-");
        if (t.isEmpty()) t = "logo";
        return t.toLowerCase(Locale.ROOT);
    }

    @Nullable
    private static String guessExtension(ContentResolver cr, Uri uri) {
        String type = cr.getType(uri);
        if (type != null) {
            String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
            if (ext != null) return ext;
        }
        return null;
    }
}