"use client";

import { type FormEvent, useState } from "react";
import { apiClient } from "@/lib/client-api";
import { RequestFailure } from "./RequestFailure";
import { MerchantShell } from "./MerchantShell";

type AiChatResponse = {
  answer: string;
  model: string | null;
  businessDataUsed: boolean;
};

type ChatMessage = {
  role: "user" | "assistant";
  content: string;
  model?: string | null;
};

const starterMessage: ChatMessage = {
  role: "assistant",
  content: "你好，我是商家经营助手的早期版本。当前可以进行基础文本对话，但还不能查询真实经营数据或执行店铺操作。",
};

export function MerchantAiChat() {
  const [messages, setMessages] = useState<ChatMessage[]>([starterMessage]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [failure, setFailure] = useState<string | null>(null);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const message = input.trim();
    if (!message || loading) return;

    setMessages((current) => [...current, { role: "user", content: message }]);
    setInput("");
    setFailure(null);
    setLoading(true);

    try {
      const result = await apiClient<AiChatResponse>("/api/backend/merchant/ai/chat", {
        method: "POST",
        body: JSON.stringify({ message }),
      });
      setMessages((current) => [
        ...current,
        { role: "assistant", content: result.answer, model: result.model },
      ]);
    } catch (error) {
      setFailure(error instanceof Error ? error.message : "AI 服务暂时不可用，请稍后重试。");
    } finally {
      setLoading(false);
    }
  }

  return (
    <MerchantShell title="AI 经营助手" eyebrow="MORROW / AI OPERATIONS">
      <section className="ai-chat-layout" aria-label="AI 经营助手">
        <div className="ai-chat-intro">
          <span className="eyebrow">A2 / TEXT MODEL</span>
          <h2>把问题交给助手，先从清晰的对话开始。</h2>
          <p>当前版本只提供基础文本对话，不读取订单、商品、库存、经营指标或售后数据。</p>
        </div>

        <section className="ai-chat-panel surface" aria-label="对话记录">
          <div className="ai-chat-status">
            <span className="status-dot" aria-hidden="true" />
            <span>在线 · 早期版本</span>
          </div>

          <div className="ai-chat-messages" aria-live="polite">
            {messages.map((message, index) => (
              <article className={`ai-chat-message ai-chat-message--${message.role}`} key={`${message.role}-${index}`}>
                <span className="ai-chat-role">{message.role === "user" ? "你" : "助手"}</span>
                <p>{message.content}</p>
                {message.model ? <small>模型：{message.model}</small> : null}
              </article>
            ))}
            {loading ? <div className="ai-chat-loading">助手正在思考…</div> : null}
          </div>

          {failure ? <RequestFailure error={failure} onRetry={() => setFailure(null)} title="AI 助手暂时无法回答" /> : null}

          <form className="ai-chat-form" onSubmit={submit}>
            <label htmlFor="merchant-ai-message">向助手提问</label>
            <textarea
              id="merchant-ai-message"
              value={input}
              onChange={(event) => setInput(event.target.value)}
              placeholder="例如：帮我写一段新品上架公告"
              maxLength={1000}
              rows={3}
              disabled={loading}
            />
            <div className="ai-chat-form-footer">
              <span>{input.length}/1000</span>
              <button className="button primary" type="submit" disabled={loading || !input.trim()}>
                {loading ? "发送中…" : "发送 ↗"}
              </button>
            </div>
          </form>
        </section>
      </section>
    </MerchantShell>
  );
}
