export interface NostrSignerPluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
