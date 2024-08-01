"use client"

import { Inter } from "next/font/google";
import { ReactNode } from "react";
import "@/styles/globals.css";
import CssBaseline from "@mui/material/CssBaseline";
import Header from "@/components/layout/Header";
import Footer from "@/components/layout/Footer";
import { createTheme } from "@mui/material";
import { ThemeProvider } from "@emotion/react";

const inter = Inter({ subsets: ["latin"] });

const theme = createTheme({
  palette: {
    mode: 'light',
  },
});

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="ko" className={inter.className}>
      <body>
        <ThemeProvider theme={theme}>
          <CssBaseline />
          <Header />
            <main>{children}</main>
          <Footer />
        </ThemeProvider>
      </body>
    </html>
  );
}
