if (!window.nostr) {
  window.nostr = {
	setPackageName: async function (packageName) {
		Capacitor.Plugins.NostrSignerPlugin.setPackageName(packageName);
	},
    getPublicKey: async function () {
      return Capacitor.Plugins.NostrSignerPlugin.getPublicKey().then(
        result => result.npub,
      );
    },
    signEvent: async function (event) {
      return NostrSignerPlugin.signEvent({
        eventJson: JSON.stringify(event),
      }).then(result => {
        return JSON.parse(result.event);
      });
    },
    nip04: {
      encrypt: async function (pubkey, plaintext) {
        return NostrSignerPlugin.nip04Encrypt({
          pubKey: pubkey,
          plainText: plaintext,
        }).then(result => result.result);
      },
      decrypt: async function (pubkey, ciphertext) {
        return NostrSignerPlugin.nip04Decrypt({
          pubKey: pubkey,
          encryptedText: ciphertext,
        }).then(result => result.result);
      },
    },
    nip44: {
      encrypt: async function (pubkey, plaintext) {
        return NostrSignerPlugin.nip44Encrypt({
          pubKey: pubkey,
          plainText: plaintext,
        }).then(result => result.result);
      },
      decrypt: async function (pubkey, ciphertext) {
        return NostrSignerPlugin.nip44Decrypt({
          pubKey: pubkey,
          encryptedText: ciphertext,
        }).then(result => result.result);
      },
    },
    getRelays: async function () {
      return {};
    },
  };
}
