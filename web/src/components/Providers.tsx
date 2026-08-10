"use client";

import { LayoutProvider, ThemeProvider, ToastProvider } from "@once-ui-system/core";
import { SessionProvider } from "./SessionProvider";

export function Providers({ children }: { children: React.ReactNode }) {
  return (
    <LayoutProvider>
      <ThemeProvider
        theme="system"
        neutral="gray"
        brand="moss"
        accent="yellow"
        solid="contrast"
        solidStyle="flat"
        border="playful"
        surface="translucent"
        transition="all"
        scaling="100"
      >
        <ToastProvider>
          <SessionProvider>{children}</SessionProvider>
        </ToastProvider>
      </ThemeProvider>
    </LayoutProvider>
  );
}
