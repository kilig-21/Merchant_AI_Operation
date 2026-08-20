import { GeistMono } from "geist/font/mono";
import { GeistSans } from "geist/font/sans";
import type { Metadata } from "next";
import "@once-ui-system/core/css/styles.css";
import "@once-ui-system/core/css/tokens.css";
import "@/components/ui/DepthText.css";
import "@/components/ui/DriftWall.css";
import "@/components/ui/GlassSurface.css";
import "@/components/ui/LineSidebar.css";
import "@/components/ui/MorphSlider.css";
import "@/components/ui/SparkField.css";
import "./globals.css";
import "./marketplace.css";
import "./operations.css";
import { Providers } from "@/components/Providers";

export const metadata: Metadata = {
  title: "Morrow · Quiet Commerce",
  description: "为日常认真选择的现代电商空间。",
  icons: { icon: "/media/product-1.svg" },
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html
      className={`${GeistSans.variable} ${GeistMono.variable}`}
      data-scroll-behavior="smooth"
      lang="zh-CN"
      suppressHydrationWarning
    >
      <head>
        <script>{`(function(){try{var t=localStorage.getItem('data-theme')||'system';var r=t==='system'?(matchMedia('(prefers-color-scheme: dark)').matches?'dark':'light'):t;document.documentElement.dataset.theme=r}catch(e){document.documentElement.dataset.theme='light'}})()`}</script>
      </head>
      <body>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
