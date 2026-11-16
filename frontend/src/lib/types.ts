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

export type Product = {
  id: string;
  kind: "PRODUCT" | "SERVICE";
  name: string;
  description?: string | null;
  price?: number | null;
  priceNote?: string | null;
  category?: string | null;
  imageUrl?: string | null;
  visible: boolean;
  sortOrder: number;
  updatedAt: string;
};

export type Page<T> = { items: T[]; page: number; size: number; totalItems: number; totalPages: number };

export type LeadStatus = "NEW" | "CONTACTED" | "QUALIFIED" | "QUOTED" | "CONVERTED" | "LOST";

export const LEAD_STATUSES: { value: LeadStatus; label: string }[] = [
  { value: "NEW", label: "New" },
  { value: "CONTACTED", label: "Contacted" },
  { value: "QUALIFIED", label: "Qualified" },
  { value: "QUOTED", label: "Quoted" },
  { value: "CONVERTED", label: "Converted" },
  { value: "LOST", label: "Lost" },
];

export type Lead = {
  id: string;
  customerName: string;
  phone?: string | null;
  email?: string | null;
  message: string;
  productId?: string | null;
  productName?: string | null;
  preferredDate?: string | null;
  budget?: string | null;
  source: "WEBSITE" | "MANUAL";
  status: LeadStatus;
  notes?: string | null;
  quoteDraft?: string | null;
  statusChangedAt: string;
  createdAt: string;
};

export type LeadSummary = { total: number; lastSevenDays: number; byStatus: Record<LeadStatus, number> };
