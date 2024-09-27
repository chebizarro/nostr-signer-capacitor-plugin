import { registerPlugin } from '@capacitor/core';

import { NostrSignerPlugin, AppInfo } from './definitions';

const NostrSignerPlugin = registerPlugin<NostrSignerPlugin>(
  'NostrSignerPlugin',
);

export * from './definitions';
export { NostrSignerPlugin, AppInfo };
