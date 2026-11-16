import { cn } from "@/lib/utils";
import { LEAD_STATUSES, type LeadStatus } from "@/lib/types";

const STYLES: Record<LeadStatus, string> = {
  NEW: "bg-primary/10 text-primary",
  CONTACTED: "bg-sky-500/10 text-sky-700",
  QUALIFIED: "bg-violet-500/10 text-violet-700",
  QUOTED: "bg-amber-500/15 text-amber-800",
  CONVERTED: "bg-emerald-500/15 text-emerald-700",
  LOST: "bg-muted text-muted-foreground",
};

export function LeadStatusBadge({ status, className }: { status: LeadStatus; className?: string }) {
  return (
    <span className={cn("inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium", STYLES[status], className)}>
      {LEAD_STATUSES.find((s) => s.value === status)?.label}
    </span>
  );
}
