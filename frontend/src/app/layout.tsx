import type { Metadata } from "next";
import { Figtree, Newsreader } from "next/font/google";
import "./globals.css";
import { AuthProvider } from "@/context/AuthContext";

const serif = Newsreader({ subsets: ["latin"], variable: "--font-serif" });
const sans = Figtree({ subsets: ["latin"], variable: "--font-sans" });

export const metadata: Metadata = {
  title: "MediBridge — private visit coordination",
  description:
    "Privacy-first appointment coordination. Educational software — not a medical device, not a doctor, and not clinical advice.",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className={`${serif.variable} ${sans.variable} font-sans antialiased`}>
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}
