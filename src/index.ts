import { registerPlugin } from '@capacitor/core';

import { NostrSignerPlugin } from './definitions';

const NostrSignerPlugin = registerPlugin<NostrSignerPlugin>(
  'NostrSignerPlugin',
  {
    web: () => import('./web').then(m => new m.NostrSignerPluginWeb()),
  },
);

export * from './definitions';
export { NostrSignerPlugin };
