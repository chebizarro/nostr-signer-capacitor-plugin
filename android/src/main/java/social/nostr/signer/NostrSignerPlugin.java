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
		String packageName = call.getString("packageName");
		List<ResolveInfo> signers = implementation.isExternalSignerInstalled(context, signerPackageName);
		boolean isInstalled = !signers.isEmpty();
		JSObject ret = new JSObject();
		ret.put("installed", isInstalled);
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
			appInfo.put("icon", signerAppInfo.icon);
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
		String publicKey = implementation.getPublicKey(context, signerPackageName);
		if (publicKey != null) {
			JSObject ret = new JSObject();
			ret.put("npub", publicKey);
			ret.put("package", packageName);
			call.resolve(ret);
		} else {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:"));
			intent.setPackage(packageName);
			intent.putExtra("type", "get_public_key");

			String permissions = call.getString("permissions");
			if (permissions != null) {
				intent.putExtra("permissions", permissions);
			}
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
		String[] signedEventJson = implementation.signEvent(context, packageName, eventJson, npub);
		if (signedEventJson != null) {
			JSObject ret = new JSObject();
			ret.put("signature", signedEventJson[0]);
			ret.put("id", eventId);
			ret.put("event", signedEventJson[1]);
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
		String encryptedText = implementation.nip04Encrypt(context, packageName, plainText, pubKey, npub);
		if (encryptedText != null) {
			JSObject ret = new JSObject();
			ret.put("result", encryptedText);
			ret.put("id", id);
			call.resolve(ret);
		} else {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:" + plainText));
			intent.setPackage(signerPackageName);
			intent.putExtra("type", "nip04_encrypt");
			intent.putExtra("id", id);
			intent.putExtra("current_user", npub);
			intent.putExtra("pubKey", pubKey);
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
		String encryptedText = implementation.nip44Encrypt(context, packageName, plainText, pubKey, npub);
		if (encryptedText != null) {
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
			intent.putExtra("pubKey", pubKey);

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
		String decryptedText = implementation.nip04Decrypt(context, packageName, encryptedText, pubKey, npub);
		if (decryptedText != null) {
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
			intent.putExtra("pubKey", pubKey);
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
		String decryptedText = implementation.nip44Decrypt(context, packageName, encryptedText, pubKey, npub);

		if (decryptedText != null) {
			JSObject ret = new JSObject();
			ret.put("result", decryptedText);
			ret.put("id", id);
			call.resolve(ret);
		} else {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:" + encryptedText));
			intent.setPackage(signerPackageName);
			intent.putExtra("type", "nip44_decrypt");
			intent.putExtra("id", id);
			intent.putExtra("current_user", npub);
			intent.putExtra("pubKey", pubKey);

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
		String decryptedEventJson = implementation.decryptZapEvent(context, packageName, eventJson, npub);
		if (decryptedEventJson != null) {
			JSObject ret = new JSObject();
			ret.put("result", decryptedEventJson);
			ret.put("id", id);
			call.resolve(ret);
		} else {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:" + eventJson));
			intent.setPackage(signerPackageName);
			intent.putExtra("type", "decrypt_zap_event");
			intent.putExtra("id", id);
			intent.putExtra("current_user", npub);
			startActivityForResult(call, intent, "encryptEventActivity");
		}
	}

}
