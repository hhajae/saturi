"use client"

import Link from "next/link";
import Image from "next/image";
import { Inter } from "next/font/google";
import { ReactNode } from "react";
import "@/styles/globals.css";
import Header from "@/components/layout/Header";
import Footer from "@/components/layout/Footer";
import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { authToken } from "@/utils/authutils";

const inter = Inter({ subsets: ["latin"] });

export default function RootLayout({children}: Readonly<{children: React.ReactNode}>) {
  const router = useRouter()
  useEffect(() => {
    if (typeof window !== "undefined" && sessionStorage.getItem("accessToken")) {
      authToken(router)
    }
  },[])
  return (
    <html lang="ko" className={inter.className}>
    <body>
      <Header />
        <main>{children}</main>
      <Footer />
    </body>
  </html>
  );
}
