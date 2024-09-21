export interface NostrSignerPlugin {
	setPackageName(options: { packageName: string }): Promise<void>;
  
	isExternalSignerInstalled(): Promise<{ installed: boolean }>;
  
	getPublicKey(): Promise<{ npub: string }>;
  
	signEvent(options: { eventJson: string }): Promise<{ event: string }>;
  
	nip04Encrypt(options: { plainText: string; pubKey: string }): Promise<{ result: string }>;
  
	nip04Decrypt(options: { encryptedText: string; pubKey: string }): Promise<{ result: string }>;
  
	nip44Encrypt(options: { plainText: string; pubKey: string }): Promise<{ result: string }>;
  
	nip44Decrypt(options: { encryptedText: string; pubKey: string }): Promise<{ result: string }>;
  
	decryptZapEvent(options: { eventJson: string }): Promise<{ result: string }>; // May not be available on web
  }
  