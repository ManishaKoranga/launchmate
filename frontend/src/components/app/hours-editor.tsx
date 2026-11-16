"use client";

import { useState } from "react";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Switch } from "@/components/ui/switch";
import { api, errorMessage } from "@/lib/api";
import type { Business, DailyHours, DayOfWeek } from "@/lib/types";

const DAYS: { day: DayOfWeek; label: string }[] = [
  { day: "MONDAY", label: "Monday" },
  { day: "TUESDAY", label: "Tuesday" },
  { day: "WEDNESDAY", label: "Wednesday" },
  { day: "THURSDAY", label: "Thursday" },
  { day: "FRIDAY", label: "Friday" },
  { day: "SATURDAY", label: "Saturday" },
  { day: "SUNDAY", label: "Sunday" },
];

export function HoursEditor({ business, onSaved }: { business: Business; onSaved: (b: Business) => void }) {
  const [hours, setHours] = useState<DailyHours[]>(() =>
    DAYS.map(({ day }) => business.openingHours?.find((h) => h.day === day) ?? { day, closed: false, opens: "10:00", closes: "20:00" }),
  );
  const [saving, setSaving] = useState(false);

  const update = (day: DayOfWeek, patch: Partial<DailyHours>) =>
    setHours((hs) => hs.map((h) => (h.day === day ? { ...h, ...patch } : h)));

  async function save() {
    setSaving(true);
    try {
      const body = hours.map((h) => (h.closed ? { day: h.day, closed: true } : h));
      onSaved(await api<Business>(`/api/businesses/${business.id}/hours`, { method: "PUT", body: { openingHours: body } }));
      toast.success("Opening hours saved");
    } catch (e) {
      toast.error(errorMessage(e));
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="grid gap-3">
      {DAYS.map(({ day, label }) => {
        const h = hours.find((x) => x.day === day)!;
        return (
          <div key={day} className="grid grid-cols-[6.5rem_auto_1fr] items-center gap-3 text-sm">
            <span className="font-medium">{label}</span>
            <label className="flex items-center gap-2 text-muted-foreground">
              <Switch checked={!h.closed} onCheckedChange={(open) => update(day, { closed: !open })} aria-label={`${label} open`} />
              <span className="w-12">{h.closed ? "Closed" : "Open"}</span>
            </label>
            {!h.closed && (
              <div className="flex items-center gap-2">
                <Input type="time" className="w-28" value={h.opens ?? ""} onChange={(e) => update(day, { opens: e.target.value })} aria-label={`${label} opens`} />
                <span className="text-muted-foreground">to</span>
                <Input type="time" className="w-28" value={h.closes ?? ""} onChange={(e) => update(day, { closes: e.target.value })} aria-label={`${label} closes`} />
              </div>
            )}
          </div>
        );
      })}
      <div className="mt-2">
        <Button onClick={() => void save()} disabled={saving}>{saving && <Loader2 className="animate-spin" />} Save hours</Button>
      </div>
    </div>
  );
}
