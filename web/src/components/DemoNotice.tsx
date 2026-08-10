export function DemoNotice({ children = "服务未连接，当前显示演示内容。" }: { children?: React.ReactNode }) {
  return (
    <p className="demo-notice">
      <i /> DEMO / {children}
    </p>
  );
}
