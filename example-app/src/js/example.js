import { NostrSignerPlugin } from 'nostr-signer-capacitor-plugin';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    NostrSignerPlugin.echo({ value: inputValue })
}
