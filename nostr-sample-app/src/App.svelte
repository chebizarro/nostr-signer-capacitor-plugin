<script lang="ts">
  import { NostrSignerPlugin, type AppInfo } from 'nostr-signer-capacitor-plugin';
  import { Capacitor } from '@capacitor/core';
  import { onMount } from 'svelte';
  import { getEventHash, type UnsignedEvent } from 'nostr-tools';
  import * as nip19 from 'nostr-tools/nip19';

  let publicKey = '';
  let eventContent = '';
  let signedEvent = '';
  let encryptPubKey = '';
  let messageToEncrypt = '';
  let encryptedMessage = '';
  let decryptedMessage = '';
  let signerInstalled = false;
  let packageName = '';
  let signerApps: AppInfo[] = [];
  let isScriptActive: boolean = false;

  function selectSignerApp(app: AppInfo) {
    // Store the selected app's package name
    packageName = app.packageName;
    // Proceed to use this package name when invoking other plugin methods
  }

  // Get the installed signer apps
  async function getSignerApps() {
    try {
      const result = await NostrSignerPlugin.getInstalledSignerApps();
      signerApps = result.apps;
      console.log('Installed Signer Apps:', signerApps);
    } catch (error) {
      console.error('Error getting installed signer apps:', error);
    }
  }

  // Check if signer app is installed
  async function checkSignerInstalled() {
    if (Capacitor.getPlatform() === 'android') {
      try {
        await NostrSignerPlugin.setPackageName({ packageName });
        const { installed } =
          await NostrSignerPlugin.isExternalSignerInstalled();
        signerInstalled = installed;
      } catch (error) {
        console.error('Error checking signer installation:', error);
        signerInstalled = false;
      }
    } else {
      signerInstalled = true; // Assume true for web (NIP-07)
    }
  }

  // Call checkSignerInstalled when the component mounts
  onMount(async () => {
    await getSignerApps();
  });

  // Watch for changes in packageName and update signerInstalled accordingly
  $: if (packageName) {
    checkSignerInstalled();
  }

  // Function to get the public key
  async function getPublicKey() {
    try {
      if (
        Capacitor.getPlatform() === 'web' &&
        window.nostr &&
        window.nostr.getPublicKey
      ) {
        publicKey = await window.nostr.getPublicKey();
      } else {
        const { npub } = await NostrSignerPlugin.getPublicKey();
        publicKey = npub;
      }
    } catch (error) {
      console.error('Error getting public key:', error);
    }
  }

  // Function to sign an event
  async function signEvent() {
    try {
      let { data } = nip19.decode(publicKey);
      const event = {
        kind: 1,
        created_at: Math.round(Date.now() / 1000),
        content: eventContent,
        tags: [],
        pubkey: data,
        id: '',
        sig: '',
      };
      let hash = getEventHash(event);
      event.id = hash;
      if (
        Capacitor.getPlatform() === 'web' &&
        window.nostr &&
        window.nostr.signEvent
      ) {
        signedEvent = JSON.stringify(
          await window.nostr.signEvent(event),
          null,
          2,
        );
      } else {
        const { event: signedEventJson } = await NostrSignerPlugin.signEvent({
          eventJson: JSON.stringify(event),
          eventId: hash,
          npub: data,
        });
        signedEvent = JSON.stringify(JSON.parse(signedEventJson), null, 2);
      }
    } catch (error) {
      console.error('Error signing event:', error);
    }
  }

  // Function to encrypt a message
  async function encryptMessage() {
    try {
      if (
        Capacitor.getPlatform() === 'web' &&
        window.nostr &&
        window.nostr.nip04 &&
        window.nostr.nip04.encrypt
      ) {
        encryptedMessage = await window.nostr.nip04.encrypt(
          encryptPubKey,
          messageToEncrypt,
        );
      } else {
        let { data } = nip19.decode(encryptPubKey);
        if (isScriptActive) {
          const { result } = await NostrSignerPlugin.nip44Encrypt({
            plainText: messageToEncrypt,
            pubKey: data,
            npub: publicKey,
          });
          encryptedMessage = result;
        } else {
          const { result } = await NostrSignerPlugin.nip04Encrypt({
            plainText: messageToEncrypt,
            pubKey: data,
            npub: publicKey,
          });
          encryptedMessage = result;
        }
      }
    } catch (error) {
      console.error('Error encrypting message:', error);
    }
  }

  // Function to decrypt a message
  async function decryptMessage() {
    try {
      if (
        Capacitor.getPlatform() === 'web' &&
        window.nostr &&
        window.nostr.nip04 &&
        window.nostr.nip04.decrypt
      ) {
        decryptedMessage = await window.nostr.nip04.decrypt(
          encryptPubKey,
          encryptedMessage,
        );
      } else {
        let { data } = nip19.decode(encryptPubKey);
        if (isScriptActive) {
          const { result } = await NostrSignerPlugin.nip44Decrypt({
            pubKey: data,
            npub: publicKey,
            encryptedText: encryptedMessage,
          });
          decryptedMessage = result;
        } else {
          const { result } = await NostrSignerPlugin.nip04Decrypt({
            pubKey: data,
            npub: publicKey,
            encryptedText: encryptedMessage,
          });
          decryptedMessage = result;
        }
      }
    } catch (error) {
      console.error('Error decrypting message:', error);
    }
  }
</script>

<main>
  <div class="container">
    <h2>Nostr Signer Demo</h2>

    <h2>Installed Signers</h2>
    {#if signerApps.length > 0}
      <ul>
        {#each signerApps as app}
          <li>
            <button class="app-chooser" on:click={() => selectSignerApp(app)}>
              <img src={app.iconUrl} alt={app.name} width="48" height="48" />
              <span>{app.name}</span>
            </button>
          </li>
        {/each}
      </ul>
    {:else}
      <p>No signer apps installed.</p>
    {/if}

    <hr />

    <button on:click={getPublicKey} disabled={!signerInstalled}
      >Get Public Key</button
    >
    {#if publicKey}
      <div class="output">
        <strong>Public Key:</strong>
        <p class="text-element">{publicKey}</p>
      </div>
    {/if}

    <hr />

    <h2>Sign Event</h2>
    <textarea bind:value={eventContent} placeholder="Event Content"></textarea>
    <button on:click={signEvent} disabled={!signerInstalled}>Sign Event</button>
    {#if signedEvent}
      <div class="output">
        <strong>Signed Event:</strong>
        <p class="text-element">{signedEvent}</p>
      </div>
    {/if}

    <hr />

    <h2>Encryption</h2>
    <input
      type="text"
      bind:value={encryptPubKey}
      placeholder="Recipient Public Key"
    />
    <textarea bind:value={messageToEncrypt} placeholder="Message to Encrypt"
    ></textarea>
    <div class="toggle-container">
      <label class="switch">
        <input type="checkbox" bind:checked={isScriptActive} />
        <span class="slider"></span>
      </label>
      <span class="toggle-text">{isScriptActive ? 'NIP-44' : 'NIP-04'}</span>
    </div>

    <div class="button-container">
      <button on:click={encryptMessage} disabled={!signerInstalled}
        >Encrypt Message</button
      >
      <button on:click={decryptMessage} disabled={!signerInstalled}
        >Decrypt Message</button
      >
    </div>
    {#if encryptedMessage}
      <div class="output">
        <strong>Encrypted Message:</strong>
        <p class="text-element">{encryptedMessage}</p>
      </div>
    {/if}
    {#if decryptedMessage}
      <div class="output">
        <strong>Decrypted Message:</strong>
        <p class="text-element">{decryptedMessage}</p>
      </div>
    {/if}
  </div>
</main>

<style>
  ul {
    list-style-type: none;
    padding: 0;
    margin: 0;
  }

  li {
    display: flex;
    flex-direction: column; /* Stack the children vertically */
    align-items: center; /* Center the children horizontally */
    cursor: pointer;
    margin-bottom: 20px;
  }

  img {
    width: 80px; /* Adjust the size as needed */
    height: 80px;
    margin-bottom: 10px; /* Space between the image and the label */
  }

  span {
    font-size: 16px;
    text-align: center; /* Center the text */
  }

  .app-chooser {
    display: flex;
    flex-direction: column; /* Stack the children vertically */
    align-items: center; /* Center the children horizontally */
  }

  input,
  textarea {
    width: 100%;
    margin-bottom: 1rem;
  }

  button {
    margin-bottom: 1rem;
  }

  .button-container {
    display: flex; /* Use flexbox to align buttons side by side */
    gap: 10px; /* Adds space between the buttons */
  }

  .output {
    background-color: #f0f0f0;
    padding: 1rem;
    white-space: pre-wrap;
  }

  .toggle-container {
    display: flex;
    align-items: center;
    gap: 10px;
    padding-bottom: 10px;
  }

  /* Toggle Switch - slider style */
  .switch {
    position: relative;
    display: inline-block;
    width: 60px;
    height: 34px;
  }

  /* Hide default checkbox */
  .switch input {
    opacity: 0;
    width: 0;
    height: 0;
  }

  /* The slider */
  .slider {
    position: absolute;
    cursor: pointer;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: #ccc;
    transition: 0.4s;
    border-radius: 34px;
  }

  .slider:before {
    position: absolute;
    content: '';
    height: 26px;
    width: 26px;
    left: 4px;
    bottom: 4px;
    background-color: white;
    transition: 0.4s;
    border-radius: 50%;
  }

  /* When checked, slide the toggle */
  input:checked + .slider {
    background-color: #0f81e8;
  }

  input:checked + .slider:before {
    transform: translateX(26px);
  }

  /* Style for the text label */
  .toggle-text {
    margin-left: 10px;
    font-size: 1.2rem;
  }
</style>
