"use client";

import { type FormEvent, useEffect, useRef, useState } from "react";
import { apiClient } from "@/lib/client-api";
import {
  MERCHANT_AI_MESSAGE_LIMIT,
  merchantAiStarterQuestions,
  normalizeMerchantAiMessage,
} from "@/lib/merchant-ai";
import type { MerchantAiChatResponse } from "@/lib/types";
import { RequestFailure } from "./RequestFailure";
import { MerchantShell } from "./MerchantShell";

type ChatMessage = {
  role: "user" | "assistant";
  content: string;
  model?: string | null;
  businessDataUsed?: boolean;
};

type ServiceState = "idle" | "loading" | "ready" | "error";

const starterMessage: ChatMessage = {
  role: "assistant",
  content: "你好，我是商家经营助手。当前可以进行基础文本对话，并查询当前商家在明确日期范围内的经营汇总；不能执行任何店铺操作。",
};

export function MerchantAiChat() {
  const [messages, setMessages] = useState<ChatMessage[]>([starterMessage]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [failure, setFailure] = useState<Error | null>(null);
  const [retryMessage, setRetryMessage] = useState<string | null>(null);
  const [serviceState, setServiceState] = useState<ServiceState>("idle");
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });
  });

  async function requestAnswer(message: string) {
    setFailure(null);
    setRetryMessage(null);
    setLoading(true);
    setServiceState("loading");

    try {
      const result = await apiClient<MerchantAiChatResponse>("/api/backend/merchant/ai/chat", {
        method: "POST",
        body: JSON.stringify({ message }),
      });
      setMessages((current) => [
        ...current,
        {
          role: "assistant",
          content: result.answer,
          model: result.model,
          businessDataUsed: result.businessDataUsed,
        },
      ]);
      setServiceState("ready");
    } catch (error) {
      setFailure(error instanceof Error ? error : new Error("AI 服务暂时不可用，请稍后重试。"));
      setRetryMessage(message);
      setServiceState("error");
    } finally {
      setLoading(false);
    }
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const message = normalizeMerchantAiMessage(input);
    if (!message || loading) return;

    setMessages((current) => [...current, { role: "user", content: message }]);
    setInput("");
    await requestAnswer(message);
  }

  function retry() {
    if (retryMessage && !loading) void requestAnswer(retryMessage);
  }

  const statusLabel = {
    idle: "等待首次对话",
    loading: "正在连接 DeepSeek",
    ready: "DeepSeek 已响应",
    error: "服务暂不可用",
  }[serviceState];

  return (
    <MerchantShell title="AI 经营助手" eyebrow="MORROW / AI OPERATIONS">
      <section className="ai-chat-layout" aria-label="AI 经营助手">
        <div className="ai-chat-intro">
          <span className="eyebrow">A3 / READ-ONLY TOOL</span>
          <h2>把问题交给助手，需要时用真实数据回答。</h2>
          <p>当前版本可查询当前商家在明确日期范围内的经营汇总，仍不会执行任何店铺操作。</p>
          <div className="ai-chat-boundary">
            <strong>A3 当前能力</strong>
            <span>可以生成通用建议与文案</span>
            <span>可以查询当前商家的经营汇总</span>
            <span>不会执行改价、上架或促销操作</span>
          </div>
          <div className="ai-chat-starters" aria-label="快捷问题">
            {merchantAiStarterQuestions.map((question) => (
              <button disabled={loading} key={question} onClick={() => setInput(question)} type="button">
                {question}<span aria-hidden="true">↗</span>
              </button>
            ))}
          </div>
        </div>

        <section className="ai-chat-panel surface" aria-label="对话记录">
          <div className={`ai-chat-status ai-chat-status--${serviceState}`}>
            <span className="status-dot" aria-hidden="true" />
            <span>{statusLabel}</span>
            <small>A3 · 经营汇总 · 只读查询</small>
          </div>

          <div className="ai-chat-messages" aria-live="polite">
            {messages.map((message, index) => (
              <article className={`ai-chat-message ai-chat-message--${message.role}`} key={`${message.role}-${index}`}>
                <span className="ai-chat-role">{message.role === "user" ? "你" : "助手"}</span>
                <p>{message.content}</p>
                {message.model ? <small>模型：{message.model}</small> : null}
                {message.role === "assistant" && typeof message.businessDataUsed === "boolean" ? (
                  <small className={`ai-chat-data-note ai-chat-data-note--${message.businessDataUsed ? "used" : "unused"}`}>
                    {message.businessDataUsed
                      ? "本次回答已查询当前商家经营数据"
                      : "本次回答未使用店铺经营数据"}
                  </small>
                ) : null}
              </article>
            ))}
            {loading ? <div className="ai-chat-loading">助手正在思考…</div> : null}
            <div ref={messagesEndRef} />
          </div>

          {failure ? <RequestFailure error={failure} loginHref="/merchant/login" onRetry={retry} title="AI 助手暂时无法回答" /> : null}

          <form className="ai-chat-form" onSubmit={submit}>
            <label htmlFor="merchant-ai-message">向助手提问</label>
            <textarea
              id="merchant-ai-message"
              value={input}
              onChange={(event) => setInput(event.target.value)}
              placeholder="例如：帮我写一段新品上架公告"
              maxLength={MERCHANT_AI_MESSAGE_LIMIT}
              rows={3}
              disabled={loading}
            />
            <div className="ai-chat-form-footer">
              <span>{input.length}/{MERCHANT_AI_MESSAGE_LIMIT}</span>
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
