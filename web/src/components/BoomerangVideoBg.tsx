"use client";

import { useEffect, useRef, useState } from "react";

const VIDEO_URL =
  "https://d8j0ntlcm91z4.cloudfront.net/user_38xzZboKViGWJOttwIXH07lWA1P/hf_20260715_090628_7052d8a6-a094-4341-a4a2-ad58493a67a9.mp4";

export function BoomerangVideoBg({ poster }: { poster: string }) {
  const videoRef = useRef<HTMLVideoElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const frames = useRef<ImageData[]>([]);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const video = videoRef.current;
    if (!video) return;
    const capture = document.createElement("canvas");
    const context = capture.getContext("2d", { willReadFrequently: true });
    let capturing = true;
    let lastTime = -1;
    let requestId = 0;
    let timer = 0;

    const grab = () => {
      if (!capturing || !context || video.currentTime === lastTime || frames.current.length >= 240) return;
      const width = Math.min(960, video.videoWidth);
      if (!width || !video.videoHeight) return;
      const height = Math.max(1, Math.round(width * (video.videoHeight / video.videoWidth)));
      capture.width = width;
      capture.height = height;
      context.drawImage(video, 0, 0, width, height);
      try {
        frames.current.push(context.getImageData(0, 0, width, height));
        lastTime = video.currentTime;
      } catch {
        capturing = false;
      }
    };

    const next = () => {
      if (!capturing) return;
      const enhanced = video as HTMLVideoElement & {
        requestVideoFrameCallback?: (callback: () => void) => number;
      };
      if (enhanced.requestVideoFrameCallback)
        enhanced.requestVideoFrameCallback(() => {
          grab();
          next();
        });
      else
        requestId = requestAnimationFrame(() => {
          grab();
          next();
        });
    };

    const start = async () => {
      frames.current = [];
      try {
        await video.play();
        next();
      } catch {
        capturing = false;
      }
    };

    const ended = () => {
      capturing = false;
      if (frames.current.length < 2 || !canvasRef.current) return;
      const canvas = canvasRef.current;
      let index = 0;
      let direction = 1;
      setReady(true);
      const draw = () => {
        const frame = frames.current[index];
        canvas.width = frame.width;
        canvas.height = frame.height;
        canvas.getContext("2d")?.putImageData(frame, 0, 0);
      };
      draw();
      timer = window.setInterval(() => {
        index += direction;
        if (index >= frames.current.length - 1) {
          index = frames.current.length - 1;
          direction = -1;
        }
        if (index <= 0) {
          index = 0;
          direction = 1;
        }
        draw();
      }, 1000 / 30);
    };

    video.addEventListener("loadedmetadata", start);
    video.addEventListener("ended", ended);
    return () => {
      capturing = false;
      cancelAnimationFrame(requestId);
      window.clearInterval(timer);
      video.removeEventListener("loadedmetadata", start);
      video.removeEventListener("ended", ended);
    };
  }, []);

  return (
    <div className="boomerang-bg" style={{ backgroundImage: `url(${poster})` }} aria-hidden="true">
      <video
        ref={videoRef}
        className={ready ? "hidden" : ""}
        src={VIDEO_URL}
        poster={poster}
        muted
        playsInline
        preload="auto"
        crossOrigin="anonymous"
      />
      <canvas ref={canvasRef} className={ready ? "visible" : ""} />
    </div>
  );
}
