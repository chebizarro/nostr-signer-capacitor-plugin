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
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("nostrsigner:"));
		PackageManager packageManager = context.getPackageManager();

		List<ResolveInfo> resolveInfos = isExternalSignerInstalled(context, null);
		List<SignerAppInfo> appsArray = new ArrayList<SignerAppInfo>();
		for (ResolveInfo resolveInfo : resolveInfos) {
			CharSequence appName = resolveInfo.loadLabel(packageManager);
			String packageName = resolveInfo.activityInfo.packageName;
			Drawable iconDrawable = resolveInfo.loadIcon(packageManager);
			SignerAppInfo appInfo = new SignerAppInfo(appName, packageName, iconDrawable);
			appsArray.add(appInfo);
		}
		return appsArray;
	}

	public String getPublicKey(Context context, String packageName) {
		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = Uri.parse("content://" + packageName + ".GET_PUBLIC_KEY");
		String[] projection = new String[] { "login" };
		Cursor result = contentResolver.query(uri, projection, null, null, null);
		if (result == null) {
			return null;
		}
		String npub = null;
		if (result.moveToFirst()) {
			int index = result.getColumnIndex("signature");
			if (index >= 0) {
				npub = result.getString(index);
			}
		}
		result.close();
		return npub;
	}

	public String[] signEvent(Context context, String packageName, String eventJson, String loggedInUserNpub) {
		Uri uri = Uri.parse("content://" + packageName + ".SIGN_EVENT");
		String[] projection = new String[] { eventJson, "", loggedInUserNpub };
		ContentResolver contentResolver = context.getContentResolver();
		Cursor result = contentResolver.query(uri, projection, "1", null, null);
		if (result == null) {
			return null;
		}
		String[] signedEvent = null;
		if (result.moveToFirst()) {
			int signatureIndex = result.getColumnIndex("signature");
			int eventIndex = result.getColumnIndex("event");

			if (signatureIndex >= 0 && eventIndex >= 0) {
				String signature = result.getString(signatureIndex);
				String signedEventJson = result.getString(eventIndex);
				signedEvent = new String[]{signature, signedEventJson};
			}
		}
		result.close();
		return signedEvent;
	}

	public String nip04Encrypt(Context context, String packageName, String plainText, String recipientPubKey, String loggedInUserNpub) {
		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = Uri.parse("content://" + packageName + ".NIP04_ENCRYPT");
		String[] projection = new String[] { plainText, recipientPubKey, loggedInUserNpub };
		Cursor result = contentResolver.query(uri, projection, null, null, null);

		if (result == null) {
			return null;
		}

		String encryptedText = null;
		if (result.moveToFirst()) {
			int index = result.getColumnIndex("signature");
			if (index >= 0) {
				encryptedText = result.getString(index);
			}
		}
		result.close();
		return encryptedText;
	}

	public String nip04Decrypt(Context context, String packageName, String encryptedText, String senderPubKey, String loggedInUserNpub) {
		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = Uri.parse("content://" + packageName + ".NIP04_DECRYPT");
		String[] projection = new String[] { encryptedText, senderPubKey, loggedInUserNpub };
		Cursor result = contentResolver.query(uri, projection, null, null, null);

		if (result == null) {
			return null;
		}

		String decryptedText = null;
		if (result.moveToFirst()) {
			int index = result.getColumnIndex("signature");
			if (index >= 0) {
				decryptedText = result.getString(index);
			}
		}
		result.close();
		return decryptedText;
	}

	public String nip44Encrypt(Context context, String packageName, String plainText, String recipientPubKey, String loggedInUserNpub) {
		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = Uri.parse("content://" + packageName + ".NIP44_ENCRYPT");
		String[] projection = new String[] { plainText, recipientPubKey, loggedInUserNpub };
		Cursor result = contentResolver.query(uri, projection, null, null, null);

		if (result == null) {
			return null;
		}

		String encryptedText = null;
		if (result.moveToFirst()) {
			int index = result.getColumnIndex("signature");
			if (index >= 0) {
				encryptedText = result.getString(index);
			}
		}
		result.close();
		return encryptedText;
	}

	public String nip44Decrypt(Context context, String packageName, String encryptedText, String senderPubKey, String loggedInUserNpub) {
		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = Uri.parse("content://" + packageName + ".NIP44_DECRYPT");
		String[] projection = new String[] { encryptedText, senderPubKey, loggedInUserNpub };
		Cursor result = contentResolver.query(uri, projection, null, null, null);

		if (result == null) {
			return null;
		}

		String decryptedText = null;
		if (result.moveToFirst()) {
			int index = result.getColumnIndex("signature");
			if (index >= 0) {
				decryptedText = result.getString(index);
			}
		}
		result.close();
		return decryptedText;
	}

	public String decryptZapEvent(Context context, String packageName, String eventJson, String loggedInUserNpub) {
		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = Uri.parse("content://" + packageName + ".DECRYPT_ZAP_EVENT");
		String[] projection = new String[] { eventJson, "", loggedInUserNpub };
		Cursor result = contentResolver.query(uri, projection, null, null, null);

		if (result == null) {
			return null;
		}

		String decryptedEventJson = null;
		if (result.moveToFirst()) {
			int index = result.getColumnIndex("signature");
			if (index >= 0) {
				decryptedEventJson = result.getString(index);
			}
		}
		result.close();
		return decryptedEventJson;
	}

	public String getRelays(Context context, String packageName, String loggedInUserNpub) {
		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = Uri.parse("content://" + packageName + ".GET_RELAYS");
		String[] projection = new String[] { loggedInUserNpub };
		Cursor result = contentResolver.query(uri, projection, null, null, null);

		if (result == null) {
			return null;
		}

		String relayJson = null;
		if (result.moveToFirst()) {
			int index = result.getColumnIndex("signature");
			if (index >= 0) {
				relayJson = result.getString(index);
			}
		}
		result.close();
		return relayJson;
	}

}
