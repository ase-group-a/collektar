import type { Metadata } from "next";
import "./globals.css";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import {AuthProvider} from "@/lib/auth/AuthProvider";
import React from "react";

export const metadata: Metadata = {
  title: "Collektar",
  description: "Collect, organize, and manage your favorite media in one place.",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode; }>) {
  return (
    <html lang="en" data-theme="dark">
        <body className="min-h-screen flex flex-col">
            <AuthProvider>
              <Header />
                <main className="flex-1">{children}</main>
              <Footer />
            </AuthProvider>
        </body>
    </html>
  );
}
