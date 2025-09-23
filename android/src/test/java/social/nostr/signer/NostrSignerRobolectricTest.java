package social.nostr.signer;

import static org.junit.Assert.*;

import android.content.pm.ProviderInfo;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
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

        // Register providers per authority for success path
        ProviderInfo info = new ProviderInfo();
        info.authority = packageName + ".GET_PUBLIC_KEY";
        shadowResolver.registerProviderInternal(info.authority, new TestCursorProvider());

        info = new ProviderInfo();
        info.authority = packageName + ".SIGN_EVENT";
        shadowResolver.registerProviderInternal(info.authority, new TestCursorProvider());

        info = new ProviderInfo();
        info.authority = packageName + ".NIP04_ENCRYPT";
        shadowResolver.registerProviderInternal(info.authority, new TestCursorProvider());

        info = new ProviderInfo();
        info.authority = packageName + ".NIP04_DECRYPT";
        shadowResolver.registerProviderInternal(info.authority, new TestCursorProvider());

        info = new ProviderInfo();
        info.authority = packageName + ".NIP44_ENCRYPT";
        shadowResolver.registerProviderInternal(info.authority, new TestCursorProvider());

        info = new ProviderInfo();
        info.authority = packageName + ".NIP44_DECRYPT";
        shadowResolver.registerProviderInternal(info.authority, new TestCursorProvider());

        info = new ProviderInfo();
        info.authority = packageName + ".DECRYPT_ZAP_EVENT";
        shadowResolver.registerProviderInternal(info.authority, new TestCursorProvider());

        info = new ProviderInfo();
        info.authority = packageName + ".GET_RELAYS";
        shadowResolver.registerProviderInternal(info.authority, new TestCursorProvider());
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
    public void getRelays_resolverReturnsList() {
        NostrSigner signer = new NostrSigner();
        String res = signer.getRelays(ApplicationProvider.getApplicationContext(), packageName, "id-1", "npub");
        assertEquals("[\"wss://relay.example.com\"]", res);
    }

    @Test
    public void providerRejected_returnsNullToTriggerIntentFallback() {
        // Re-register a provider that always rejects for GET_PUBLIC_KEY
        ProviderInfo info = new ProviderInfo();
        String authority = packageName + ".GET_PUBLIC_KEY";
        info.authority = authority;
        shadowResolver.registerProviderInternal(authority, new TestRejectedCursorProvider());

        NostrSigner signer = new NostrSigner();
        String npub = signer.getPublicKey(ApplicationProvider.getApplicationContext(), packageName);
        assertNull(npub);
    }
}
