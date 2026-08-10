import { AuthForm } from "@/components/AuthForm";
import { SiteNav } from "@/components/SiteNav";
import { heroPoster } from "@/lib/demo-data";
import Image from "next/image";
import { Suspense } from "react";
export default function RegisterPage() {
  return (
    <>
      <SiteNav />
      <main className="auth-page">
        <section className="auth-visual">
          <Image src={heroPoster} alt="明亮的生活空间" fill priority sizes="50vw" />
          <div className="auth-visual-copy">
            <span className="eyebrow">MORROW / MEMBER</span>
            <h1>
              从今天开始，
              <br />
              慢慢选。
            </h1>
          </div>
        </section>
        <section className="auth-panel">
          <Suspense>
            <AuthForm mode="register" />
          </Suspense>
        </section>
      </main>
    </>
  );
}
