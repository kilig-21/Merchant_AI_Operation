"use client";

import { useGSAP } from "@gsap/react";
import gsap from "gsap";
import { SplitText as GSAPSplitText } from "gsap/SplitText";
import type { ElementType } from "react";
import { useEffect, useRef, useState } from "react";

gsap.registerPlugin(GSAPSplitText, useGSAP);

type SplitTextTag = "span" | "p" | "h3";
type SplitTextType = "chars" | "words" | "lines" | "words, chars";

export interface SplitTextProps {
  text: string;
  as?: SplitTextTag;
  splitType?: SplitTextType;
  delay?: number;
  startDelay?: number;
  duration?: number;
  ease?: string;
  from?: gsap.TweenVars;
  to?: gsap.TweenVars;
  className?: string;
  onComplete?: () => void;
}

export default function SplitText({
  text,
  as = "span",
  splitType = "chars",
  delay = 38,
  startDelay = 0,
  duration = 0.72,
  ease = "power3.out",
  from = { opacity: 0, y: 46 },
  to = { opacity: 1, y: 0 },
  className = "",
  onComplete,
}: SplitTextProps) {
  const ref = useRef<HTMLElement>(null);
  const callbackRef = useRef(onComplete);
  const [fontsLoaded, setFontsLoaded] = useState(false);
  const [reducedMotion, setReducedMotion] = useState(false);

  useEffect(() => {
    callbackRef.current = onComplete;
  }, [onComplete]);

  useEffect(() => {
    let active = true;
    const query = window.matchMedia("(prefers-reduced-motion: reduce)");
    const updateMotion = () => setReducedMotion(query.matches);
    updateMotion();
    query.addEventListener("change", updateMotion);

    const markReady = () => {
      if (active) setFontsLoaded(true);
    };
    if (document.fonts.status === "loaded") markReady();
    else void document.fonts.ready.then(markReady);

    return () => {
      active = false;
      query.removeEventListener("change", updateMotion);
    };
  }, []);

  useGSAP(
    () => {
      const element = ref.current;
      if (!element || !text || !fontsLoaded || reducedMotion) return;

      const split = new GSAPSplitText(element, {
        type: splitType,
        smartWrap: true,
        linesClass: "split-line",
        wordsClass: "split-word",
        charsClass: "split-char",
        reduceWhiteSpace: false,
      });
      const targets = splitType.includes("chars")
        ? split.chars
        : splitType.includes("words")
          ? split.words
          : split.lines;
      const tween = gsap.fromTo(
        targets,
        { ...from },
        {
          ...to,
          delay: startDelay,
          duration,
          ease,
          stagger: delay / 1000,
          force3D: true,
          onComplete: () => {
            gsap.set(targets, { clearProps: "willChange" });
            callbackRef.current?.();
          },
        },
      );
      gsap.set(targets, { willChange: "transform, opacity" });

      return () => {
        tween.kill();
        split.revert();
      };
    },
    {
      scope: ref,
      dependencies: [
        text,
        splitType,
        delay,
        startDelay,
        duration,
        ease,
        JSON.stringify(from),
        JSON.stringify(to),
        fontsLoaded,
        reducedMotion,
      ],
    },
  );

  const Tag = as as ElementType;
  return (
    <Tag ref={ref} className={`split-parent ${className}`} aria-label={text}>
      {text}
    </Tag>
  );
}
