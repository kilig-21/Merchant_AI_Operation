"use client";

import { useGSAP } from "@gsap/react";
import gsap from "gsap";
import { ScrollTrigger } from "gsap/ScrollTrigger";
import React, { useEffect, useRef, useState } from "react";

gsap.registerPlugin(ScrollTrigger);

function cx(...parts: Array<string | undefined | false | null>) {
  return parts.filter(Boolean).join(" ");
}

export interface FlowSectionProps {
  id?: string;
  className?: string;
  innerClassName?: string;
  style?: React.CSSProperties;
  children: React.ReactNode;
  "aria-label"?: string;
}

export function FlowSection({
  id,
  className,
  innerClassName,
  style,
  children,
  "aria-label": ariaLabel,
}: FlowSectionProps) {
  return (
    <section id={id} data-flow-section aria-label={ariaLabel} className={cx("story-flow-section", className)}>
      <div
        data-flow-inner
        className={cx("story-flow-inner", innerClassName)}
        style={{ transformOrigin: "bottom left", ...style }}
      >
        {children}
      </div>
    </section>
  );
}

export interface FlowArtProps {
  children: React.ReactNode;
  className?: string;
  "aria-label"?: string;
  onActiveSectionChange?: (index: number) => void;
}

export default function FlowArt({
  children,
  className,
  "aria-label": ariaLabel = "Morrow 首页故事滚动",
  onActiveSectionChange,
}: FlowArtProps) {
  const containerRef = useRef<HTMLElement>(null);
  const [reducedMotion, setReducedMotion] = useState(false);
  const sectionCount = React.Children.count(children);

  useEffect(() => {
    const query = window.matchMedia("(prefers-reduced-motion: reduce)");
    const update = () => setReducedMotion(query.matches);
    update();
    query.addEventListener("change", update);
    return () => query.removeEventListener("change", update);
  }, []);

  useGSAP(
    () => {
      if (!containerRef.current) return;

      const sections = Array.from(containerRef.current.querySelectorAll<HTMLElement>("[data-flow-section]"));
      const triggers: ScrollTrigger[] = [];
      const compact = window.matchMedia("(max-width: 680px)").matches;

      onActiveSectionChange?.(0);
      sections.forEach((section, index) => {
        triggers.push(
          ScrollTrigger.create({
            trigger: section,
            start: "top 54%",
            end: "bottom 46%",
            onEnter: () => onActiveSectionChange?.(index),
            onEnterBack: () => onActiveSectionChange?.(index),
          }),
        );
      });

      if (reducedMotion) {
        ScrollTrigger.refresh();
        return () => {
          for (const trigger of triggers) trigger.kill();
        };
      }

      sections.forEach((section, index) => {
        gsap.set(section, { zIndex: index + 1 });
        const inner = section.querySelector<HTMLElement>("[data-flow-inner]");
        if (!inner) return;

        if (index > 0) {
          gsap.set(inner, {
            rotation: compact ? 7 : 14,
            transformOrigin: "bottom left",
          });
          const entrance = gsap.to(inner, {
            rotation: 0,
            ease: "none",
            scrollTrigger: {
              trigger: section,
              start: "top bottom",
              end: compact ? "top 48%" : "top 28%",
              scrub: 0.35,
              invalidateOnRefresh: true,
            },
          });
          if (entrance.scrollTrigger) triggers.push(entrance.scrollTrigger);
        }

        if (index < sections.length - 1) {
          triggers.push(
            ScrollTrigger.create({
              trigger: section,
              start: "bottom bottom",
              end: "bottom top",
              pin: true,
              pinSpacing: false,
              anticipatePin: 1,
              invalidateOnRefresh: true,
            }),
          );
        }
      });

      ScrollTrigger.refresh();
      return () => {
        for (const trigger of triggers) trigger.kill();
      };
    },
    { scope: containerRef, dependencies: [sectionCount, reducedMotion, onActiveSectionChange] },
  );

  return (
    <main ref={containerRef} aria-label={ariaLabel} className={cx("story-flow", className)}>
      {children}
    </main>
  );
}
