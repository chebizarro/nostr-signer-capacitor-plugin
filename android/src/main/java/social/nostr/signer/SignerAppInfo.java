package social.nostr.signer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

class SignerAppInfo {
	
	String name;
	String packageName;
	String icon;
	String iconUrl;

	public SignerAppInfo(CharSequence appName, String packageName, Drawable icon) {
		this.name = appName != null ? appName.toString() : "";
		this.packageName = packageName;
		this.icon = drawableToBase64(icon);
		this.iconUrl = "data:image/png;base64," + this.icon;
	}

	private String drawableToBase64(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
			// Handle vector drawables and others
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			drawable.draw(canvas);
        }
        return bitmapToBase64(bitmap);
    }
	
	private String bitmapToBase64(Bitmap bitmap) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
		byte[] byteArray = outputStream.toByteArray();
		return Base64.encodeToString(byteArray, Base64.NO_WRAP);
	}


}
