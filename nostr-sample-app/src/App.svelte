<script lang="ts">
  import { NostrSignerPlugin } from 'nostr-signer-capacitor-plugin';
  import { Capacitor } from '@capacitor/core';
  import { onMount } from 'svelte';
  import { getEventHash, type UnsignedEvent } from 'nostr-tools';
  import { ShortTextNote } from 'nostr-tools/kinds';
  import * as nip19 from 'nostr-tools/nip19'

  let publicKey = '';
  let eventContent = '';
  let signedEvent = '';
  let encryptPubKey = '';
  let messageToEncrypt = '';
  let encryptedMessage = '';
  let messageToDecrypt = '';
  let decryptedMessage = '';
  let signerInstalled = false;
  let packageName = 'com.greenart7c3.nostrsigner'; // Default package name

  // Function to check if signer app is installed
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
    await checkSignerInstalled();
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
      };
	  let hash = getEventHash(event);
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
          npub: publicKey,
        });
        signedEvent = JSON.stringify(JSON.parse(signedEventJson), null, 2);
      }
    } catch (error) {
      console.error('Error signing event:', error);
    }
  }

  // Function to encrypt a message using NIP-04
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
        const { result } = await NostrSignerPlugin.nip04Encrypt({
          pubKey: data,
          plainText: messageToEncrypt,
		  npub: publicKey,
        });
        encryptedMessage = result;
      }
    } catch (error) {
      console.error('Error encrypting message:', error);
    }
  }

  // Function to decrypt a message using NIP-04
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
          messageToDecrypt,
        );
      } else {
        const { result } = await NostrSignerPlugin.nip04Decrypt({
          pubKey: encryptPubKey,
          encryptedText: messageToDecrypt,
        });
        decryptedMessage = result;
      }
    } catch (error) {
      console.error('Error decrypting message:', error);
    }
  }
</script>

<main>
  <div class="container">
    <h1>Nostr Sample App</h1>

    <!-- Input for the signer app package name -->
    <h2>Signer App Package Name</h2>
    <input
      type="text"
      bind:value={packageName}
      placeholder="Signer App Package Name"
    />
    <button on:click={checkSignerInstalled}>Check Signer Installation</button>

    {#if !signerInstalled}
      <p style="color: red;">
        Signer app is not installed. Please install it to proceed.
      </p>
	  {:else}
      <p style="color: green;">
        Signer app is installed.
      </p>
	  {/if}

    <hr />

    <button on:click={getPublicKey} disabled={!signerInstalled}
      >Get Public Key</button
    >
    {#if publicKey}
      <div class="output">
        <strong>Public Key:</strong>
        <pre>{publicKey}</pre>
      </div>
    {/if}

    <hr />

    <h2>Sign Event</h2>
    <textarea bind:value={eventContent} placeholder="Event Content"></textarea>
    <button on:click={signEvent} disabled={!signerInstalled}>Sign Event</button>
    {#if signedEvent}
      <div class="output">
        <strong>Signed Event:</strong>
        <pre>{signedEvent}</pre>
      </div>
    {/if}

    <hr />

    <h2>Encrypt Message (NIP-04)</h2>
    <input
      type="text"
      bind:value={encryptPubKey}
      placeholder="Recipient Public Key"
    />
    <textarea bind:value={messageToEncrypt} placeholder="Message to Encrypt"
    ></textarea>
    <button on:click={encryptMessage} disabled={!signerInstalled}
      >Encrypt Message</button
    >
    {#if encryptedMessage}
      <div class="output">
        <strong>Encrypted Message:</strong>
        <pre>{encryptedMessage}</pre>
      </div>
    {/if}

    <hr />

    <h2>Decrypt Message (NIP-04)</h2>
    <textarea bind:value={messageToDecrypt} placeholder="Encrypted Message"
    ></textarea>
    <button on:click={decryptMessage} disabled={!signerInstalled}
      >Decrypt Message</button
    >
    {#if decryptedMessage}
      <div class="output">
        <strong>Decrypted Message:</strong>
        <pre>{decryptedMessage}</pre>
      </div>
    {/if}
  </div>
</main>

<style>
  /* Simple styles for the UI */
  .container {
    max-width: 600px;
    margin: auto;
    padding: 1rem;
  }

  input,
  textarea {
    width: 100%;
    margin-bottom: 1rem;
  }

  button {
    margin-bottom: 1rem;
  }

  .output {
    background-color: #f0f0f0;
    padding: 1rem;
    white-space: pre-wrap;
  }
</style>
