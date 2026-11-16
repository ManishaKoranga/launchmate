import Link from "next/link";

export default function SiteNotFound() {
  return (
    <div className="grid min-h-svh place-items-center px-4 text-center">
      <div>
        <h1 className="text-2xl font-semibold">This website isn&apos;t available</h1>
        <p className="mt-2 text-muted-foreground">It may not be published yet, or the address may be wrong.</p>
        <Link href="/" className="mt-6 inline-block text-sm font-medium text-primary hover:underline">Go to LaunchMate</Link>
      </div>
    </div>
  );
}
