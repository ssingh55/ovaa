package oversecured.ovaa.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TheftOverwriteProvider extends ContentProvider {
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        File baseDir = Environment.getExternalStorageDirectory();
        File file = new File(baseDir, uri.getLastPathSegment());
        String baseCanonicalPath;
        String fileCanonicalPath;
        try {
            baseCanonicalPath = baseDir.getCanonicalPath();
            fileCanonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            throw new FileNotFoundException(e.getMessage());
        }
        if (!fileCanonicalPath.equals(baseCanonicalPath)
                && !fileCanonicalPath.startsWith(baseCanonicalPath + File.separator)) {
            throw new FileNotFoundException("Invalid file path");
        }
        return ParcelFileDescriptor.open(new File(fileCanonicalPath), ParcelFileDescriptor.MODE_READ_WRITE);
    }
}
