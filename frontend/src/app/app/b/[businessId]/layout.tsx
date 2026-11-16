import { AppShell } from "@/components/app/app-shell";

export default function BusinessLayout({ children }: LayoutProps<"/app/b/[businessId]">) {
  return <AppShell>{children}</AppShell>;
}
