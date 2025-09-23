package social.nostr.signer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.annotation.Nullable;

public class TestCursorProvider extends ContentProvider {
    public boolean rejected = false;

    @Override
    public boolean onCreate() { return true; }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String authority = uri.getAuthority();
        // authority pattern: <package>.OPERATION
        if (authority == null) return null;
        String[] cols;
        MatrixCursor c;
        if (authority.endsWith("GET_PUBLIC_KEY")) {
            cols = rejected ? new String[]{"rejected"} : new String[]{"signature"};
            c = new MatrixCursor(cols);
            if (rejected) {
                c.addRow(new Object[]{"1"});
            } else {
                c.addRow(new Object[]{"npub1testpublickey"});
            }
            return c;
        } else if (authority.endsWith("SIGN_EVENT")) {
            cols = rejected ? new String[]{"rejected"} : new String[]{"signature", "event"};
            c = new MatrixCursor(cols);
            if (rejected) {
                c.addRow(new Object[]{"1"});
            } else {
                c.addRow(new Object[]{"signaturehex", projection != null && projection.length > 0 ? projection[0] : "{}"});
            }
            return c;
        } else if (authority.endsWith("NIP04_ENCRYPT") || authority.endsWith("NIP44_ENCRYPT")) {
            cols = rejected ? new String[]{"rejected"} : new String[]{"signature"};
            c = new MatrixCursor(cols);
            if (rejected) {
                c.addRow(new Object[]{"1"});
            } else {
                c.addRow(new Object[]{"encrypted"});
            }
            return c;
        } else if (authority.endsWith("NIP04_DECRYPT") || authority.endsWith("NIP44_DECRYPT")) {
            cols = rejected ? new String[]{"rejected"} : new String[]{"signature"};
            c = new MatrixCursor(cols);
            if (rejected) {
                c.addRow(new Object[]{"1"});
            } else {
                c.addRow(new Object[]{"decrypted"});
            }
            return c;
        } else if (authority.endsWith("DECRYPT_ZAP_EVENT")) {
            cols = rejected ? new String[]{"rejected"} : new String[]{"signature"};
            c = new MatrixCursor(cols);
            if (rejected) {
                c.addRow(new Object[]{"1"});
            } else {
                c.addRow(new Object[]{"{\"result\":true}"});
            }
            return c;
        } else if (authority.endsWith("GET_RELAYS")) {
            cols = rejected ? new String[]{"rejected"} : new String[]{"signature"};
            c = new MatrixCursor(cols);
            if (rejected) {
                c.addRow(new Object[]{"1"});
            } else {
                c.addRow(new Object[]{"[\"wss://relay.example.com\"]"});
            }
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
