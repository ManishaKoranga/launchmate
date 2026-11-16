import { SessionProvider } from "@/components/app/session-provider";

export default function AppLayout({ children }: LayoutProps<"/app">) {
  return <SessionProvider>{children}</SessionProvider>;
}
