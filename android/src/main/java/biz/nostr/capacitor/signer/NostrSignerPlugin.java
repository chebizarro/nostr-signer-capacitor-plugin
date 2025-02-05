package biz.nostr.capacitor.signer;

import biz.nostr.android.nip55.Signer;
import biz.nostr.android.nip55.AppInfo;
import biz.nostr.android.nip55.IntentBuilder;

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

	private String signerPackageName = null;

	@PluginMethod
	public void isExternalSignerInstalled(PluginCall call) {
		Context context = getContext();
		String packageName = call.getString("packageName");
		List<ResolveInfo> signers = Signer.isExternalSignerInstalled(context, signerPackageName);
		boolean isInstalled = !signers.isEmpty();
		JSObject ret = new JSObject();
		ret.put("installed", isInstalled);
		call.resolve(ret);
	}

	@PluginMethod
	public void getInstalledSignerApps(PluginCall call) {
		Context context = getContext();
		List<AppInfo> signerAppInfos = Signer.getInstalledSignerApps(context);
		JSArray appsArray = new JSArray();
		for (AppInfo signerAppInfo : signerAppInfos) {
			JSObject appInfo = new JSObject();
			appInfo.put("name", signerAppInfo.name);
			appInfo.put("packageName", signerAppInfo.packageName);
			appInfo.put("iconData", signerAppInfo.iconData);
			appInfo.put("iconUrl", signerAppInfo.iconUrl);
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
		if (packageName == null || packageName.isEmpty()) {
			call.reject("Signer package name not set. Call setPackageName first.");
			return;
		}
		Context context = getContext();
		String publicKey = Signer.getPublicKey(context, signerPackageName);
		if (publicKey != null) {
			JSObject ret = new JSObject();
			ret.put("npub", publicKey);
			ret.put("package", packageName);
			call.resolve(ret);
		} else {
			String permissions = call.getString("permissions");
			Intent intent = IntentBuilder.getPublicKeyIntent(packageName, permissions);
			startActivityForResult(call, intent, "getPublicKeyResult");
		}
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
		Context context = getContext();
		String[] signedEventJson = Signer.signEvent(context, packageName, eventJson, npub);
		if (signedEventJson != null) {
			JSObject ret = new JSObject();
			ret.put("signature", signedEventJson[0]);
			ret.put("id", eventId);
			ret.put("event", signedEventJson[1]);
			call.resolve(ret);
		} else {
			Intent intent = IntentBuilder.signEventIntent(packageName, eventJson, eventId, npub);
			startActivityForResult(call, intent, "signEventActivity");
		}
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
		Context context = getContext();
		String encryptedText = Signer.nip04Encrypt(context, packageName, plainText, pubKey, npub);
		if (encryptedText != null) {
			JSObject ret = new JSObject();
			ret.put("result", encryptedText);
			ret.put("id", id);
			call.resolve(ret);
		} else {
			Intent intent = IntentBuilder.nip04EncryptIntent(packageName, plainText, id, npub, pubKey);
			startActivityForResult(call, intent, "encryptEventActivity");
		}
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

		Context context = getContext();
		String encryptedText = Signer.nip44Encrypt(context, packageName, plainText, pubKey, npub);
		if (encryptedText != null) {
			JSObject ret = new JSObject();
			ret.put("result", encryptedText);
			ret.put("id", id);
			call.resolve(ret);
		} else {
			Intent intent = IntentBuilder.nip44EncryptIntent(packageName, plainText, id, npub, pubKey);
			startActivityForResult(call, intent, "encryptEventActivity");
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

		Context context = getContext();
		String decryptedText = Signer.nip04Decrypt(context, packageName, encryptedText, pubKey, npub);
		if (decryptedText != null) {
			JSObject ret = new JSObject();
			ret.put("result", decryptedText);
			ret.put("id", id);
			call.resolve(ret);
		} else {
			Intent intent = IntentBuilder.nip04DecryptIntent(packageName, encryptedText, id, pubKey, npub);
			startActivityForResult(call, intent, "encryptEventActivity");
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

		Context context = getContext();
		String decryptedText = Signer.nip44Decrypt(context, packageName, encryptedText, pubKey, npub);

		if (decryptedText != null) {
			JSObject ret = new JSObject();
			ret.put("result", decryptedText);
			ret.put("id", id);
			call.resolve(ret);
		} else {
			Intent intent = IntentBuilder.nip44DecryptIntent(packageName, encryptedText, id, pubKey, npub);
			startActivityForResult(call, intent, "encryptEventActivity");
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

		Context context = getContext();
		String decryptedEventJson = Signer.decryptZapEvent(context, packageName, eventJson, npub);
		if (decryptedEventJson != null) {
			JSObject ret = new JSObject();
			ret.put("result", decryptedEventJson);
			ret.put("id", id);
			call.resolve(ret);
		} else {
			Intent intent = IntentBuilder.decryptZapEventIntent(packageName, eventJson, id, npub);
			startActivityForResult(call, intent, "encryptEventActivity");
		}
	}

}
