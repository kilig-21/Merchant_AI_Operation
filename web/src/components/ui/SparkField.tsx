"use client";

import { useEffect, useRef } from "react";

interface SparkCell {
  x: number;
  y: number;
  phase: number;
  speed: number;
  strength: number;
  accent: boolean;
}

const hash = (x: number, y: number) => {
  const value = Math.sin(x * 127.1 + y * 311.7) * 43758.5453;
  return value - Math.floor(value);
};

export default function SparkField() {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    const context = canvas?.getContext("2d");
    if (!canvas || !context) return undefined;

    const motionQuery = window.matchMedia("(prefers-reduced-motion: reduce)");
    let reduced = motionQuery.matches;
    let frame = 0;
    let cells: SparkCell[] = [];
    let width = 0;
    let height = 0;
    let ratio = 1;

    const buildCells = () => {
      const rect = canvas.getBoundingClientRect();
      width = Math.max(1, Math.round(rect.width));
      height = Math.max(1, Math.round(rect.height));
      ratio = Math.min(window.devicePixelRatio || 1, 1.6);
      canvas.width = Math.round(width * ratio);
      canvas.height = Math.round(height * ratio);
      context.setTransform(ratio, 0, 0, ratio, 0, 0);

      const step = width < 640 ? 15 : 16;
      const columns = Math.ceil(width / step) + 1;
      const rows = Math.ceil(height / step) + 1;
      cells = [];
      for (let row = 0; row < rows; row += 1) {
        for (let column = 0; column < columns; column += 1) {
          const seed = hash(column, row);
          cells.push({
            x: column * step + step / 2,
            y: row * step + step / 2,
            phase: seed * Math.PI * 2 + (column + row) * 0.08,
            speed: 0.55 + hash(row + 17, column + 3) * 0.85,
            strength: 0.38 + hash(column + 29, row + 11) * 0.62,
            accent: hash(column + 5, row + 41) > 0.94,
          });
        }
      }
    };

    const draw = (timestamp: number) => {
      context.clearRect(0, 0, width, height);
      const time = reduced ? 0.7 : timestamp / 1000;

      for (const cell of cells) {
        const individual = Math.max(0, Math.sin(time * cell.speed * 2.2 + cell.phase));
        const travelling = Math.max(
          0,
          Math.sin(time * 1.05 - cell.x * 0.014 - cell.y * 0.009 + cell.phase * 0.18),
        );
        const blink = individual ** 12;
        const wave = travelling ** 16;
        const alpha = 0.1 + (blink * 0.68 + wave * 0.35) * cell.strength;
        const size = 2.4 + Math.max(blink, wave) * 1.25;
        const half = size / 2;

        context.fillStyle = cell.accent
          ? `rgba(198, 240, 77, ${Math.min(0.82, alpha * 0.8)})`
          : `rgba(244, 245, 239, ${Math.min(0.9, alpha)})`;
        context.fillRect(cell.x - half, cell.y - half, size, size);
      }

      if (!reduced) frame = requestAnimationFrame(draw);
    };

    const resizeObserver = new ResizeObserver(() => {
      buildCells();
      if (reduced) draw(0);
    });
    resizeObserver.observe(canvas);

    const handleMotionChange = (event: MediaQueryListEvent) => {
      reduced = event.matches;
      cancelAnimationFrame(frame);
      draw(0);
    };
    motionQuery.addEventListener("change", handleMotionChange);

    buildCells();
    draw(performance.now());

    return () => {
      cancelAnimationFrame(frame);
      resizeObserver.disconnect();
      motionQuery.removeEventListener("change", handleMotionChange);
    };
  }, []);

  return <canvas ref={canvasRef} className="spark-field" />;
}
