package social.nostr.signer;

import static org.junit.Assert.*;

import android.content.ContentProvider;
import android.content.pm.ProviderInfo;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowContentResolver;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class NostrSignerRobolectricTest {

    private ShadowContentResolver shadowResolver;
    private String packageName;

    @Before
    public void setup() {
        shadowResolver = Shadows.shadowOf(ApplicationProvider.getApplicationContext().getContentResolver());
        packageName = ApplicationProvider.getApplicationContext().getPackageName();

        registerProvider(packageName + ".GET_PUBLIC_KEY", new TestCursorProvider());
        registerProvider(packageName + ".SIGN_EVENT", new TestCursorProvider());
        registerProvider(packageName + ".NIP04_ENCRYPT", new TestCursorProvider());
        registerProvider(packageName + ".NIP04_DECRYPT", new TestCursorProvider());
        registerProvider(packageName + ".NIP44_ENCRYPT", new TestCursorProvider());
        registerProvider(packageName + ".NIP44_DECRYPT", new TestCursorProvider());
        registerProvider(packageName + ".DECRYPT_ZAP_EVENT", new TestCursorProvider());
    }

    private void registerProvider(String authority, ContentProvider provider) {
        ProviderInfo info = new ProviderInfo();
        info.authority = authority;
        shadowResolver.registerProviderInternal(authority, provider);
    }

    @Test
    public void getPublicKey_resolverReturnsNpub() {
        NostrSigner signer = new NostrSigner();
        String npub = signer.getPublicKey(ApplicationProvider.getApplicationContext(), packageName);
        assertEquals("npub1testpublickey", npub);
    }

    @Test
    public void signEvent_resolverReturnsSignatureAndEvent() {
        NostrSigner signer = new NostrSigner();
        String eventJson = "{\"kind\":1}";
        String[] result = signer.signEvent(ApplicationProvider.getApplicationContext(), packageName, eventJson, "npubX");
        assertNotNull(result);
        assertEquals("signaturehex", result[0]);
        assertEquals(eventJson, result[1]);
    }

    @Test
    public void nip04Encrypt_resolverReturnsEncrypted() {
        NostrSigner signer = new NostrSigner();
        String res = signer.nip04Encrypt(ApplicationProvider.getApplicationContext(), packageName, "hello", "pub", "npub");
        assertEquals("encrypted", res);
    }

    @Test
    public void nip04Decrypt_resolverReturnsDecrypted() {
        NostrSigner signer = new NostrSigner();
        String res = signer.nip04Decrypt(ApplicationProvider.getApplicationContext(), packageName, "enc", "pub", "npub");
        assertEquals("decrypted", res);
    }

    @Test
    public void nip44Encrypt_resolverReturnsEncrypted() {
        NostrSigner signer = new NostrSigner();
        String res = signer.nip44Encrypt(ApplicationProvider.getApplicationContext(), packageName, "hello", "pub", "npub");
        assertEquals("encrypted", res);
    }

    @Test
    public void nip44Decrypt_resolverReturnsDecrypted() {
        NostrSigner signer = new NostrSigner();
        String res = signer.nip44Decrypt(ApplicationProvider.getApplicationContext(), packageName, "enc", "pub", "npub");
        assertEquals("decrypted", res);
    }

    @Test
    public void decryptZapEvent_resolverReturnsJson() {
        NostrSigner signer = new NostrSigner();
        String res = signer.decryptZapEvent(ApplicationProvider.getApplicationContext(), packageName, "{\"zap\":true}", "npub");
        assertEquals("{\"result\":true}", res);
    }

    @Test
    public void providerRejected_returnsSentinel() {
        registerProvider(packageName + ".GET_PUBLIC_KEY", new TestRejectedCursorProvider());

        NostrSigner signer = new NostrSigner();
        String result = signer.getPublicKey(ApplicationProvider.getApplicationContext(), packageName);
        assertEquals(NostrSigner.REJECTED, result);
    }
}
