"use client";

import Link from "next/link";
import { useParams, usePathname } from "next/navigation";
import { useState } from "react";
import { LayoutDashboard, LogOut, Menu, Settings } from "lucide-react";
import { Logo } from "@/components/marketing/logo";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetTitle } from "@/components/ui/sheet";
import { BusinessProvider, useBusiness } from "@/components/app/business-context";
import { useSession } from "@/components/app/session-provider";
import { cn } from "@/lib/utils";

export type NavItem = { href: string; label: string; icon: React.ComponentType<{ className?: string }>; exact?: boolean };

/** Navigation entries; each module adds its entry here when its screen exists (no dead links). */
function navItems(base: string): NavItem[] {
  return [
    { href: base, label: "Overview", icon: LayoutDashboard, exact: true },
    { href: `${base}/settings`, label: "Business profile", icon: Settings },
  ];
}

export function AppShell({ children }: { children: React.ReactNode }) {
  const { businessId } = useParams<{ businessId: string }>();
  return (
    <BusinessProvider businessId={businessId}>
      <ShellFrame base={`/app/b/${businessId}`}>{children}</ShellFrame>
    </BusinessProvider>
  );
}

function ShellFrame({ base, children }: { base: string; children: React.ReactNode }) {
  const [open, setOpen] = useState(false);
  const { business } = useBusiness();
  return (
    <div className="flex min-h-svh flex-1">
      <aside className="sticky top-0 hidden h-svh w-60 shrink-0 border-r bg-sidebar md:block">
        <SidebarContent base={base} />
      </aside>
      <Sheet open={open} onOpenChange={setOpen}>
        <SheetContent side="left" className="w-64 p-0">
          <SheetTitle className="sr-only">Navigation</SheetTitle>
          <SidebarContent base={base} onNavigate={() => setOpen(false)} />
        </SheetContent>
      </Sheet>
      <div className="flex min-w-0 flex-1 flex-col">
        <header className="flex h-14 items-center gap-3 border-b px-4 md:hidden">
          <Button variant="ghost" size="icon" onClick={() => setOpen(true)} aria-label="Open navigation"><Menu /></Button>
          <span className="truncate font-medium">{business.name}</span>
        </header>
        <main className="flex-1">{children}</main>
      </div>
    </div>
  );
}

function SidebarContent({ base, onNavigate }: { base: string; onNavigate?: () => void }) {
  const pathname = usePathname();
  const { business } = useBusiness();
  const { user, signOut } = useSession();
  return (
    <div className="flex h-full flex-col">
      <div className="flex h-14 items-center border-b px-4"><Logo href="/app" /></div>
      <div className="border-b px-4 py-3">
        <p className="truncate text-sm font-medium">{business.name}</p>
        <p className="truncate text-xs text-muted-foreground">/{business.slug}</p>
      </div>
      <nav className="flex-1 space-y-0.5 p-2" aria-label="Main">
        {navItems(base).map((item) => {
          const active = item.exact ? pathname === item.href : pathname.startsWith(item.href);
          return (
            <Link
              key={item.href}
              href={item.href}
              onClick={onNavigate}
              aria-current={active ? "page" : undefined}
              className={cn(
                "flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm text-muted-foreground transition-colors hover:bg-muted hover:text-foreground",
                active && "bg-accent font-medium text-accent-foreground hover:bg-accent hover:text-accent-foreground",
              )}
            >
              <item.icon className="size-4" />
              {item.label}
            </Link>
          );
        })}
      </nav>
      <div className="border-t p-3">
        <p className="truncate px-1 text-sm font-medium">{user.fullName}</p>
        <p className="truncate px-1 text-xs text-muted-foreground">{user.email}</p>
        <Button variant="ghost" size="sm" className="mt-2 w-full justify-start" onClick={() => void signOut()}>
          <LogOut /> Sign out
        </Button>
      </div>
    </div>
  );
}
