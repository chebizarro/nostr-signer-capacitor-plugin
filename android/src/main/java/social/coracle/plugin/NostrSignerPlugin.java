package social.coracle.plugin;

import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.database.Cursor;
import android.app.Activity;
import android.widget.Toast;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import java.util.List;

@CapacitorPlugin(name = "NostrSigner")
public class NostrSignerPlugin extends Plugin {

	protected PluginCall savedCall;
	protected static final int SIGNER_REQUEST_CODE = 1001;
	private static String signerPackageName = null;

	public static void setSignerPackageName(String packageName) {
		signerPackageName = packageName;
	}

	public static String getSignerPackageName() {
		return signerPackageName;
	}

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
		if (signerPackageName == null || signerPackageName.isEmpty()) {
			call.reject("Signer package name not set. Call setPackageName first.");
			return;
		}

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
		return activities.size() > 0;
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

		startActivityForResult(call, intent, SIGNER_REQUEST_CODE);
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

		startActivityForResult(call, intent, SIGNER_REQUEST_CODE);
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
		String id = call.getString("id", "some_id");

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

		startActivityForResult(call, intent, SIGNER_REQUEST_CODE);
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

		startActivityForResult(call, intent, SIGNER_REQUEST_CODE);
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

		startActivityForResult(call, intent, SIGNER_REQUEST_CODE);
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

		startActivityForResult(call, intent, SIGNER_REQUEST_CODE);
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

		startActivityForResult(call, intent, SIGNER_REQUEST_CODE);
	}

	@Override
	protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SIGNER_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK && savedCall != null) {
				String type = data.getStringExtra("type");
				JSObject ret = new JSObject();

				switch (type) {
					case "get_public_key":
						String npub = data.getStringExtra("signature");
						String packageName = data.getStringExtra("package");
						ret.put("npub", npub);
						ret.put("package", packageName);
						break;
					case "sign_event":
						String signature = data.getStringExtra("signature");
						String id = data.getStringExtra("id");
						String signedEventJson = data.getStringExtra("event");
						ret.put("signature", signature);
						ret.put("id", id);
						ret.put("event", signedEventJson);
						break;
					case "nip04_encrypt":
					case "nip44_encrypt":
					case "nip04_decrypt":
					case "nip44_decrypt":
					case "decrypt_zap_event":
						String result = data.getStringExtra("signature");
						String resultId = data.getStringExtra("id");
						ret.put("result", result);
						ret.put("id", resultId);
						break;
					default:
						savedCall.reject("Unknown response type");
						return;
				}
				savedCall.resolve(ret);
			} else if (savedCall != null) {
				savedCall.reject("Request rejected or cancelled");
			}
		}
	}
}
