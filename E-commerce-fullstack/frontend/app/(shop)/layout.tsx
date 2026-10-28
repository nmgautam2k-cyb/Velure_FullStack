"use client";

import { usePathname } from "next/navigation";
import Navbar from "../components/Navbar";

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const pathname = usePathname();

  return (
    <>
      {/* ❌ Hide navbar in admin */}
      {!pathname.startsWith("/admin") && <Navbar />}
      
      {children}
    </>
  );
}