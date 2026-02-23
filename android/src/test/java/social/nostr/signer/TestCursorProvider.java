package social.nostr.signer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.annotation.Nullable;

public class TestCursorProvider extends ContentProvider {
    @Override
    public boolean onCreate() { return true; }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String authority = uri.getAuthority();
        if (authority == null) return null;
        MatrixCursor c;
        if (authority.endsWith("GET_PUBLIC_KEY")) {
            c = new MatrixCursor(new String[]{"result"});
            c.addRow(new Object[]{"npub180cvv07tjdrrgpa0j7j7tmnyl2yr6yr7l8j4s3evf6u64th6gkwsyjh6w6"});
            return c;
        } else if (authority.endsWith("SIGN_EVENT")) {
            c = new MatrixCursor(new String[]{"result", "event"});
            String event = projection != null && projection.length > 0 ? projection[0] : "{}";
            c.addRow(new Object[]{"signaturehex", event});
            return c;
        } else if (authority.endsWith("NIP04_ENCRYPT") || authority.endsWith("NIP44_ENCRYPT")) {
            c = new MatrixCursor(new String[]{"result"});
            c.addRow(new Object[]{"encrypted"});
            return c;
        } else if (authority.endsWith("NIP04_DECRYPT") || authority.endsWith("NIP44_DECRYPT")) {
            c = new MatrixCursor(new String[]{"result"});
            c.addRow(new Object[]{"decrypted"});
            return c;
        } else if (authority.endsWith("DECRYPT_ZAP_EVENT")) {
            c = new MatrixCursor(new String[]{"result"});
            c.addRow(new Object[]{"{\"result\":true}"});
            return c;
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) { return null; }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) { return null; }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) { return 0; }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { return 0; }
}
