"use client";

import type { ReactNode } from "react";
import { useCallback, useState } from "react";
import { SiteNav } from "./SiteNav";
import LineSidebar, { type LineSidebarItem } from "./ui/LineSidebar";
import FlowArt from "./ui/story-scroll";

const chapters: LineSidebarItem[] = [
  { id: "campaign", label: "本期广告", shortLabel: "广告" },
  { id: "curation", label: "生活策选", shortLabel: "策选" },
  { id: "object-wall", label: "物件墙", shortLabel: "物件" },
  { id: "arrivals", label: "刚刚抵达", shortLabel: "抵达" },
  { id: "journal", label: "选物志", shortLabel: "选物" },
];

export function HomeStoryExperience({ children }: { children: ReactNode }) {
  const [activeSection, setActiveSection] = useState(0);
  const jumpToChapter = useCallback((_index: number, item: LineSidebarItem) => {
    document.getElementById(item.id)?.scrollIntoView({ behavior: "smooth", block: "start" });
  }, []);

  return (
    <>
      <SiteNav storyMode={activeSection === 0 ? "hero" : "content"} />
      <LineSidebar
        className={activeSection > 0 ? "line-sidebar--visible" : ""}
        items={chapters}
        activeIndex={Math.max(activeSection - 1, 0)}
        onItemClick={jumpToChapter}
      />
      <FlowArt className="home-story" onActiveSectionChange={setActiveSection}>
        {children}
      </FlowArt>
    </>
  );
}
