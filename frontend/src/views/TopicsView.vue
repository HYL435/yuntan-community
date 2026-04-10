<template>
  <section class="topics-page">
    <div class="topics-shell">
      <header class="topics-head">
        <p class="topics-kicker">Topics</p>
        <h1 class="topics-title">话题广场</h1>
        <p class="topics-desc">页面结构已就绪，后续可在此接入话题列表、热度排行、标签筛选与详情流。</p>
      </header>

      <div class="topics-toolbar">
        <div class="tool-chip">全部话题</div>
        <div class="tool-chip">最新发布</div>
        <div class="tool-chip">热度排序</div>
      </div>

      <div class="topics-layout">
        <main class="topics-main" aria-label="话题内容区域">
          <div class="empty-board">
            <h2>内容暂未开放</h2>
            <p>当前仅完成页面布局，内容模块将在后续版本逐步上线。</p>
          </div>
        </main>

        <aside class="topics-side" aria-label="话题侧边栏占位">
          <div class="side-card">
            <h3>热议标签</h3>
            <p>占位区域</p>
          </div>
          <div class="side-card">
            <h3>活跃作者</h3>
            <p>占位区域</p>
          </div>
        </aside>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts" name="TopicsView">
</script>

<style scoped>
.topics-page {
  min-height: calc(100vh - 140px);
  padding: 112px 24px 48px;
  color: #11323b;
  background:
    radial-gradient(circle at 8% 10%, rgba(20, 184, 166, 0.14), transparent 30%),
    radial-gradient(circle at 92% 16%, rgba(59, 130, 246, 0.14), transparent 30%),
    linear-gradient(180deg, #f8fdff 0%, #eefaf7 52%, #f3f8ff 100%);
}

.dark .topics-page {
  color: #e7eef1;
  background:
    radial-gradient(circle at 8% 10%, rgba(239, 68, 68, 0.16), transparent 28%),
    radial-gradient(circle at 92% 16%, rgba(244, 63, 94, 0.14), transparent 28%),
    linear-gradient(180deg, #080808 0%, #101010 52%, #0b0b0b 100%);
}

.topics-shell {
  max-width: 1260px;
  margin: 0 auto;
}

.topics-head {
  margin-bottom: 18px;
}

.topics-kicker {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  color: rgba(17, 50, 59, 0.56);
}

.dark .topics-kicker {
  color: rgba(226, 232, 240, 0.62);
}

.topics-title {
  margin: 10px 0 0;
  font-size: clamp(34px, 6vw, 52px);
  line-height: 1.05;
}

.topics-desc {
  margin: 12px 0 0;
  font-size: 16px;
  color: rgba(17, 50, 59, 0.72);
}

.dark .topics-desc {
  color: rgba(226, 232, 240, 0.76);
}

.topics-toolbar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.tool-chip {
  height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  border: 1px solid rgba(17, 50, 59, 0.12);
  background: rgba(255, 255, 255, 0.7);
}

.dark .tool-chip {
  border-color: rgba(248, 113, 113, 0.2);
  background: rgba(22, 22, 22, 0.9);
}

.topics-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 16px;
}

.topics-main,
.side-card {
  border-radius: 16px;
  border: 1px solid rgba(17, 50, 59, 0.1);
  background: rgba(255, 255, 255, 0.78);
}

.dark .topics-main,
.dark .side-card {
  border-color: rgba(248, 113, 113, 0.2);
  background: rgba(18, 18, 18, 0.92);
}

.topics-main {
  min-height: 420px;
  padding: 20px;
}

.empty-board {
  height: 100%;
  min-height: 380px;
  display: grid;
  place-content: center;
  text-align: center;
  color: rgba(17, 50, 59, 0.72);
}

.dark .empty-board {
  color: rgba(226, 232, 240, 0.78);
}

.topics-side {
  display: grid;
  gap: 12px;
  align-content: start;
}

.side-card {
  padding: 16px;
  min-height: 130px;
}

.side-card h3 {
  margin: 0;
  font-size: 18px;
}

.side-card p {
  margin: 10px 0 0;
  color: rgba(17, 50, 59, 0.66);
}

.dark .side-card p {
  color: rgba(226, 232, 240, 0.72);
}

@media (max-width: 1024px) {
  .topics-layout {
    grid-template-columns: 1fr;
  }

  .topics-page {
    padding-top: 98px;
  }
}
</style>