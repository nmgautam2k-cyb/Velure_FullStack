import type { Metadata } from "next";
import "./globals.css";
import { AuthProvider } from "../lib/AuthContext";

export const metadata: Metadata = {
  title: "Velure — Premium Apparel",
  description: "Shop shirts, t-shirts and pants at Velure, your premium apparel destination.",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>
        <AuthProvider>
          {children}
        </AuthProvider>
      </body>
    </html>
  );
}
