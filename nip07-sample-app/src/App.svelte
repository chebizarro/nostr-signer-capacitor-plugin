<script lang="ts">
	import { onMount } from 'svelte';
	import { Capacitor } from '@capacitor/core';
	import { NostrSignerPlugin } from 'nostr-signer-capacitor-plugin';
  
	let address = 'https://www.example.com';
	let iframeSrc = address;
  
	function loadPage() {
	  // Ensure the URL has a protocol
	  if (!address.startsWith('http')) {
		address = 'https://' + address;
	  }
	  iframeSrc = address;
	}
  
	onMount(async () => {
	  if (Capacitor.isNativePlatform()) {
		// Inject window.nostr
		await NostrSignerPlugin.injectNostr();
	  }
	});
  
	// Test function to get the public key
	async function getPublicKey() {
	  if (window.nostr) {
		try {
		  const publicKey = await window.nostr.getPublicKey();
		  alert('Public Key: ' + publicKey);
		} catch (error) {
		  console.error('Error getting public key:', error);
		}
	  } else {
		console.error('window.nostr is not available.');
	  }
	}
  </script>
  
  <style>
	/* Basic styling */
	#browser-container {
	  display: flex;
	  flex-direction: column;
	  height: 100vh;
	}
	#address-bar {
	  display: flex;
	}
	#address-input {
	  flex: 1;
	  padding: 8px;
	  font-size: 16px;
	}
	#go-button, #test-button {
	  padding: 8px;
	  font-size: 16px;
	}
	#webview {
	  flex: 1;
	  border: none;
	  width: 100%;
	}
  </style>
  
  <div id="browser-container">
	<div id="address-bar">
	  <input
		type="text"
		id="address-input"
		bind:value={address}
		placeholder="Enter URL"
		on:keyup={(e) => { if (e.key === 'Enter') loadPage(); }}
	  />
	  <button id="go-button" on:click={loadPage}>Go</button>
	  <button id="test-button" on:click={getPublicKey}>Get Public Key</button>
	</div>
	<iframe id="webview" src={iframeSrc} allow="clipboard-read; clipboard-write"></iframe>
  </div>
  