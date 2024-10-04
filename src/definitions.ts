export interface NostrSignerPlugin {
  injectNostr(): Promise<void>;
  /**
   * Retrieve an array of {AppInfo} records listing the available signer apps.
   */
  getInstalledSignerApps(): Promise<{ apps: AppInfo[] }>;
  /**
   * Checks if the external Nostr signer app is installed on the device.
   */
  isExternalSignerInstalled(options?: {
    packageName: string;
  }): Promise<{ installed: boolean }>;
  /**
   * Requests the public key from the Nostr signer app or extension
   * and returns an object containing the public key in npub format
   * and the package name of the signer app.
   */
  getPublicKey(options?: {
    permissions: Permission;
  }): Promise<{ npub: string; package?: string }>;

  setPackageName(options: { packageName: string }): Promise<void>;
  /**
   * Requests the signer app to sign a Nostr event.
   */
  signEvent(options: {
    eventJson: string;
    eventId: string;
    npub: string;
  }): Promise<{ signature: string; id: string; event: string }>;
  /**
   * Encrypts an event using NIP-04 encryption.
   */
  nip04Encrypt(options: {
    plainText: string;
    pubKey: string;
    npub: string;
    id?: string;
  }): Promise<{ result: string; id: string }>;
  /**
   * Decrypts an event using NIP-04 encryption.
   */
  nip04Decrypt(options: {
    encryptedText: string;
    pubKey: string;
    npub: string;
    id?: string;
  }): Promise<{ result: string; id: string }>;
  /**
   * Encrypts an event using NIP-44 encryption.
   */
  nip44Encrypt(options: {
    plainText: string;
    pubKey: string;
    npub: string;
    id?: string;
  }): Promise<{ result: string; id: string }>;
  /**
   * Decrypts an event using NIP-44 encryption.
   */
  nip44Decrypt(options: {
    encryptedText: string;
    pubKey: string;
    npub: string;
    id?: string;
  }): Promise<{ result: string; id: string }>;
  /**
   * Decrypts a zap event.
   */
  decryptZapEvent(options: {
    eventJson: string;
    npub: string;
    id?: string;
  }): Promise<{ result: string; id: string }>;
}

export interface AppInfo {
  name: string;
  packageName: string;
  icon: string;
}

export interface Permission {
  type: string;
  kind?: string;
}
