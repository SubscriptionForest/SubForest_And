package com.example.subforest.network;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public final class MultipartUtils {
    private MultipartUtils(){}

    public static RequestBody plain(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    public static MultipartBody.Part image(Context ctx, Uri uri, String partName) {
        if (uri == null) return null;
        byte[] bytes = readAll(ctx, uri);
        String fileName = getFileName(ctx, uri);
        RequestBody body = RequestBody.create(MediaType.parse("image/*"), bytes);
        return MultipartBody.Part.createFormData(partName, fileName, body);
    }

    private static byte[] readAll(Context ctx, Uri uri) {
        try (InputStream is = ctx.getContentResolver().openInputStream(uri);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getFileName(Context ctx, Uri uri) {
        String name = "upload.jpg";
        Cursor c = ctx.getContentResolver().query(uri, null, null, null, null);
        if (c != null) {
            int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (idx >= 0 && c.moveToFirst()) name = c.getString(idx);
            c.close();
        }
        return name;
    }
}
