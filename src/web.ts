// web.ts
import { WebPlugin } from '@capacitor/core';

import type { NostrSignerPlugin } from './definitions';

export class NostrSignerPluginWeb
  extends WebPlugin
  implements NostrSignerPlugin
{
  async setPackageName(options: { packageName: string }): Promise<void> {
    // On web platforms, setting a package name is not applicable.
    console.warn('setPackageName is not applicable on web platform.');
  }

  async isExternalSignerInstalled(): Promise<{ installed: boolean }> {
    const installed = typeof window !== 'undefined' && typeof window.nostr !== 'undefined';
    return { installed };
  }

  async getPublicKey(): Promise<{ npub: string }> {
    if (this.isNostrAvailable() && typeof window.nostr.getPublicKey === 'function') {
      try {
        const publicKeyHex = await window.nostr.getPublicKey();
        return { npub: publicKeyHex };
      } catch (error) {
        throw this.convertError(error);
      }
    } else {
      throw this.unavailable('window.nostr.getPublicKey is not available');
    }
  }

  async signEvent(options: { eventJson: string }): Promise<{ event: string }> {
    if (this.isNostrAvailable() && typeof window.nostr.signEvent === 'function') {
      try {
        const event = JSON.parse(options.eventJson);
        const signedEvent = await window.nostr.signEvent(event);
        return { event: JSON.stringify(signedEvent) };
      } catch (error) {
        throw this.convertError(error);
      }
    } else {
      throw this.unavailable('window.nostr.signEvent is not available');
    }
  }

  async nip04Encrypt(options: { plainText: string; pubKey: string }): Promise<{ result: string }> {
    if (this.isNostrNip04Available() && typeof window.nostr.nip04.encrypt === 'function') {
      try {
        const result = await window.nostr.nip04.encrypt(options.pubKey, options.plainText);
        return { result };
      } catch (error) {
        throw this.convertError(error);
      }
    } else {
      throw this.unavailable('window.nostr.nip04.encrypt is not available');
    }
  }

  async nip04Decrypt(options: { encryptedText: string; pubKey: string }): Promise<{ result: string }> {
    if (this.isNostrNip04Available() && typeof window.nostr.nip04.decrypt === 'function') {
      try {
        const result = await window.nostr.nip04.decrypt(options.pubKey, options.encryptedText);
        return { result };
      } catch (error) {
        throw this.convertError(error);
      }
    } else {
      throw this.unavailable('window.nostr.nip04.decrypt is not available');
    }
  }

  async nip44Encrypt(options: { plainText: string; pubKey: string }): Promise<{ result: string }> {
    if (this.isNostrNip44Available() && typeof window.nostr.nip44.encrypt === 'function') {
      try {
        const result = await window.nostr.nip44.encrypt(options.pubKey, options.plainText);
        return { result };
      } catch (error) {
        throw this.convertError(error);
      }
    } else {
      throw this.unavailable('window.nostr.nip44.encrypt is not available');
    }
  }

  async nip44Decrypt(options: { encryptedText: string; pubKey: string }): Promise<{ result: string }> {
    if (this.isNostrNip44Available() && typeof window.nostr.nip44.decrypt === 'function') {
      try {
        const result = await window.nostr.nip44.decrypt(options.pubKey, options.encryptedText);
        return { result };
      } catch (error) {
        throw this.convertError(error);
      }
    } else {
      throw this.unavailable('window.nostr.nip44.decrypt is not available');
    }
  }

  async decryptZapEvent(options: { eventJson: string }): Promise<{ result: string }> {
    // NIP-07 does not specify decryptZapEvent, so we'll throw an error.
    throw this.unavailable('decryptZapEvent is not available on web platform');
  }

  // Helper methods to check availability

  private isNostrAvailable(): boolean {
    return typeof window !== 'undefined' && typeof window.nostr !== 'undefined';
  }

  private isNostrNip04Available(): boolean {
    return this.isNostrAvailable() && typeof window.nostr.nip04 !== 'undefined';
  }

  private isNostrNip44Available(): boolean {
    return this.isNostrAvailable() && typeof window.nostr.nip44 !== 'undefined';
  }

  // Helper method to convert errors
  private convertError(error: any): Error {
    if (error instanceof Error) {
      return error;
    } else if (typeof error === 'string') {
      return new Error(error);
    } else {
      return new Error('An unknown error occurred');
    }
  }
}
