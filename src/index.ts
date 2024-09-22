import { registerPlugin } from '@capacitor/core';

import { NostrSignerPlugin } from './definitions';

const NostrSignerPlugin = registerPlugin<NostrSignerPlugin>(
  'NostrSignerPlugin',
);

export * from './definitions';
export { NostrSignerPlugin };
