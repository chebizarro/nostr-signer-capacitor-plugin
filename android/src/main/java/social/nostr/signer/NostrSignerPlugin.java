package social.nostr.signer;

import android.content.Intent;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.app.Activity;

import androidx.activity.result.ActivityResult;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import java.util.List;

@CapacitorPlugin(name = "NostrSignerPlugin")
public class NostrSignerPlugin extends Plugin {

	private NostrSigner implementation;
	private String signerPackageName = null;

	@Override
	public void load() {
		implementation = new NostrSigner();
	}

	@PluginMethod
	public void isExternalSignerInstalled(PluginCall call) {
		Context context = getContext();
		String requested = call.getString("packageName");
		String effectivePackage = (requested != null && !requested.isEmpty()) ? requested : signerPackageName;
		List<ResolveInfo> signers = implementation.isExternalSignerInstalled(context, effectivePackage);
		JSObject ret = new JSObject();
		ret.put("installed", !signers.isEmpty());
		call.resolve(ret);
	}

	@PluginMethod
	public void getInstalledSignerApps(PluginCall call) {
		Context context = getContext();
		List<SignerAppInfo> signerAppInfos = implementation.getInstalledSignerApps(context);
		JSArray appsArray = new JSArray();
		for (SignerAppInfo signerAppInfo : signerAppInfos) {
			JSObject appInfo = new JSObject();
			appInfo.put("name", signerAppInfo.name);
			appInfo.put("packageName", signerAppInfo.packageName);
			if (signerAppInfo.iconUrl != null && !signerAppInfo.iconUrl.isEmpty()) {
				appInfo.put("iconUrl", signerAppInfo.iconUrl);
			}
			appsArray.put(appInfo);
		}
		JSObject ret = new JSObject();
		ret.put("apps", appsArray);
		call.resolve(ret);
	}

	private String getPackageName(PluginCall call) {
		String packageName = call.getString("packageName");
		if (packageName == null || packageName.isEmpty()) {
			packageName = signerPackageName;
		}
		return packageName;
	}

	@PluginMethod
	public void setPackageName(PluginCall call) {
		String packageName = getPackageName(call);
		if (packageName == null || packageName.isEmpty()) {
			call.reject("Missing or empty packageName parameter");
			return;
		}
		signerPackageName = packageName;
		call.resolve();
	}

	@PluginMethod
	public void getPublicKey(PluginCall call) {
		String packageName = getPackageName(call);

		// If we have a package name, try the content resolver first (background, no UI).
		if (packageName != null && !packageName.isEmpty()) {
			String result = implementation.getPublicKey(getContext(), packageName);
			if (NostrSigner.REJECTED.equals(result)) {
				call.reject("Rejected by signer", "REJECTED", (JSObject) null);
				return;
			}
			if (result != null) {
				JSObject ret = new JSObject();
				ret.put("npub", result);
				ret.put("package", packageName);
				call.resolve(ret);
				return;
			}
		}

		// Fall back to intent (also the only path when no package name is known yet).
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:"));
		if (packageName != null && !packageName.isEmpty()) {
			intent.setPackage(packageName);
		}
		intent.putExtra("type", "get_public_key");
		String permissions = call.getString("permissions");
		if (permissions != null) {
			intent.putExtra("permissions", permissions);
		}
		startActivityForResult(call, intent, "getPublicKeyResult");
	}

	@ActivityCallback
	private void getPublicKeyResult(PluginCall call, ActivityResult result) {
		if (result.getResultCode() == Activity.RESULT_CANCELED) {
			call.reject("Activity Cancelled", "CANCELLED", (JSObject) null);
			return;
		}
		Intent data = result.getData();
		if (data == null) {
			call.reject("No data returned", "INTENT_FAILED", (JSObject) null);
			return;
		}
		String npub = data.getStringExtra("result");
		String packageName = data.getStringExtra("package");
		JSObject ret = new JSObject();
		ret.put("npub", npub);
		ret.put("package", packageName);
		call.resolve(ret);
	}

	@PluginMethod
	public void signEvent(PluginCall call) {
		String packageName = getPackageName(call);
		if (packageName == null || packageName.isEmpty()) {
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

		String[] signedEvent = implementation.signEvent(getContext(), packageName, eventJson, npub);
		if (signedEvent != null) {
			if (NostrSigner.REJECTED.equals(signedEvent[0])) {
				call.reject("Rejected by signer", "REJECTED", (JSObject) null);
				return;
			}
			JSObject ret = new JSObject();
			ret.put("signature", signedEvent[0]);
			ret.put("id", eventId);
			ret.put("event", signedEvent[1]);
			call.resolve(ret);
		} else {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:" + eventJson));
			intent.setPackage(packageName);
			intent.putExtra("type", "sign_event");
			intent.putExtra("id", eventId);
			intent.putExtra("current_user", npub);
			startActivityForResult(call, intent, "signEventActivity");
		}
	}

	@ActivityCallback
	private void signEventActivity(PluginCall call, ActivityResult result) {
		if (result.getResultCode() == Activity.RESULT_CANCELED) {
			call.reject("Activity Cancelled", "CANCELLED", (JSObject) null);
			return;
		}
		Intent data = result.getData();
		if (data == null) {
			call.reject("No data returned", "INTENT_FAILED", (JSObject) null);
			return;
		}
		JSObject ret = new JSObject();
		ret.put("signature", data.getStringExtra("result"));
		ret.put("id", data.getStringExtra("id"));
		ret.put("event", data.getStringExtra("event"));
		call.resolve(ret);
	}

	@PluginMethod
	public void nip04Encrypt(PluginCall call) {
		String packageName = getPackageName(call);
		if (packageName == null || packageName.isEmpty()) {
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

		String encryptedText = implementation.nip04Encrypt(getContext(), packageName, plainText, pubKey, npub);
		if (encryptedText != null) {
			if (NostrSigner.REJECTED.equals(encryptedText)) {
				call.reject("Rejected by signer", "REJECTED", (JSObject) null);
				return;
			}
			JSObject ret = new JSObject();
			ret.put("result", encryptedText);
			ret.put("id", id);
			call.resolve(ret);
		} else {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:" + plainText));
			intent.setPackage(packageName);
			intent.putExtra("type", "nip04_encrypt");
			intent.putExtra("id", id);
			intent.putExtra("current_user", npub);
			intent.putExtra("pubkey", pubKey);
			startActivityForResult(call, intent, "genericResultActivity");
		}
	}

	@PluginMethod
	public void nip44Encrypt(PluginCall call) {
		String packageName = getPackageName(call);
		if (packageName == null || packageName.isEmpty()) {
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

		String encryptedText = implementation.nip44Encrypt(getContext(), packageName, plainText, pubKey, npub);
		if (encryptedText != null) {
			if (NostrSigner.REJECTED.equals(encryptedText)) {
				call.reject("Rejected by signer", "REJECTED", (JSObject) null);
				return;
			}
			JSObject ret = new JSObject();
			ret.put("result", encryptedText);
			ret.put("id", id);
			call.resolve(ret);
		} else {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:" + plainText));
			intent.setPackage(packageName);
			intent.putExtra("type", "nip44_encrypt");
			intent.putExtra("id", id);
			intent.putExtra("current_user", npub);
			intent.putExtra("pubkey", pubKey);
			startActivityForResult(call, intent, "genericResultActivity");
		}
	}

	@PluginMethod
	public void nip04Decrypt(PluginCall call) {
		String packageName = getPackageName(call);
		if (packageName == null || packageName.isEmpty()) {
			call.reject("Signer package name not set. Call setPackageName first.");
			return;
		}
		String encryptedText = call.getString("encryptedText");
		String pubKey = call.getString("pubKey");
		String npub = call.getString("npub");
		String id = call.getString("id");
		if (encryptedText == null || pubKey == null || npub == null) {
			call.reject("Missing parameters");
			return;
		}

		String decryptedText = implementation.nip04Decrypt(getContext(), packageName, encryptedText, pubKey, npub);
		if (decryptedText != null) {
			if (NostrSigner.REJECTED.equals(decryptedText)) {
				call.reject("Rejected by signer", "REJECTED", (JSObject) null);
				return;
			}
			JSObject ret = new JSObject();
			ret.put("result", decryptedText);
			ret.put("id", id);
			call.resolve(ret);
		} else {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:" + encryptedText));
			intent.setPackage(packageName);
			intent.putExtra("type", "nip04_decrypt");
			intent.putExtra("id", id);
			intent.putExtra("current_user", npub);
			intent.putExtra("pubkey", pubKey);
			startActivityForResult(call, intent, "genericResultActivity");
		}
	}

	@PluginMethod
	public void nip44Decrypt(PluginCall call) {
		String packageName = getPackageName(call);
		if (packageName == null || packageName.isEmpty()) {
			call.reject("Signer package name not set. Call setPackageName first.");
			return;
		}
		String encryptedText = call.getString("encryptedText");
		String pubKey = call.getString("pubKey");
		String npub = call.getString("npub");
		String id = call.getString("id");
		if (encryptedText == null || pubKey == null || npub == null) {
			call.reject("Missing parameters");
			return;
		}

		String decryptedText = implementation.nip44Decrypt(getContext(), packageName, encryptedText, pubKey, npub);
		if (decryptedText != null) {
			if (NostrSigner.REJECTED.equals(decryptedText)) {
				call.reject("Rejected by signer", "REJECTED", (JSObject) null);
				return;
			}
			JSObject ret = new JSObject();
			ret.put("result", decryptedText);
			ret.put("id", id);
			call.resolve(ret);
		} else {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:" + encryptedText));
			intent.setPackage(packageName);
			intent.putExtra("type", "nip44_decrypt");
			intent.putExtra("id", id);
			intent.putExtra("current_user", npub);
			intent.putExtra("pubkey", pubKey);
			startActivityForResult(call, intent, "genericResultActivity");
		}
	}

	@PluginMethod
	public void decryptZapEvent(PluginCall call) {
		String packageName = getPackageName(call);
		if (packageName == null || packageName.isEmpty()) {
			call.reject("Signer package name not set. Call setPackageName first.");
			return;
		}
		String eventJson = call.getString("eventJson");
		String npub = call.getString("npub");
		String id = call.getString("id");
		if (eventJson == null || npub == null) {
			call.reject("Missing parameters");
			return;
		}

		String decryptedEventJson = implementation.decryptZapEvent(getContext(), packageName, eventJson, npub);
		if (decryptedEventJson != null) {
			if (NostrSigner.REJECTED.equals(decryptedEventJson)) {
				call.reject("Rejected by signer", "REJECTED", (JSObject) null);
				return;
			}
			JSObject ret = new JSObject();
			ret.put("result", decryptedEventJson);
			ret.put("id", id);
			call.resolve(ret);
		} else {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:" + eventJson));
			intent.setPackage(packageName);
			intent.putExtra("type", "decrypt_zap_event");
			intent.putExtra("id", id);
			intent.putExtra("current_user", npub);
			startActivityForResult(call, intent, "genericResultActivity");
		}
	}

	/** Shared activity callback for all single-result methods (encrypt/decrypt/zapEvent). */
	@ActivityCallback
	private void genericResultActivity(PluginCall call, ActivityResult result) {
		if (result.getResultCode() == Activity.RESULT_CANCELED) {
			call.reject("Activity Cancelled", "CANCELLED", (JSObject) null);
			return;
		}
		Intent data = result.getData();
		if (data == null) {
			call.reject("No data returned", "INTENT_FAILED", (JSObject) null);
			return;
		}
		JSObject ret = new JSObject();
		ret.put("result", data.getStringExtra("result"));
		ret.put("id", data.getStringExtra("id"));
		call.resolve(ret);
	}
}
