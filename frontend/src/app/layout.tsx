"use client"

import "@/styles/globals.css";
import Link from "next/link";
import Image from "next/image";
// import type { Metadata } from "next"
import { Inter } from "next/font/google";
import { Button } from "@nextui-org/react";
import Providers from "./providers";
import { usePathname } from "next/navigation";


const inter = Inter({ subsets: ["latin"] });

// export const metadata: Metadata = {
//   title: "사투리가 서툴러유",
//   description: "Generated by create next app",
// };

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const pathname = usePathname(); // 현재 경로 가져오기

  // 특정 경로에서 header를 숨기기
  const hideHeader = pathname.startsWith("/accounts/profile");

  return (
    <html lang="en" className="light">
      <body className={inter.className}>
        <Providers>
          {!hideHeader && (
            <header className="header">
            <Link href="/main">
              <Image src="/SSLogo.png" width={120} height={120} alt="SSLogo" />
            </Link>
            <div className="buttons">
              <Link href="/login">
                <Button className="loginButton">로그인</Button>
              </Link>
            </div>
          </header>
          )}
            {children}
          <footer className="footer">
            <div className="footer-content">
              <Image src="/SSLogo.png" width={100} height={100} alt="SSLogo"/>
              <div className="footer-links">
                <a href="/">Home</a>
                <a href="/about">About</a>
                <a href="/contact">Contact</a>
              </div>
              <p>&copy; 2024 My Next.js App. All rights reserved.</p>
            </div>
          </footer>
        </Providers>
      </body>
    </html>
  );
}
