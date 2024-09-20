import { registerPlugin } from '@capacitor/core';

import type { NostrSignerPluginPlugin } from './definitions';

const NostrSignerPlugin = registerPlugin<NostrSignerPluginPlugin>(
  'NostrSignerPlugin',
  {
    web: () => import('./web').then(m => new m.NostrSignerPluginWeb()),
  },
);

export * from './definitions';
export { NostrSignerPlugin };
