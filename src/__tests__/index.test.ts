import { NostrSignerPlugin, buildPermissionsJson } from '../index';

// Mock Capacitor core
jest.mock('@capacitor/core', () => {
  const actual = jest.requireActual('@capacitor/core');
  return {
    ...actual,
    Capacitor: { getPlatform: jest.fn(() => 'android') },
    registerPlugin: jest.fn(() => ({
      setPackageName: jest.fn(async () => {}),
      isExternalSignerInstalled: jest.fn(async () => ({ installed: true })),
      getInstalledSignerApps: jest.fn(async () => ({ apps: [{ name: 'Signer', packageName: 'com.signer', iconUrl: 'data:image/png;base64,x' }] })),
      getPublicKey: jest.fn(async () => ({ npub: 'npub1...', package: 'com.signer' })),
      signEvent: jest.fn(async () => ({ signature: 'sig', id: '1', event: '{"k":1}' })),
      nip04Encrypt: jest.fn(async () => ({ result: 'enc', id: '1' })),
      nip04Decrypt: jest.fn(async () => ({ result: 'dec', id: '1' })),
      nip44Encrypt: jest.fn(async () => ({ result: 'enc44', id: '1' })),
      nip44Decrypt: jest.fn(async () => ({ result: 'dec44', id: '1' })),
      decryptZapEvent: jest.fn(async () => ({ result: '{"ok":true}', id: '1' })),
    })),
  };
});

describe('TS bridge', () => {
  it('buildPermissionsJson serializes array', () => {
    const json = buildPermissionsJson([{ type: 'get_public_key' }]);
    expect(json).toBe('[{"type":"get_public_key"}]');
  });

  it('getPublicKey serializes permissions when array', async () => {
    await expect(NostrSignerPlugin.getPublicKey('com.signer', [{ type: 'get_public_key' }])).resolves.toEqual({ npub: 'npub1...', package: 'com.signer' });
  });

  it('signEvent passes through result', async () => {
    const res = await NostrSignerPlugin.signEvent('com.signer', '{"k":1}', '1', 'npub');
    expect(res).toEqual({ signature: 'sig', id: '1', event: '{"k":1}' });
  });

  it('Android-only guard rejects on non-android', async () => {
    const core = require('@capacitor/core');
    core.Capacitor.getPlatform.mockReturnValue('web');
    await expect(NostrSignerPlugin.getInstalledSignerApps()).rejects.toThrowError();
    core.Capacitor.getPlatform.mockReturnValue('android');
  });
});
