<script lang="ts">
  import { Badge } from "../ui/badge";
  import { AlertTriangle, GitBranch } from "@lucide/svelte";
  const { hasGraspDelay = false, headChanged = false } = $props<{
    hasGraspDelay?: boolean;
    headChanged?: boolean;
  }>();
</script>

<div class="flex items-center gap-2">
  {#if headChanged}
    <Badge title="HEAD changed in this session" class="bg-amber-100 text-amber-800 border-amber-200">
      <GitBranch class="h-3 w-3 mr-1" /> HEAD switched
    </Badge>
  {/if}
  {#if hasGraspDelay}
    <Badge title="GRASP push delay is enabled" class="bg-sky-100 text-sky-800 border-sky-200">
      <AlertTriangle class="h-3 w-3 mr-1" /> GRASP delay
    </Badge>
  {/if}
</div>
