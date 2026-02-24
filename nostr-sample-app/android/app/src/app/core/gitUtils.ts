import type { TrustedEvent } from "@welshman/util";
import { Address } from "@welshman/util";
import { groupByEuc, type RepoGroup } from "@nostr-git/core";

// Thin shim that calls core grouping but normalizes inputs for tests
export function groupReposByEuc(events: TrustedEvent[]): RepoGroup[] {
  return groupByEuc(events as any);
}

// Resolve first announcement per EUC for navigation
export function firstAnnouncementByEuc(events: TrustedEvent[]): Map<string, TrustedEvent> {
  const map = new Map<string, TrustedEvent>();
  for (const ev of events) {
    const t = (ev.tags || []).find((t: string[]) => t[0] === "r" && t[2] === "euc");
    const euc = t ? t[1] : "";
    if (!euc) continue;
    if (!map.has(euc)) map.set(euc, ev);
  }
  return map;
}

// Determine inline alert flags for a repo card
export function computeRepoAlertFlags(args: {
  sendDelayMs?: number | null;
  headChangedEucs: Set<string>;
  euc: string;
}) {
  const hasGraspDelay = typeof args.sendDelayMs === "number" && args.sendDelayMs > 0;
  const headChanged = args.headChangedEucs.has(args.euc);
  return { hasGraspDelay, headChanged };
}

// Helper for generating an naddr path for navigation (string only; leaves routing to caller)
export function toNaddrPath(relayParam: string, ev: TrustedEvent | undefined): string | undefined {
  if (!ev) return undefined;
  const naddr = Address.fromEvent(ev as any).toString();
  return `/spaces/${relayParam}/git/${naddr}`;
}
