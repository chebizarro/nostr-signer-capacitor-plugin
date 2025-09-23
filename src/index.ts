import { Capacitor, registerPlugin } from '@capacitor/core';
import type {
  NostrSignerNative,
  Permission,
  SignerAppInfo as AppInfo,
} from './definitions';

const native = registerPlugin<NostrSignerNative>('NostrSignerPlugin');

const ANDROID_ONLY = 'ANDROID_ONLY';

const ensureAndroid = () => {
  if (Capacitor.getPlatform() !== 'android') {
    const err = new Error('nostr-capacitor is Android-only. Use a NIP-07 provider on Web/iOS.');
    // @ts-expect-error attach code
    err.code = ANDROID_ONLY;
    throw err;
  }
};

export const buildPermissionsJson = (perms: Permission[]) =>
  JSON.stringify(perms ?? []);

function normalizePermissions(permissions?: Permission[] | string): string | undefined {
  if (permissions == null) return undefined;
  if (typeof permissions === 'string') return permissions;
  return buildPermissionsJson(permissions);
}

export const NostrSignerPlugin = {
  async setPackageName(packageName: string): Promise<void> {
    ensureAndroid();
    if (!packageName) throw new Error('MISSING_PARAMS: packageName');
    await native.setPackageName({ packageName });
  },

  async isExternalSignerInstalled(packageName?: string): Promise<{ installed: boolean }> {
    ensureAndroid();
    return native.isExternalSignerInstalled({ packageName });
  },

  async getInstalledSignerApps(): Promise<{ apps: AppInfo[] }> {
    ensureAndroid();
    return native.getInstalledSignerApps();
  },

  async getPublicKey(
    packageName?: string,
    permissions?: Permission[] | string,
  ): Promise<{ npub: string; package: string }> {
    ensureAndroid();
    const perm = normalizePermissions(permissions);
    return native.getPublicKey({ packageName, permissions: perm });
  },

  async signEvent(
    packageName: string,
    eventJson: string,
    id: string,
    npub: string,
  ): Promise<{ signature: string; id: string; event: string }> {
    ensureAndroid();
    if (!eventJson || !id || !npub) {
      throw new Error('MISSING_PARAMS: eventJson,id,npub');
    }
    return native.signEvent({ packageName, eventJson, eventId: id, npub });
  },

  async nip04Encrypt(
    packageName: string,
    plainText: string,
    id: string,
    pubKey: string,
    npub: string,
  ): Promise<{ result: string; id: string }> {
    ensureAndroid();
    if (!plainText || !pubKey || !npub) {
      throw new Error('MISSING_PARAMS: plainText,pubKey,npub');
    }
    return native.nip04Encrypt({ packageName, plainText, pubKey, npub, id });
  },

  async nip04Decrypt(
    packageName: string,
    encryptedText: string,
    id: string,
    pubKey: string,
    npub: string,
  ): Promise<{ result: string; id: string }> {
    ensureAndroid();
    if (!encryptedText || !pubKey || !npub) {
      throw new Error('MISSING_PARAMS: encryptedText,pubKey,npub');
    }
    return native.nip04Decrypt({ packageName, encryptedText, pubKey, npub, id });
  },

  async nip44Encrypt(
    packageName: string,
    plainText: string,
    id: string,
    pubKey: string,
    npub: string,
  ): Promise<{ result: string; id: string }> {
    ensureAndroid();
    if (!plainText || !pubKey || !npub) {
      throw new Error('MISSING_PARAMS: plainText,pubKey,npub');
    }
    return native.nip44Encrypt({ packageName, plainText, pubKey, npub, id });
  },

  async nip44Decrypt(
    packageName: string,
    encryptedText: string,
    id: string,
    pubKey: string,
    npub: string,
  ): Promise<{ result: string; id: string }> {
    ensureAndroid();
    if (!encryptedText || !pubKey || !npub) {
      throw new Error('MISSING_PARAMS: encryptedText,pubKey,npub');
    }
    return native.nip44Decrypt({ packageName, encryptedText, pubKey, npub, id });
  },

  async decryptZapEvent(
    packageName: string,
    eventJson: string,
    id: string,
    npub: string,
  ): Promise<{ result: string; id: string }> {
    ensureAndroid();
    if (!eventJson || !npub) {
      throw new Error('MISSING_PARAMS: eventJson,npub');
    }
    return native.decryptZapEvent({ packageName, eventJson, npub, id });
  },

  async getRelays(
    packageName: string,
    id: string,
    npub: string,
  ): Promise<{ result: string; id: string }> {
    ensureAndroid();
    if (!id || !npub) {
      throw new Error('MISSING_PARAMS: id,npub');
    }
    return native.getRelays({ packageName, id, npub });
  },
};

export type { AppInfo, Permission };
