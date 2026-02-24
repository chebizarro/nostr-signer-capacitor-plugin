package social.nostr.signer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class NostrSigner {

	/** Sentinel returned when the content provider explicitly rejected the request. */
	static final String REJECTED = "__REJECTED__";

	private static final String BECH32_CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";

	/**
	 * Converts an npub (bech32-encoded public key) to a lowercase hex string.
	 * Returns the input unchanged if it is not a valid npub (e.g. already hex).
	 */
	static String npubToHex(String input) {
		if (input == null || !input.startsWith("npub1")) return input;
		try {
			// Strip the "npub1" human-readable part and decode the data part.
			String data = input.substring(5).toLowerCase();
			int[] values = new int[data.length()];
			for (int i = 0; i < data.length(); i++) {
				int v = BECH32_CHARSET.indexOf(data.charAt(i));
				if (v < 0) return input; // invalid character
				values[i] = v;
			}
			// Convert from 5-bit groups to 8-bit bytes (drop the 6-byte checksum at the end).
			byte[] bytes = convertBits(values, 0, values.length - 6, 5, 8, false);
			if (bytes == null || bytes.length != 32) return input;
			StringBuilder hex = new StringBuilder(64);
			for (byte b : bytes) hex.append(String.format("%02x", b & 0xff));
			return hex.toString();
		} catch (Exception e) {
			return input;
		}
	}

	private static byte[] convertBits(int[] data, int offset, int length, int from, int to, boolean pad) {
		int acc = 0, bits = 0;
		byte[] out = new byte[(length * from + to - 1) / to];
		int idx = 0;
		for (int i = offset; i < offset + length; i++) {
			acc = (acc << from) | data[i];
			bits += from;
			while (bits >= to) {
				bits -= to;
				out[idx++] = (byte) ((acc >> bits) & ((1 << to) - 1));
			}
		}
		if (pad && bits > 0) out[idx++] = (byte) ((acc << (to - bits)) & ((1 << to) - 1));
		if (!pad && (bits >= from || ((acc << (to - bits)) & ((1 << to) - 1)) != 0)) return null;
		return java.util.Arrays.copyOf(out, idx);
	}

	List<ResolveInfo> isExternalSignerInstalled(Context context, String packageName) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("nostrsigner:"));
		if (packageName != null) {
			intent.setPackage(packageName);
		}
		PackageManager packageManager = context.getPackageManager();
		return packageManager.queryIntentActivities(intent, 0);
	}

	List<SignerAppInfo> getInstalledSignerApps(Context context) {
		List<ResolveInfo> resolveInfos = isExternalSignerInstalled(context, null);
		PackageManager packageManager = context.getPackageManager();
		List<SignerAppInfo> appsArray = new ArrayList<SignerAppInfo>();
		for (ResolveInfo resolveInfo : resolveInfos) {
			CharSequence appName = resolveInfo.loadLabel(packageManager);
			String packageName = resolveInfo.activityInfo.packageName;
			Drawable iconDrawable = resolveInfo.loadIcon(packageManager);
			appsArray.add(new SignerAppInfo(appName, packageName, iconDrawable));
		}
		return appsArray;
	}

	/**
	 * Returns the npub string, {@link #REJECTED} if the provider explicitly rejected,
	 * or null if the provider is unavailable (trigger intent fallback).
	 */
	public String getPublicKey(Context context, String packageName) {
		Uri uri = Uri.parse("content://" + packageName + ".GET_PUBLIC_KEY");
		String[] projection = new String[] { "login" };
		return npubToHex(querySingleResult(context, uri, projection));
	}

	/**
	 * Returns [signature, eventJson], {@link #REJECTED} (as first element) if rejected,
	 * or null if unavailable.
	 */
	public String[] signEvent(Context context, String packageName, String eventJson, String loggedInUserNpub) {
		Uri uri = Uri.parse("content://" + packageName + ".SIGN_EVENT");
		String[] projection = new String[] { eventJson, "", loggedInUserNpub };
		ContentResolver contentResolver = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = contentResolver.query(uri, projection, "1", null, null);
			if (cursor == null) return null;
			if (cursor.moveToFirst()) {
				if (isRejected(cursor)) return new String[]{ REJECTED, null };
				int sigIdx = cursor.getColumnIndex("result");
				int evIdx = cursor.getColumnIndex("event");
				if (sigIdx >= 0 && evIdx >= 0) {
					return new String[]{ cursor.getString(sigIdx), cursor.getString(evIdx) };
				}
			}
			return null;
		} finally {
			if (cursor != null) cursor.close();
		}
	}

	public String nip04Encrypt(Context context, String packageName, String plainText, String recipientPubKey, String loggedInUserNpub) {
		Uri uri = Uri.parse("content://" + packageName + ".NIP04_ENCRYPT");
		return querySingleResult(context, uri, new String[]{ plainText, recipientPubKey, loggedInUserNpub });
	}

	public String nip04Decrypt(Context context, String packageName, String encryptedText, String senderPubKey, String loggedInUserNpub) {
		Uri uri = Uri.parse("content://" + packageName + ".NIP04_DECRYPT");
		return querySingleResult(context, uri, new String[]{ encryptedText, senderPubKey, loggedInUserNpub });
	}

	public String nip44Encrypt(Context context, String packageName, String plainText, String recipientPubKey, String loggedInUserNpub) {
		Uri uri = Uri.parse("content://" + packageName + ".NIP44_ENCRYPT");
		return querySingleResult(context, uri, new String[]{ plainText, recipientPubKey, loggedInUserNpub });
	}

	public String nip44Decrypt(Context context, String packageName, String encryptedText, String senderPubKey, String loggedInUserNpub) {
		Uri uri = Uri.parse("content://" + packageName + ".NIP44_DECRYPT");
		return querySingleResult(context, uri, new String[]{ encryptedText, senderPubKey, loggedInUserNpub });
	}

	public String decryptZapEvent(Context context, String packageName, String eventJson, String loggedInUserNpub) {
		Uri uri = Uri.parse("content://" + packageName + ".DECRYPT_ZAP_EVENT");
		return querySingleResult(context, uri, new String[]{ eventJson, "", loggedInUserNpub });
	}

	// -------------------------------------------------------------------------

	/**
	 * Queries a content provider and returns the value of the "result" column,
	 * {@link #REJECTED} if the provider explicitly rejected, or null if unavailable.
	 */
	private String querySingleResult(Context context, Uri uri, String[] projection) {
		ContentResolver contentResolver = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = contentResolver.query(uri, projection, null, null, null);
			if (cursor == null) return null;
			if (cursor.moveToFirst()) {
				if (isRejected(cursor)) return REJECTED;
				int index = cursor.getColumnIndex("result");
				if (index >= 0) return cursor.getString(index);
			}
			return null;
		} finally {
			if (cursor != null) cursor.close();
		}
	}

	private boolean isRejected(Cursor cursor) {
		int idx = cursor.getColumnIndex("rejected");
		if (idx < 0) return false;
		String val = cursor.getString(idx);
		return "1".equals(val) || "true".equalsIgnoreCase(val);
	}
}
