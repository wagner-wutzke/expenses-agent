(() => {
  const form = document.querySelector("#chat-form");
  const input = document.querySelector("#chat-input");
  const messages = document.querySelector("#chat-messages");
  const sendButton = form.querySelector("button[type='submit']");
  const tracePrompt = document.querySelector("#trace-prompt");
  const traceReasoning = document.querySelector("#trace-reasoning");
  const traceResponse = document.querySelector("#trace-response");
  const traceState = document.querySelector("#trace-state");
  const traceToggle = document.querySelector(".trace-toggle");
  const tokenInput = document.querySelector("#token-input");
  const tokenOutput = document.querySelector("#token-output");
  const tokenTotal = document.querySelector("#token-total");
  const tokenNote = document.querySelector("#token-note");
  const sessionId = getOrCreateId("pennywise-session");
  const userId = getOrCreateId("pennywise-user");

  if (globalThis.mermaid) {
    globalThis.mermaid.initialize({
      startOnLoad: false,
      securityLevel: "strict",
      theme: "neutral"
    });
  }

  document.querySelectorAll("[data-prompt]").forEach((button) => {
    button.addEventListener("click", () => {
      input.value = button.dataset.prompt;
      input.focus();
      resizeInput();
      input.setSelectionRange(input.value.length, input.value.length);
      document.querySelector("#assistant").scrollIntoView({ behavior: "smooth", block: "start" });
    });
  });

  input.addEventListener("input", resizeInput);
  input.addEventListener("keydown", (event) => {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      form.requestSubmit();
    }
  });

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const prompt = input.value.trim();
    if (!prompt || sendButton.disabled) return;

    appendMessage("user", prompt);
    clearTrace(prompt);
    input.value = "";
    resizeInput();
    sendButton.disabled = true;
    const pending = appendTypingIndicator();

    try {
      const query = new URLSearchParams({ userPrompt: prompt, userId, sessionId });
      const response = await fetch(`/api/agent/chat?${query.toString()}`, {
        method: "POST",
        headers: { Accept: "application/json" }
      });
      if (!response.ok) {
        const details = (await response.text()).trim();
        throw new Error(details || `The assistant returned an error (${response.status}).`);
      }
      const turn = await response.json();
      pending.remove();
      const answer = turn.response || "I didn't receive a reply. Please try again.";
      tracePrompt.textContent = turn.prompt || prompt;
      traceReasoning.textContent = turn.reasoning || "No reasoning summary was returned.";
      traceResponse.textContent = answer;
      updateTokenUsage(turn.tokenUsage);
      setTraceState("COMPLETE");
      appendMessage("assistant", answer);
    } catch (error) {
      pending.remove();
      const errorMessage = error.message || "I couldn't reach the assistant. Please try again.";
      traceResponse.textContent = errorMessage;
      updateTokenUsage(null);
      setTraceState("ERROR", true);
      appendMessage("assistant", errorMessage, true);
    } finally {
      sendButton.disabled = false;
      input.focus();
    }
  });

  function appendMessage(role, text, isError = false) {
    const row = document.createElement("div");
    row.className = `message-row ${role === "user" ? "user-message" : "assistant-message"}`;

    const avatar = document.createElement("div");
    avatar.className = "message-avatar";
    if (role === "user") {
      avatar.textContent = "Y";
      avatar.setAttribute("aria-hidden", "true");
    } else {
      const icon = document.createElement("i");
      icon.className = "bi bi-flower1";
      icon.setAttribute("aria-hidden", "true");
      avatar.append(icon);
    }

    const content = document.createElement("div");
    content.className = "message-content";
    const meta = document.createElement("div");
    meta.className = "message-meta";
    meta.append(document.createTextNode(role === "user" ? "YOU" : "PENNY"));
    const time = document.createElement("span");
    time.textContent = new Intl.DateTimeFormat(undefined, { hour: "numeric", minute: "2-digit" }).format(new Date());
    meta.append(time);
    const bubble = document.createElement("div");
    bubble.className = "message-bubble";
    renderMarkdown(bubble, text);
    if (isError) bubble.setAttribute("role", "alert");

    content.append(meta, bubble);
    row.append(avatar, content);
    messages.append(row);
    messages.scrollTop = messages.scrollHeight;
    return row;
  }

  function appendTypingIndicator() {
    const row = document.createElement("div");
    row.className = "message-row assistant-message";
    row.setAttribute("aria-label", "Penny is responding");
    const avatar = document.createElement("div");
    avatar.className = "message-avatar";
    const icon = document.createElement("i");
    icon.className = "bi bi-flower1";
    avatar.append(icon);
    const content = document.createElement("div");
    content.className = "message-content";
    const meta = document.createElement("div");
    meta.className = "message-meta";
    meta.textContent = "PENNY";
    const dots = document.createElement("div");
    dots.className = "typing-dots";
    dots.innerHTML = "<span></span><span></span><span></span>";
    dots.setAttribute("aria-hidden", "true");
    content.append(meta, dots);
    row.append(avatar, content);
    messages.append(row);
    messages.scrollTop = messages.scrollHeight;
    return row;
  }

  function resizeInput() {
    input.style.height = "auto";
    input.style.height = `${Math.min(input.scrollHeight, 120)}px`;
  }

  function renderMarkdown(container, markdown) {
    if (globalThis.marked && globalThis.DOMPurify) {
      const renderer = new globalThis.marked.Renderer();
      const defaultCodeRenderer = renderer.code.bind(renderer);
      renderer.code = (token) => {
        if (globalThis.mermaid && token.lang?.trim().toLowerCase() === "mermaid") {
          return `<div class="mermaid">${escapeHTML(token.text)}</div>`;
        }
        return defaultCodeRenderer(token);
      };
      const html = globalThis.marked.parse(markdown, { gfm: true, breaks: true, renderer });
      container.innerHTML = globalThis.DOMPurify.sanitize(html);
      renderMermaidDiagrams(container);
      return;
    }
    container.textContent = markdown;
  }

  function renderMermaidDiagrams(container) {
    if (!globalThis.mermaid) return;
    const diagrams = [...container.querySelectorAll(".mermaid")];
    if (!diagrams.length) return;
    globalThis.mermaid.run({ nodes: diagrams }).catch(() => {
      diagrams.forEach((diagram) => {
        const source = document.createElement("pre");
        source.className = "mermaid-fallback";
        source.textContent = `Diagram could not be rendered. Source:\n${diagram.textContent}`;
        diagram.replaceWith(source);
      });
    });
  }

  function escapeHTML(value) {
    return value.replace(/[&<>"']/g, (character) => ({
      "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    })[character]);
  }

  function clearTrace(prompt) {
    tracePrompt.textContent = prompt;
    traceReasoning.textContent = "Waiting for the assistant's activity summary…";
    traceResponse.textContent = "Waiting for the assistant's response…";
    tokenInput.textContent = "—";
    tokenOutput.textContent = "—";
    tokenTotal.textContent = "—";
    tokenNote.textContent = "Waiting for model usage…";
    setTraceState("WORKING", false, true);
  }

  function updateTokenUsage(usage) {
    const format = (value) => Number.isFinite(value) ? value.toLocaleString() : "—";
    tokenInput.textContent = format(usage?.inputTokens);
    tokenOutput.textContent = format(usage?.outputTokens);
    tokenTotal.textContent = format(usage?.totalTokens);
    tokenNote.textContent = usage?.totalTokens == null
      ? "Token usage was not returned by the model."
      : "Reported by the model for this response.";
  }

  function setTraceState(label, isError = false, isWorking = false) {
    traceState.lastChild.textContent = ` ${label}`;
    traceState.classList.toggle("is-error", isError);
    traceState.classList.toggle("is-working", isWorking);
    traceToggle.setAttribute("aria-label", `Prompt and reasoning panel, ${label.toLowerCase()}`);
  }

  function getOrCreateId(key) {
    let id = sessionStorage.getItem(key);
    if (!id) {
      id = globalThis.crypto?.randomUUID?.() || `${Date.now()}-${Math.random().toString(16).slice(2)}`;
      sessionStorage.setItem(key, id);
    }
    return id;
  }
})();
