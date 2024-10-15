export interface NostrSignerPlugin {
  getInstalledSignerApps(): Promise<{ apps: AppInfo[] }>;
  isExternalSignerInstalled(options?: {
    packageName: string;
  }): Promise<{ installed: boolean }>;
  getPublicKey(options?: {
    permissions: string;
  }): Promise<{ npub: string; package: string }>;
  setPackageName(options: { packageName: string }): Promise<void>;
  signEvent(options: {
    eventJson: string;
    eventId: string;
    npub: string;
  }): Promise<{ signature: string; id: string; event: string }>;
  nip04Encrypt(options: {
    plainText: string;
    pubKey: string;
    npub: string;
    id?: string;
  }): Promise<{ result: string; id: string }>;
  nip04Decrypt(options: {
    encryptedText: string;
    pubKey: string;
    npub: string;
    id?: string;
  }): Promise<{ result: string; id: string }>;
  nip44Encrypt(options: {
    plainText: string;
    pubKey: string;
    npub: string;
    id?: string;
  }): Promise<{ result: string; id: string }>;
  nip44Decrypt(options: {
    encryptedText: string;
    pubKey: string;
    npub: string;
    id?: string;
  }): Promise<{ result: string; id: string }>;
  decryptZapEvent(options: {
    eventJson: string;
    npub: string;
    id?: string;
  }): Promise<{ result: string; id: string }>;
}

export interface AppInfo {
  name: string;
  packageName: string;
  iconData: string;
  iconUrl: string;
}
