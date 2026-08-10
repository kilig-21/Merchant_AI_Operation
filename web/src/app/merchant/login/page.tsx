import { AuthForm } from "@/components/AuthForm";
import { heroPoster } from "@/lib/demo-data";
import Image from "next/image";
import { Suspense } from "react";
export default function MerchantLoginPage() {
  return (
    <main className="auth-page">
      <section className="auth-visual">
        <Image src={heroPoster} alt="商家工作空间" fill priority sizes="50vw" />
        <div className="auth-visual-copy">
          <span className="eyebrow">MORROW OS</span>
          <h1>
            让每一次经营，
            <br />
            都有清楚回声。
          </h1>
        </div>
      </section>
      <section className="auth-panel">
        <Suspense>
          <AuthForm mode="login" merchant />
        </Suspense>
      </section>
    </main>
  );
}
