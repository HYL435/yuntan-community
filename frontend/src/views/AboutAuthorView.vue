<template>
  <div class="about-author-page">
    <section class="author-hero">
      <BackHomeButton />
      <div class="hero-shell">
        <div class="hero-left reveal reveal-1">
          <p class="hero-kicker">About The Author</p>
          <h1 class="hero-title">你好，我是云坛博主</h1>
          <p class="hero-desc">
            专注于全栈开发与产品体验打磨，热爱把复杂问题拆成清晰、可维护、可扩展的实现方案。
            喜欢记录真实踩坑、沉淀工程实践，也持续探索更好的交互表达。
          </p>
          <div class="hero-actions">
            <a class="hero-btn hero-btn--solid" href="#skills">技能栈</a>
            <a class="hero-btn" href="#timeline">成长经历</a>
            <a class="hero-btn" href="#contact">联系我</a>
          </div>
        </div>

        <div class="hero-right reveal reveal-2">
          <div class="avatar-shell">
            <div class="avatar-ring"></div>
            <div class="avatar-core">YT</div>
          </div>
          <div class="hero-badges">
            <div class="badge-card">
              <span class="badge-label">专注方向</span>
              <strong class="badge-value">前后端工程化</strong>
            </div>
            <div class="badge-card">
              <span class="badge-label">写作习惯</span>
              <strong class="badge-value">每周技术复盘</strong>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="highlights reveal reveal-3">
      <article v-for="item in highlights" :key="item.title" class="highlight-card">
        <p class="highlight-title">{{ item.title }}</p>
        <h3 class="highlight-value">{{ item.value }}</h3>
        <p class="highlight-desc">{{ item.desc }}</p>
      </article>
    </section>

    <section id="skills" class="section-block reveal reveal-2">
      <header class="section-head">
        <p class="section-kicker">Skills</p>
        <h2 class="section-title">技术栈与能力地图</h2>
      </header>
      <div class="skill-list">
        <article v-for="item in skills" :key="item.name" class="skill-item">
          <div class="skill-top">
            <span class="skill-name">{{ item.name }}</span>
            <span class="skill-level">{{ item.level }}%</span>
          </div>
          <div class="skill-track">
            <span class="skill-fill" :style="{ width: `${item.level}%` }"></span>
          </div>
        </article>
      </div>
    </section>

    <section id="timeline" class="section-block reveal reveal-3">
      <header class="section-head">
        <p class="section-kicker">Journey</p>
        <h2 class="section-title">成长时间线</h2>
      </header>
      <div class="timeline-list">
        <article v-for="item in timeline" :key="item.time + item.title" class="timeline-item">
          <div class="timeline-dot"></div>
          <div class="timeline-main">
            <time class="timeline-time">{{ item.time }}</time>
            <h3 class="timeline-title">{{ item.title }}</h3>
            <p class="timeline-desc">{{ item.desc }}</p>
          </div>
        </article>
      </div>
    </section>

    <section class="section-block reveal reveal-3">
      <header class="section-head">
        <p class="section-kicker">Projects</p>
        <h2 class="section-title">代表项目</h2>
      </header>
      <div class="project-grid">
        <article v-for="item in projects" :key="item.name" class="project-card">
          <h3 class="project-name">{{ item.name }}</h3>
          <p class="project-desc">{{ item.desc }}</p>
          <div class="project-tags">
            <span v-for="tag in item.tags" :key="tag" class="project-tag">{{ tag }}</span>
          </div>
        </article>
      </div>
    </section>

    <section id="contact" class="section-block reveal reveal-2">
      <header class="section-head">
        <p class="section-kicker">Contact</p>
        <h2 class="section-title">联系与协作</h2>
      </header>
      <div class="contact-box">
        <p>欢迎交流技术实现、架构优化与产品体验设计，也接受中小型项目协作。</p>
        <div class="contact-chips">
          <span class="contact-chip">Email: yuntan.dev@example.com</span>
          <span class="contact-chip">GitHub: github.com/yuntan-dev</span>
          <span class="contact-chip">WeChat: yuntan-tech</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts" name="AboutAuthorView">
import BackHomeButton from '@/components/common/BackHomeButton.vue'

const highlights = [
  { title: '开发年限', value: '5+ 年', desc: '覆盖 Web 前后端与管理系统开发' },
  { title: '项目经验', value: '30+ 项', desc: '含博客社区、内容平台与运营后台' },
  { title: '技术文章', value: '200+ 篇', desc: '持续输出工程化与性能优化实践' },
]

const skills = [
  { name: 'Vue / TypeScript', level: 92 },
  { name: 'Spring Boot / Java', level: 88 },
  { name: 'Node.js / API Design', level: 83 },
  { name: 'MySQL / Redis', level: 86 },
  { name: 'UI/UX 与交互实现', level: 84 },
]

const timeline = [
  { time: '2020', title: '从前端切入工程化实践', desc: '开始系统化沉淀组件规范、状态管理与构建链路。' },
  { time: '2021', title: '转向全栈开发', desc: '负责多个业务模块从接口到页面的完整交付。' },
  { time: '2022', title: '聚焦性能与稳定性', desc: '在生产项目中优化渲染性能、网络层容错与监控链路。' },
  { time: '2023', title: '搭建内容平台', desc: '主导博客社区核心功能与后台管理系统设计。' },
  { time: '2024-Now', title: '持续打磨产品体验', desc: '结合真实反馈迭代交互细节，提升可用性与维护性。' },
]

const projects = [
  {
    name: '云坛社区平台',
    desc: '包含文章、评论、标签、通知、用户系统与管理后台的完整社区方案。',
    tags: ['Vue 3', 'Element Plus', 'Spring Boot', 'MySQL'],
  },
  {
    name: '内容管理后台',
    desc: '支持内容审核、数据统计、建站历程管理与多角色权限协作。',
    tags: ['Dashboard', 'ECharts', 'RBAC', 'API Integration'],
  },
  {
    name: '前端性能优化体系',
    desc: '围绕加载、渲染、交互响应建立可量化的优化闭环。',
    tags: ['Code Split', 'Lazy Load', 'Observer API', 'Monitoring'],
  },
]
</script>

<style scoped>
.about-author-page {
  min-height: 100vh;
  padding: 108px 24px 72px;
  color: #14313a;
  background:
    radial-gradient(circle at 8% 10%, rgba(34, 197, 180, 0.22), transparent 30%),
    radial-gradient(circle at 90% 12%, rgba(14, 165, 233, 0.16), transparent 26%),
    linear-gradient(180deg, #f8fcff 0%, #ecfbf8 46%, #f5faff 100%);
}

.dark .about-author-page {
  color: #ebf1f4;
  background:
    radial-gradient(circle at 10% 8%, rgba(220, 38, 38, 0.18), transparent 28%),
    radial-gradient(circle at 88% 16%, rgba(244, 63, 94, 0.14), transparent 24%),
    linear-gradient(180deg, #090909 0%, #101010 50%, #0b0b0b 100%);
}

.author-hero {
  max-width: 1260px;
  margin: 0 auto;
}

.hero-shell {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 32px;
  align-items: center;
}

.hero-kicker {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  color: rgba(20, 49, 58, 0.58);
}

.dark .hero-kicker {
  color: rgba(226, 232, 240, 0.68);
}

.hero-title {
  margin: 10px 0 0;
  font-size: clamp(36px, 6vw, 64px);
  line-height: 1.02;
  font-weight: 900;
  letter-spacing: -0.03em;
}

.hero-desc {
  margin: 18px 0 0;
  max-width: 700px;
  font-size: clamp(16px, 2vw, 21px);
  line-height: 1.78;
  color: rgba(20, 49, 58, 0.78);
}

.dark .hero-desc {
  color: rgba(226, 232, 240, 0.82);
}

.hero-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  padding: 0 16px;
  border-radius: 999px;
  border: 1px solid rgba(20, 49, 58, 0.18);
  color: inherit;
  text-decoration: none;
  background: rgba(255, 255, 255, 0.62);
  transition: transform 0.24s ease, box-shadow 0.24s ease, background-color 0.24s ease;
}

.hero-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.1);
}

.hero-btn--solid {
  border-color: transparent;
  color: #f8fffe;
  background: linear-gradient(130deg, #0f766e, #0ea5a6);
}

.dark .hero-btn {
  border-color: rgba(248, 113, 113, 0.22);
  background: rgba(19, 19, 19, 0.86);
}

.dark .hero-btn--solid {
  background: linear-gradient(130deg, #991b1b, #be123c);
}

.hero-right {
  display: grid;
  gap: 16px;
}

.avatar-shell {
  position: relative;
  width: min(300px, 70vw);
  aspect-ratio: 1;
  margin: 0 auto;
}

.avatar-ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: conic-gradient(from 90deg, rgba(15, 118, 110, 0.12), rgba(14, 165, 233, 0.5), rgba(34, 197, 180, 0.12));
  animation: spin 11s linear infinite;
}

.dark .avatar-ring {
  background: conic-gradient(from 90deg, rgba(185, 28, 28, 0.14), rgba(244, 63, 94, 0.52), rgba(220, 38, 38, 0.14));
}

.avatar-core {
  position: absolute;
  inset: 12%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: clamp(42px, 9vw, 84px);
  font-weight: 900;
  color: #0f3f49;
  background: linear-gradient(160deg, #ffffff, #def5f2);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.9), 0 18px 38px rgba(14, 116, 144, 0.22);
}

.dark .avatar-core {
  color: #fce7eb;
  background: linear-gradient(160deg, #161616, #1d1d1d);
  box-shadow: inset 0 1px 0 rgba(248, 113, 113, 0.16), 0 18px 38px rgba(0, 0, 0, 0.5);
}

.hero-badges {
  display: grid;
  gap: 10px;
}

.badge-card {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid rgba(20, 49, 58, 0.1);
  background: rgba(255, 255, 255, 0.72);
}

.dark .badge-card {
  border-color: rgba(248, 113, 113, 0.18);
  background: rgba(16, 16, 16, 0.88);
}

.badge-label {
  font-size: 12px;
  color: rgba(20, 49, 58, 0.6);
}

.dark .badge-label {
  color: rgba(226, 232, 240, 0.68);
}

.badge-value {
  font-size: 18px;
  font-weight: 800;
}

.highlights,
.section-block {
  max-width: 1260px;
  margin: 28px auto 0;
}

.highlights {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.highlight-card {
  border-radius: 18px;
  padding: 18px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(20, 49, 58, 0.08);
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.06);
}

.dark .highlight-card {
  background: rgba(16, 16, 16, 0.9);
  border-color: rgba(248, 113, 113, 0.16);
  box-shadow: 0 14px 30px rgba(0, 0, 0, 0.4);
}

.highlight-title {
  margin: 0;
  font-size: 13px;
  color: rgba(20, 49, 58, 0.62);
}

.highlight-value {
  margin: 10px 0 0;
  font-size: 38px;
  line-height: 1;
}

.highlight-desc {
  margin: 10px 0 0;
  color: rgba(20, 49, 58, 0.72);
}

.dark .highlight-desc {
  color: rgba(226, 232, 240, 0.76);
}

.section-block {
  border-radius: 22px;
  padding: 22px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(20, 49, 58, 0.08);
}

.dark .section-block {
  background: rgba(15, 15, 15, 0.92);
  border-color: rgba(248, 113, 113, 0.16);
}

.section-head {
  margin-bottom: 16px;
}

.section-kicker {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: rgba(20, 49, 58, 0.56);
}

.section-title {
  margin: 8px 0 0;
  font-size: clamp(24px, 4vw, 36px);
  line-height: 1.1;
}

.skill-list {
  display: grid;
  gap: 14px;
}

.skill-item {
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(236, 253, 250, 0.76);
}

.dark .skill-item {
  background: rgba(32, 12, 16, 0.5);
}

.skill-top {
  display: flex;
  justify-content: space-between;
  font-weight: 700;
}

.skill-track {
  margin-top: 10px;
  height: 8px;
  border-radius: 999px;
  background: rgba(20, 49, 58, 0.12);
  overflow: hidden;
}

.dark .skill-track {
  background: rgba(255, 255, 255, 0.14);
}

.skill-fill {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #0f766e, #06b6d4);
}

.dark .skill-fill {
  background: linear-gradient(90deg, #b91c1c, #e11d48);
}

.timeline-list {
  position: relative;
  display: grid;
  gap: 16px;
  padding-left: 10px;
}

.timeline-list::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 4px;
  bottom: 4px;
  width: 2px;
  background: linear-gradient(180deg, rgba(13, 148, 136, 0.42), rgba(59, 130, 246, 0.12));
}

.dark .timeline-list::before {
  background: linear-gradient(180deg, rgba(239, 68, 68, 0.5), rgba(244, 63, 94, 0.14));
}

.timeline-item {
  position: relative;
  padding-left: 18px;
}

.timeline-dot {
  position: absolute;
  left: -4px;
  top: 8px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #0f766e;
  box-shadow: 0 0 0 4px rgba(15, 118, 110, 0.2);
}

.dark .timeline-dot {
  background: #dc2626;
  box-shadow: 0 0 0 4px rgba(220, 38, 38, 0.2);
}

.timeline-time {
  font-size: 12px;
  letter-spacing: 0.1em;
  color: rgba(20, 49, 58, 0.58);
}

.timeline-title {
  margin: 4px 0 0;
  font-size: 20px;
}

.timeline-desc {
  margin: 8px 0 0;
  color: rgba(20, 49, 58, 0.72);
}

.dark .timeline-desc {
  color: rgba(226, 232, 240, 0.78);
}

.project-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.project-card {
  border-radius: 14px;
  padding: 14px;
  border: 1px solid rgba(20, 49, 58, 0.1);
  background: rgba(255, 255, 255, 0.76);
}

.dark .project-card {
  border-color: rgba(248, 113, 113, 0.16);
  background: rgba(20, 20, 20, 0.9);
}

.project-name {
  margin: 0;
  font-size: 20px;
}

.project-desc {
  margin: 8px 0 0;
  color: rgba(20, 49, 58, 0.72);
}

.dark .project-desc {
  color: rgba(226, 232, 240, 0.76);
}

.project-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.project-tag {
  height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  background: rgba(13, 148, 136, 0.12);
  color: #0f766e;
  font-size: 12px;
  font-weight: 700;
}

.dark .project-tag {
  background: rgba(220, 38, 38, 0.18);
  color: #fda4af;
}

.contact-box {
  border-radius: 14px;
  padding: 16px;
  background: rgba(236, 253, 250, 0.78);
}

.dark .contact-box {
  background: rgba(32, 12, 16, 0.56);
}

.contact-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}

.contact-chip {
  border-radius: 999px;
  height: 30px;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(20, 49, 58, 0.1);
}

.dark .contact-chip {
  background: rgba(15, 15, 15, 0.9);
  border-color: rgba(248, 113, 113, 0.2);
}

.reveal {
  opacity: 0;
  transform: translateY(18px);
  animation: riseIn 0.65s ease forwards;
}

.reveal-1 { animation-delay: 0.04s; }
.reveal-2 { animation-delay: 0.12s; }
.reveal-3 { animation-delay: 0.22s; }

@keyframes riseIn {
  from {
    opacity: 0;
    transform: translateY(18px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@media (max-width: 1024px) {
  .hero-shell,
  .project-grid,
  .highlights {
    grid-template-columns: 1fr;
  }

  .about-author-page {
    padding-top: 96px;
  }
}
</style>
