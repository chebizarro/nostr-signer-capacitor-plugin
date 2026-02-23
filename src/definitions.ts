export interface SignerAppInfo {
  name: string;
  packageName: string;
  iconUrl?: string;
}

export type PermissionType =
  | 'get_public_key'
  | 'nip04_encrypt'
  | 'nip04_decrypt'
  | 'nip44_encrypt'
  | 'nip44_decrypt'
  | 'decrypt_zap_event'
  | 'sign_event'
  | 'nip';

export interface Permission {
  type: PermissionType;
  kind?: number; // for sign_event kinds or “nip” mapping
  checked?: boolean; // default true
}

// Native bridge interface (Android implementation)
export interface NostrSignerNative {
  setPackageName(options: { packageName: string }): Promise<void>;
  isExternalSignerInstalled(options?: { packageName?: string }): Promise<{ installed: boolean }>;
  getInstalledSignerApps(): Promise<{ apps: SignerAppInfo[] }>;

  getPublicKey(options?: {
    packageName?: string;
    permissions?: string; // JSON string only on native
  }): Promise<{ npub: string; package: string }>;

  signEvent(options: {
    packageName?: string;
    eventJson: string;
    eventId: string;
    npub: string;
  }): Promise<{ signature: string; id: string; event: string }>;

  nip04Encrypt(options: {
    packageName?: string;
    plainText: string;
    pubKey: string;
    npub: string;
    id?: string;
  }): Promise<{ result: string; id: string }>;

  nip04Decrypt(options: {
    packageName?: string;
    encryptedText: string;
    pubKey: string;
    npub: string;
    id?: string;
  }): Promise<{ result: string; id: string }>;

  nip44Encrypt(options: {
    packageName?: string;
    plainText: string;
    pubKey: string;
    npub: string;
    id?: string;
  }): Promise<{ result: string; id: string }>;

  nip44Decrypt(options: {
    packageName?: string;
    encryptedText: string;
    pubKey: string;
    npub: string;
    id?: string;
  }): Promise<{ result: string; id: string }>;

  decryptZapEvent(options: {
    packageName?: string;
    eventJson: string;
    npub: string;
    id?: string;
  }): Promise<{ result: string; id: string }>;
}

// Public JS API surface types
