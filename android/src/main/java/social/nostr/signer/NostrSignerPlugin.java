package social.nostr.signer;

import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.activity.result.ActivityResult;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import java.util.List;

import android.util.Log;

@CapacitorPlugin(name = "NostrSignerPlugin")
public class NostrSignerPlugin extends Plugin {

	protected PluginCall savedCall;
	protected static final int SIGNER_REQUEST_CODE = 1001;
	private static String signerPackageName = null;

	private static final String TAG = "Nostr Signer";

	@PluginMethod
	public void setPackageName(PluginCall call) {
		String packageName = call.getString("packageName");

		if (packageName == null || packageName.isEmpty()) {
			call.reject("Missing or empty packageName parameter");
			return;
		}

		signerPackageName = packageName;
		call.resolve();
	}

	@PluginMethod
	public void isExternalSignerInstalled(PluginCall call) {

		Context context = getContext();
		boolean isInstalled = isExternalSignerInstalled(context, signerPackageName);
		JSObject ret = new JSObject();
		ret.put("installed", isInstalled);
		call.resolve(ret);
	}

	public static boolean isExternalSignerInstalled(Context context, String packageName) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("nostrsigner:"));
		intent.setPackage(packageName);
		PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
		return !activities.isEmpty();
	}

	@PluginMethod
	public void getPublicKey(PluginCall call) {
		savedCall = call;

		if (signerPackageName == null || signerPackageName.isEmpty()) {
			call.reject("Signer package name not set. Call setPackageName first.");
			return;
		}

		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:"));
		intent.setPackage(signerPackageName);
		intent.putExtra("type", "get_public_key");

		startActivityForResult(call, intent, "getPublicKeyResult");
	}

	@ActivityCallback
	private void getPublicKeyResult(PluginCall call, ActivityResult result) {
		if (result.getResultCode() == Activity.RESULT_CANCELED) {
			call.reject("Activity Cancelled");
		} else {
			Intent data = result.getData();
			JSObject ret = new JSObject();
			String npub = data.getStringExtra("signature");
			String packageName = data.getStringExtra("package");
			ret.put("npub", npub);
			ret.put("package", packageName);
			call.resolve(ret);
		}
	}

	@PluginMethod
	public void signEvent(PluginCall call) {
		savedCall = call;

		if (signerPackageName == null || signerPackageName.isEmpty()) {
			call.reject("Signer package name not set. Call setPackageName first.");
			return;
		}

		String eventJson = call.getString("eventJson");
        String eventId = call.getString("eventId");
        String npub = call.getString("npub");

        if (eventJson == null || eventId == null || npub == null) {
			call.reject("Missing parameters");
			return;
		}

		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:" + eventJson));
		intent.setPackage(signerPackageName);
		intent.putExtra("type", "sign_event");
		intent.putExtra("id", eventId);
		intent.putExtra("current_user", npub);

		startActivityForResult(call, intent, "signEventActivity");
	}
	
	@ActivityCallback
	private void signEventActivity(PluginCall call, ActivityResult result) {
		if (result.getResultCode() == Activity.RESULT_CANCELED) {
			call.reject("Activity Cancelled");
		} else {
			Intent data = result.getData();
			JSObject ret = new JSObject();
			String signature = data.getStringExtra("signature");
			String id = data.getStringExtra("id");
			String signedEventJson = data.getStringExtra("event");
			ret.put("signature", signature);
			ret.put("id", id);
			ret.put("event", signedEventJson);
			call.resolve(ret);
		}
	}

	@PluginMethod
	public void nip04Encrypt(PluginCall call) {
		savedCall = call;

		if (signerPackageName == null || signerPackageName.isEmpty()) {
			call.reject("Signer package name not set. Call setPackageName first.");
			return;
		}

		String plainText = call.getString("plainText");
		String pubKey = call.getString("pubKey");
		String npub = call.getString("npub");
		String id = call.getString("id");

		if (plainText == null || pubKey == null || npub == null) {
			call.reject("Missing parameters");
			return;
		}

		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:" + plainText));
		intent.setPackage(signerPackageName);
		intent.putExtra("type", "nip04_encrypt");
		intent.putExtra("id", id);
		intent.putExtra("current_user", npub);
		intent.putExtra("pubKey", pubKey);

		startActivityForResult(call, intent, "encryptEventActivity");
	}

	@ActivityCallback
	private void encryptEventActivity(PluginCall call, ActivityResult result) {
		if (result.getResultCode() == Activity.RESULT_CANCELED) {
			call.reject("Activity Cancelled");
		} else {
			Intent data = result.getData();
			JSObject ret = new JSObject();
			String res = data.getStringExtra("signature");
			String resultId = data.getStringExtra("id");
			ret.put("result", res);
			ret.put("id", resultId);
			call.resolve(ret);
		}
	}

	@PluginMethod
	public void nip44Encrypt(PluginCall call) {
		savedCall = call;

		if (signerPackageName == null || signerPackageName.isEmpty()) {
			call.reject("Signer package name not set. Call setPackageName first.");
			return;
		}

		String plainText = call.getString("plainText");
		String pubKey = call.getString("pubKey");
		String npub = call.getString("npub");
		String id = call.getString("id", "some_id");

		if (plainText == null || pubKey == null || npub == null) {
			call.reject("Missing parameters");
			return;
		}

		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:" + plainText));
		intent.setPackage(signerPackageName);
		intent.putExtra("type", "nip44_encrypt");
		intent.putExtra("id", id);
		intent.putExtra("current_user", npub);
		intent.putExtra("pubKey", pubKey);

		startActivityForResult(call, intent, "encryptEventActivity");
	}

	@PluginMethod
	public void nip04Decrypt(PluginCall call) {
		savedCall = call;

		if (signerPackageName == null || signerPackageName.isEmpty()) {
			call.reject("Signer package name not set. Call setPackageName first.");
			return;
		}

		String encryptedText = call.getString("encryptedText");
		String pubKey = call.getString("pubKey");
		String npub = call.getString("npub");
		String id = call.getString("id", "some_id");

		if (encryptedText == null || pubKey == null || npub == null) {
			call.reject("Missing parameters");
			return;
		}

		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:" + encryptedText));
		intent.setPackage(signerPackageName);
		intent.putExtra("type", "nip04_decrypt");
		intent.putExtra("id", id);
		intent.putExtra("current_user", npub);
		intent.putExtra("pubKey", pubKey);

		startActivityForResult(call, intent, "encryptEventActivity");
	}

	@PluginMethod
	public void nip44Decrypt(PluginCall call) {
		savedCall = call;

		if (signerPackageName == null || signerPackageName.isEmpty()) {
			call.reject("Signer package name not set. Call setPackageName first.");
			return;
		}

		String encryptedText = call.getString("encryptedText");
		String pubKey = call.getString("pubKey");
		String npub = call.getString("npub");
		String id = call.getString("id", "some_id");

		if (encryptedText == null || pubKey == null || npub == null) {
			call.reject("Missing parameters");
			return;
		}

		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:" + encryptedText));
		intent.setPackage(signerPackageName);
		intent.putExtra("type", "nip44_decrypt");
		intent.putExtra("id", id);
		intent.putExtra("current_user", npub);
		intent.putExtra("pubKey", pubKey);

		startActivityForResult(call, intent, "encryptEventActivity");
	}

	@PluginMethod
	public void decryptZapEvent(PluginCall call) {
		savedCall = call;

		if (signerPackageName == null || signerPackageName.isEmpty()) {
			call.reject("Signer package name not set. Call setPackageName first.");
			return;
		}

		String eventJson = call.getString("eventJson");
		String npub = call.getString("npub");
		String id = call.getString("id", "some_id");

		if (eventJson == null || npub == null) {
			call.reject("Missing parameters");
			return;
		}

		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:" + eventJson));
		intent.setPackage(signerPackageName);
		intent.putExtra("type", "decrypt_zap_event");
		intent.putExtra("id", id);
		intent.putExtra("current_user", npub);

		startActivityForResult(call, intent, "encryptEventActivity");
	}


}
