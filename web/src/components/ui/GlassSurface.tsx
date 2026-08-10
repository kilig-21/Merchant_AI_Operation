"use client";

import { useCallback, useEffect, useId, useRef, useState } from "react";

export interface GlassSurfaceProps {
  children: React.ReactNode;
  width?: number | string;
  height?: number | string;
  borderRadius?: number;
  borderWidth?: number;
  brightness?: number;
  opacity?: number;
  blur?: number;
  displace?: number;
  backgroundOpacity?: number;
  saturation?: number;
  distortionScale?: number;
  redOffset?: number;
  greenOffset?: number;
  blueOffset?: number;
  mixBlendMode?: string;
  variant?: "displacement" | "frosted";
  className?: string;
  style?: React.CSSProperties;
}

function cssSize(value: number | string) {
  return typeof value === "number" ? `${value}px` : value;
}

export default function GlassSurface({
  children,
  width = "fit-content",
  height = 58,
  borderRadius = 22,
  borderWidth = 0.07,
  brightness = 96,
  opacity = 0.86,
  blur = 11,
  displace = 0,
  backgroundOpacity = 0.15,
  saturation = 1.15,
  distortionScale = -80,
  redOffset = 0,
  greenOffset = 4,
  blueOffset = 8,
  mixBlendMode = "screen",
  variant = "displacement",
  className = "",
  style,
}: GlassSurfaceProps) {
  const uniqueId = useId().replace(/:/g, "-");
  const filterId = `morrow-glass-${uniqueId}`;
  const redGradientId = `morrow-glass-red-${uniqueId}`;
  const blueGradientId = `morrow-glass-blue-${uniqueId}`;
  const containerRef = useRef<HTMLDivElement>(null);
  const imageRef = useRef<SVGFEImageElement>(null);
  const redRef = useRef<SVGFEDisplacementMapElement>(null);
  const greenRef = useRef<SVGFEDisplacementMapElement>(null);
  const blueRef = useRef<SVGFEDisplacementMapElement>(null);
  const blurRef = useRef<SVGFEGaussianBlurElement>(null);
  const [svgSupported, setSvgSupported] = useState(false);

  const generateMap = useCallback(() => {
    const rect = containerRef.current?.getBoundingClientRect();
    const actualWidth = Math.max(rect?.width ?? 400, 1);
    const actualHeight = Math.max(rect?.height ?? 80, 1);
    const edge = Math.min(actualWidth, actualHeight) * (borderWidth * 0.5);
    const innerWidth = Math.max(actualWidth - edge * 2, 1);
    const innerHeight = Math.max(actualHeight - edge * 2, 1);
    const svg = `<svg viewBox="0 0 ${actualWidth} ${actualHeight}" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="${redGradientId}" x1="100%" y1="0%" x2="0%" y2="0%">
          <stop offset="0%" stop-color="#0000"/><stop offset="100%" stop-color="#fff"/>
        </linearGradient>
        <linearGradient id="${blueGradientId}" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#0000"/><stop offset="100%" stop-color="#fff"/>
        </linearGradient>
      </defs>
      <rect width="${actualWidth}" height="${actualHeight}" fill="#000"/>
      <rect width="${actualWidth}" height="${actualHeight}" rx="${borderRadius}" fill="url(#${redGradientId})"/>
      <rect width="${actualWidth}" height="${actualHeight}" rx="${borderRadius}" fill="url(#${blueGradientId})" style="mix-blend-mode:${mixBlendMode}"/>
      <rect x="${edge}" y="${edge}" width="${innerWidth}" height="${innerHeight}" rx="${Math.max(borderRadius - edge, 2)}" fill="hsl(0 0% ${brightness}% / ${opacity})" style="filter:blur(${blur}px)"/>
    </svg>`;
    return `data:image/svg+xml,${encodeURIComponent(svg)}`;
  }, [blueGradientId, borderRadius, borderWidth, brightness, blur, mixBlendMode, opacity, redGradientId]);

  const updateMap = useCallback(() => imageRef.current?.setAttribute("href", generateMap()), [generateMap]);

  useEffect(() => {
    if (variant === "frosted") {
      setSvgSupported(false);
      return;
    }
    const userAgent = navigator.userAgent;
    const unsupportedBrowser =
      (/Safari/.test(userAgent) && !/Chrome/.test(userAgent)) || /Firefox/.test(userAgent);
    setSvgSupported(!unsupportedBrowser && CSS.supports("backdrop-filter", `url(#${filterId})`));
  }, [filterId, variant]);

  useEffect(() => {
    if (variant === "frosted") return;
    updateMap();
    redRef.current?.setAttribute("scale", String(distortionScale + redOffset));
    greenRef.current?.setAttribute("scale", String(distortionScale + greenOffset));
    blueRef.current?.setAttribute("scale", String(distortionScale + blueOffset));
    blurRef.current?.setAttribute("stdDeviation", String(displace));
    for (const ref of [redRef, greenRef, blueRef]) {
      ref.current?.setAttribute("xChannelSelector", "R");
      ref.current?.setAttribute("yChannelSelector", "G");
    }
  }, [displace, distortionScale, redOffset, greenOffset, blueOffset, updateMap, variant]);

  useEffect(() => {
    if (variant === "frosted") return;
    const element = containerRef.current;
    if (!element) return;
    const observer = new ResizeObserver(() => requestAnimationFrame(updateMap));
    observer.observe(element);
    return () => observer.disconnect();
  }, [updateMap, variant]);

  const customStyle = {
    ...style,
    width: cssSize(width),
    height: cssSize(height),
    borderRadius: `${borderRadius}px`,
    "--glass-frost": backgroundOpacity,
    "--glass-saturation": saturation,
    "--glass-filter": `url(#${filterId})`,
  } as React.CSSProperties;

  return (
    <div
      ref={containerRef}
      className={`glass-surface ${
        variant === "frosted"
          ? "glass-surface--frosted"
          : svgSupported
            ? "glass-surface--svg"
            : "glass-surface--fallback"
      } ${className}`}
      style={customStyle}
    >
      {variant === "displacement" ? (
        <svg className="glass-surface__filter" aria-hidden="true" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <filter id={filterId} colorInterpolationFilters="sRGB" x="0%" y="0%" width="100%" height="100%">
              <feImage
                ref={imageRef}
                x="0"
                y="0"
                width="100%"
                height="100%"
                preserveAspectRatio="none"
                result="map"
              />
              <feDisplacementMap ref={redRef} in="SourceGraphic" in2="map" result="redDisplacement" />
              <feColorMatrix
                in="redDisplacement"
                type="matrix"
                values="1 0 0 0 0  0 0 0 0 0  0 0 0 0 0  0 0 0 1 0"
                result="red"
              />
              <feDisplacementMap ref={greenRef} in="SourceGraphic" in2="map" result="greenDisplacement" />
              <feColorMatrix
                in="greenDisplacement"
                type="matrix"
                values="0 0 0 0 0  0 1 0 0 0  0 0 0 0 0  0 0 0 1 0"
                result="green"
              />
              <feDisplacementMap ref={blueRef} in="SourceGraphic" in2="map" result="blueDisplacement" />
              <feColorMatrix
                in="blueDisplacement"
                type="matrix"
                values="0 0 0 0 0  0 0 0 0 0  0 0 1 0 0  0 0 0 1 0"
                result="blue"
              />
              <feBlend in="red" in2="green" mode="screen" result="rg" />
              <feBlend in="rg" in2="blue" mode="screen" result="output" />
              <feGaussianBlur ref={blurRef} in="output" stdDeviation="0" />
            </filter>
          </defs>
        </svg>
      ) : null}
      <div className="glass-surface__content">{children}</div>
    </div>
  );
}
