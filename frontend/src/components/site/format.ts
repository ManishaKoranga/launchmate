import type { DailyHours, DayOfWeek } from "@/lib/types";

const inr = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 2, minimumFractionDigits: 0 });

export function formatPrice(price?: number | null): string | null {
  return price == null ? null : inr.format(price);
}

/** WhatsApp click-to-chat link; assumes Indian numbers when no country code is given. */
export function whatsappLink(number: string, text?: string): string {
  let digits = number.replace(/\D/g, "");
  if (digits.length === 10) digits = `91${digits}`;
  const q = text ? `?text=${encodeURIComponent(text)}` : "";
  return `https://wa.me/${digits}${q}`;
}

export function telLink(number: string): string {
  return `tel:${number.replace(/[^+\d]/g, "")}`;
}

const DAY_LABELS: Record<DayOfWeek, string> = {
  MONDAY: "Monday",
  TUESDAY: "Tuesday",
  WEDNESDAY: "Wednesday",
  THURSDAY: "Thursday",
  FRIDAY: "Friday",
  SATURDAY: "Saturday",
  SUNDAY: "Sunday",
};
const ORDER: DayOfWeek[] = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];

function to12h(t?: string) {
  if (!t) return "";
  const [h, m] = t.split(":").map(Number);
  const suffix = h >= 12 ? "pm" : "am";
  const hh = h % 12 === 0 ? 12 : h % 12;
  return m === 0 ? `${hh} ${suffix}` : `${hh}:${String(m).padStart(2, "0")} ${suffix}`;
}

export function hoursRows(hours: DailyHours[]): { day: string; value: string }[] {
  return ORDER.map((d) => hours.find((h) => h.day === d))
    .filter((h): h is DailyHours => Boolean(h))
    .map((h) => ({ day: DAY_LABELS[h.day], value: h.closed ? "Closed" : `${to12h(h.opens)} – ${to12h(h.closes)}` }));
}
