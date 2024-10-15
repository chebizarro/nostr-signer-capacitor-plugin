# Nostr Signer Capacitor Plugin

This Capacitor plugin allows your application to interact with Nostr signer apps on the Android platform using intents, following the [NIP-55](https://github.com/nostr-protocol/nips/blob/master/55.md) specification.

## Install

```bash
npm install nostr-signer-capacitor-plugin
npx cap sync
```

## Usage

### Import the Plugin

```typescript
import NostrSignerPlugin from 'nostr-signer-capacitor-plugin';
```

### Set the Signer Package Name

Before using the plugin on Android, you need to set the package name of the external signer app.

```typescript
await NostrSignerPlugin.setPackageName({ packageName: 'com.example.signer' });
```

### Check if External Signer is Installed

```typescript
const { installed } = await NostrSignerPlugin.isExternalSignerInstalled();
if (!installed) {
  console.log('External signer app is not installed.');
}
```

### Get a List of Installed External Signers

```typescript
try {
  const result = await NostrSignerPlugin.getInstalledSignerApps();
  signerApps = result.apps;
  console.log('Installed Signer Apps:', signerApps);
} catch (error) {
  console.error('Error getting installed signer apps:', error);
}
```

The AppInfo object has the following fields"

```typescript
export interface AppInfo {
  name: string;        // The name of the app as it appears in the System launcher
  packageName: string; // The package name of the app - pass this to setPackageName
  iconData: string;    // the base 64 encoded string of the app's ico
  iconUrl: string;    // the url to app's icon
}

```

### Get Public Key

```typescript
try {
  const { npub } = await NostrSignerPlugin.getPublicKey();
  console.log('Public Key:', npub);
} catch (error) {
  console.error('Error getting public key:', error);
}
```

### Sign Event

```typescript
const event = {
  kind: 1,
  content: 'Hello, Nostr!',
  tags: [],
  created_at: Math.floor(Date.now() / 1000),
};

try {
  const { event: signedEventJson } = await NostrSignerPlugin.signEvent({
    eventJson: JSON.stringify(event),
  });
  const signedEvent = JSON.parse(signedEventJson);
  console.log('Signed Event:', signedEvent);
} catch (error) {
  console.error('Error signing event:', error);
}
```

### NIP-04 Encrypt

```typescript
try {
  const { result: encryptedText } = await NostrSignerPlugin.nip04Encrypt({
    pubKey: 'recipient_public_key',
    plainText: 'Secret message',
  });
  console.log('Encrypted Text:', encryptedText);
} catch (error) {
  console.error('Error encrypting message:', error);
}
```

### NIP-04 Decrypt

```typescript
try {
  const { result: decryptedText } = await NostrSignerPlugin.nip04Decrypt({
    pubKey: 'sender_public_key',
    encryptedText: 'encrypted_text',
  });
  console.log('Decrypted Text:', decryptedText);
} catch (error) {
  console.error('Error decrypting message:', error);
}
```

### NIP-44 Encrypt

```typescript
try {
  const { result: encryptedText } = await NostrSignerPlugin.nip44Encrypt({
    pubKey: 'recipient_public_key',
    plainText: 'Secret message',
  });
  console.log('Encrypted Text (NIP-44):', encryptedText);
} catch (error) {
  console.error('Error encrypting message (NIP-44):', error);
}
```

### NIP-44 Decrypt

```typescript
try {
  const { result: decryptedText } = await NostrSignerPlugin.nip44Decrypt({
    pubKey: 'sender_public_key',
    encryptedText: 'encrypted_text',
  });
  console.log('Decrypted Text (NIP-44):', decryptedText);
} catch (error) {
  console.error('Error decrypting message (NIP-44):', error);
}
```

### Decrypt Zap Event

```typescript
try {
  const { result: decryptedEventJson } = await NostrSignerPlugin.decryptZapEvent({
    eventJson: JSON.stringify(encryptedEvent),
  });
  const decryptedEvent = JSON.parse(decryptedEventJson);
  console.log('Decrypted Zap Event:', decryptedEvent);
} catch (error) {
  console.error('Error decrypting zap event:', error);
}
```

## API

<docgen-index>

* [`getInstalledSignerApps(...)`](#getinstalledsignerapps)
* [`setPackageName(...)`](#setpackagename)
* [`isExternalSignerInstalled()`](#isexternalsignerinstalled)
* [`getPublicKey()`](#getpublickey)
* [`signEvent(...)`](#signevent)
* [`nip04Encrypt(...)`](#nip04encrypt)
* [`nip04Decrypt(...)`](#nip04decrypt)
* [`nip44Encrypt(...)`](#nip44encrypt)
* [`nip44Decrypt(...)`](#nip44decrypt)
* [`decryptZapEvent(...)`](#decryptzapevent)

</docgen-index>

<docgen-api>


### getInstalledSignerApps(...)

```typescript
getInstalledSignerApps() => Promise<{ apps: AppInfo[] }>
```

Returns a list of AppInfo objects which contain information about which Signer apps are installed.

**Returns:** <code>Promise&lt;{ apps: AppInfop%5B%5D }&gt;</code>


### setPackageName(...)

```typescript
setPackageName(options: { packageName: string; }) => Promise<void>
```

Sets the package name of the external Nostr signer app. This is required on Android to specify which app to interact with.

| Param         | Type                                 | Description                                   |
| ------------- | ------------------------------------ | --------------------------------------------- |
| **`options`** | <code>{ packageName: string; }</code> | An object containing the package name string. |

--------------------

### isExternalSignerInstalled()

```typescript
isExternalSignerInstalled() => Promise<{ installed: boolean; }>
```

Checks if the external Nostr signer app is installed on the device.

**Returns:** <code>Promise&lt;{ installed: boolean; }&gt;</code>

An object indicating whether the signer app is installed.

--------------------

### getPublicKey()

```typescript
getPublicKey() => Promise<{ npub: string; }>
```

Requests the public key from the Nostr signer app or extension.

**Returns:** <code>Promise&lt;{ npub: string; }&gt;</code>

An object containing the public key in npub format.

--------------------

### signEvent(...)

```typescript
signEvent(options: { eventJson: string; }) => Promise<{ event: string; }>
```

Requests the signer app to sign a Nostr event.

| Param         | Type                                | Description                       |
| ------------- | ----------------------------------- | --------------------------------- |
| **`options`** | <code>{ eventJson: string; }</code> | An object containing the event in JSON string format. |

**Returns:** <code>Promise&lt;{ event: string; }&gt;</code>

An object containing the signed event in JSON string format.

--------------------

### nip04Encrypt(...)

```typescript
nip04Encrypt(options: { plainText: string; pubKey: string; }) => Promise<{ result: string; }>
```

Encrypts a message using NIP-04 encryption.

| Param         | Type                                                  | Description                                 |
| ------------- | ----------------------------------------------------- | ------------------------------------------- |
| **`options`** | <code>{ plainText: string; pubKey: string; }</code>   | An object containing the plaintext and the recipient's public key. |

**Returns:** <code>Promise&lt;{ result: string; }&gt;</code>

An object containing the encrypted text.

--------------------

### nip04Decrypt(...)

```typescript
nip04Decrypt(options: { encryptedText: string; pubKey: string; }) => Promise<{ result: string; }>
```

Decrypts a message using NIP-04 decryption.

| Param         | Type                                                    | Description                                 |
| ------------- | ------------------------------------------------------- | ------------------------------------------- |
| **`options`** | <code>{ encryptedText: string; pubKey: string; }</code> | An object containing the encrypted text and the sender's public key. |

**Returns:** <code>Promise&lt;{ result: string; }&gt;</code>

An object containing the decrypted plaintext.

--------------------

### nip44Encrypt(...)

```typescript
nip44Encrypt(options: { plainText: string; pubKey: string; }) => Promise<{ result: string; }>
```

Encrypts a message using NIP-44 encryption.

| Param         | Type                                                  | Description                                 |
| ------------- | ----------------------------------------------------- | ------------------------------------------- |
| **`options`** | <code>{ plainText: string; pubKey: string; }</code>   | An object containing the plaintext and the recipient's public key. |

**Returns:** <code>Promise&lt;{ result: string; }&gt;</code>

An object containing the encrypted text.

--------------------

### nip44Decrypt(...)

```typescript
nip44Decrypt(options: { encryptedText: string; pubKey: string; }) => Promise<{ result: string; }>
```

Decrypts a message using NIP-44 decryption.

| Param         | Type                                                    | Description                                 |
| ------------- | ------------------------------------------------------- | ------------------------------------------- |
| **`options`** | <code>{ encryptedText: string; pubKey: string; }</code> | An object containing the encrypted text and the sender's public key. |

**Returns:** <code>Promise&lt;{ result: string; }&gt;</code>

An object containing the decrypted plaintext.

--------------------

### decryptZapEvent(...)

```typescript
decryptZapEvent(options: { eventJson: string; }) => Promise<{ result: string; }>
```

Decrypts a zap event.

| Param         | Type                                | Description                       |
| ------------- | ----------------------------------- | --------------------------------- |
| **`options`** | <code>{ eventJson: string; }</code> | An object containing the encrypted zap event in JSON string format. |

**Returns:** <code>Promise&lt;{ result: string; }&gt;</code>

An object containing the decrypted zap event in JSON string format.

--------------------

</docgen-api>

## Notes

- On **Android**, the plugin communicates with external signer apps using intents and the Content Resolver as per [NIP-55](https://github.com/nostr-protocol/nips/blob/master/55.md).
- Ensure that the external signer app or browser extension supports the required NIP specifications.

## License

[MIT License](LICENSE)