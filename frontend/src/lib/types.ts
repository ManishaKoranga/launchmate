export type User = { id: string; email: string; fullName: string };

export type BusinessCategory =
  | "BAKERY_FOOD"
  | "BEAUTY_SALON"
  | "HOME_SERVICES"
  | "EDUCATION_TUTORING"
  | "HANDMADE_RETAIL"
  | "PROFESSIONAL_SERVICES";

export const CATEGORY_LABELS: Record<BusinessCategory, string> = {
  BAKERY_FOOD: "Bakery & food",
  BEAUTY_SALON: "Beauty & salon",
  HOME_SERVICES: "Home services",
  EDUCATION_TUTORING: "Education & tutoring",
  HANDMADE_RETAIL: "Handmade & retail",
  PROFESSIONAL_SERVICES: "Freelance & professional",
};

export type SetupStage = "PROFILE_DRAFT" | "PROFILE_CONFIRMED" | "WEBSITE_DRAFTED" | "PUBLISHED";

export type DayOfWeek = "MONDAY" | "TUESDAY" | "WEDNESDAY" | "THURSDAY" | "FRIDAY" | "SATURDAY" | "SUNDAY";

export type DailyHours = { day: DayOfWeek; closed: boolean; opens?: string; closes?: string };

export type BusinessProfile = {
  name: string;
  category: BusinessCategory;
  tagline?: string;
  description?: string;
  city?: string;
  area?: string;
  address?: string;
  phone?: string;
  whatsapp?: string;
  email?: string;
  targetCustomers?: string;
  languages?: string;
  openingHours?: DailyHours[];
};

export type Business = BusinessProfile & {
  id: string;
  slug: string;
  plan: "FREE" | "STARTER" | "GROWTH" | "BUSINESS";
  setupStage: SetupStage;
  createdAt: string;
};

export type BusinessSummary = Pick<Business, "id" | "slug" | "name" | "category" | "setupStage">;
