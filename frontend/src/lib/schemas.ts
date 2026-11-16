import { z } from "zod";

/** Client-side mirror of the backend BusinessProfile rules (the backend remains authoritative). */
const phone = z
  .string()
  .trim()
  .regex(/^[+0-9 ()-]{6,20}$/, "Enter a valid phone number")
  .or(z.literal(""))
  .optional();

export const businessProfileSchema = z.object({
  name: z.string().trim().min(1, "Business name is required").max(120),
  category: z.enum([
    "BAKERY_FOOD",
    "BEAUTY_SALON",
    "HOME_SERVICES",
    "EDUCATION_TUTORING",
    "HANDMADE_RETAIL",
    "PROFESSIONAL_SERVICES",
  ]),
  tagline: z.string().max(200).optional(),
  description: z.string().max(4000).optional(),
  city: z.string().max(80).optional(),
  area: z.string().max(120).optional(),
  address: z.string().max(300).optional(),
  phone,
  whatsapp: phone,
  email: z.email("Enter a valid email").or(z.literal("")).optional(),
  targetCustomers: z.string().max(300).optional(),
  languages: z.string().max(120).optional(),
});

export type BusinessProfileValues = z.infer<typeof businessProfileSchema>;

/** Converts empty strings to undefined so optional fields are stored as null. */
export function compact<T extends Record<string, unknown>>(values: T): T {
  return Object.fromEntries(
    Object.entries(values).map(([k, v]) => [k, typeof v === "string" && v.trim() === "" ? undefined : v]),
  ) as T;
}
