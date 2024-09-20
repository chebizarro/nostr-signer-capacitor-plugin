import { WebPlugin } from '@capacitor/core';

import type { NostrSignerPluginPlugin } from './definitions';

export class NostrSignerPluginWeb
  extends WebPlugin
  implements NostrSignerPluginPlugin
{
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
