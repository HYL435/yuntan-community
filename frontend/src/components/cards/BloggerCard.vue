<template>
  <div class="card">
    <button class="mail">
      <svg
        class="lucide lucide-mail"
        stroke-linejoin="round"
        stroke-linecap="round"
        stroke-width="2"
        stroke="currentColor"
        fill="none"
        viewBox="0 0 24 24"
        height="24"
        width="24"
        xmlns="http://www.w3.org/2000/svg"
      >
        <rect rx="2" y="4" x="2" height="16" width="20"></rect>
        <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"></path>
      </svg>
    </button>
    
    <!-- 中国传统文化主题插图：简约河图洛书星图（线条为主） -->
    <div class="profile-pic">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 280 280" aria-label="河图洛书线条星图">
        <defs>
          <radialGradient id="bgGrad" cx="50%" cy="50%" r="55%">
            <stop offset="0%"   stop-color="#2a1a0a"/>
            <stop offset="60%"  stop-color="#1a0f05"/>
            <stop offset="100%" stop-color="#0d0804"/>
          </radialGradient>
          <linearGradient id="goldGrad" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%"   stop-color="#f5d47a"/>
            <stop offset="50%"  stop-color="#c9922a"/>
            <stop offset="100%" stop-color="#f5d47a"/>
          </linearGradient>
          <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
            <feGaussianBlur stdDeviation="2.5" result="blur"/>
            <feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge>
          </filter>
        </defs>

        <circle cx="140" cy="140" r="140" fill="url(#bgGrad)"/>

        <!-- 外层星图环线 -->
        <circle cx="140" cy="140" r="114" fill="none" stroke="url(#goldGrad)" stroke-width="0.8" opacity="0.35"/>
        <circle cx="140" cy="140" r="86" fill="none" stroke="url(#goldGrad)" stroke-width="0.9" opacity="0.42"/>

        <!-- 河图：五方点位 -->
        <g fill="#f5d47a" opacity="0.68" filter="url(#glow)">
          <circle cx="140" cy="40" r="3"/>
          <circle cx="140" cy="248" r="3"/>
          <circle cx="40" cy="140" r="3"/>
          <circle cx="248" cy="140" r="3"/>
          <circle cx="140" cy="140" r="3.2"/>
        </g>

        <!-- 河图骨架线（十字与斜向） -->
        <g stroke="url(#goldGrad)" stroke-width="0.9" opacity="0.34">
          <line x1="140" y1="48" x2="140" y2="236"/>
          <line x1="48" y1="140" x2="232" y2="140"/>
          <line x1="76" y1="76" x2="204" y2="204"/>
          <line x1="204" y1="76" x2="76" y2="204"/>
        </g>

        <!-- 洛书：分散九宫星点（4-9-2 / 3-5-7 / 8-1-6） -->
        <g fill="#f5d47a" opacity="0.58">
          <!-- 上排 4 9 2 -->
          <circle cx="98" cy="96" r="2"/><circle cx="104" cy="102" r="2"/><circle cx="92" cy="102" r="2"/><circle cx="98" cy="108" r="2"/>
          <circle cx="140" cy="86" r="2"/><circle cx="134" cy="92" r="2"/><circle cx="146" cy="92" r="2"/><circle cx="128" cy="98" r="2"/><circle cx="140" cy="98" r="2"/><circle cx="152" cy="98" r="2"/><circle cx="134" cy="104" r="2"/><circle cx="146" cy="104" r="2"/><circle cx="140" cy="110" r="2"/>
          <circle cx="182" cy="96" r="2"/><circle cx="176" cy="102" r="2"/>

          <!-- 中排 3 5 7 -->
          <circle cx="110" cy="138" r="2"/><circle cx="100" cy="144" r="2"/><circle cx="110" cy="150" r="2"/>
          <circle cx="140" cy="136" r="2"/><circle cx="134" cy="142" r="2"/><circle cx="146" cy="142" r="2"/><circle cx="136" cy="148" r="2"/><circle cx="144" cy="148" r="2"/>
          <circle cx="170" cy="136" r="2"/><circle cx="176" cy="136" r="2"/><circle cx="182" cy="136" r="2"/><circle cx="168" cy="142" r="2"/><circle cx="174" cy="142" r="2"/><circle cx="180" cy="142" r="2"/><circle cx="174" cy="148" r="2"/>

          <!-- 下排 8 1 6 -->
          <circle cx="98" cy="178" r="2"/><circle cx="104" cy="178" r="2"/><circle cx="110" cy="178" r="2"/><circle cx="92" cy="184" r="2"/><circle cx="98" cy="184" r="2"/><circle cx="104" cy="184" r="2"/><circle cx="110" cy="184" r="2"/><circle cx="98" cy="190" r="2"/>
          <circle cx="140" cy="182" r="2"/>
          <circle cx="176" cy="178" r="2"/><circle cx="182" cy="178" r="2"/><circle cx="170" cy="184" r="2"/><circle cx="176" cy="184" r="2"/><circle cx="182" cy="184" r="2"/><circle cx="176" cy="190" r="2"/>
        </g>

        <!-- 星图连线（简约分散） -->
        <g stroke="url(#goldGrad)" stroke-width="0.7" opacity="0.28">
          <line x1="98" y1="96" x2="140" y2="86"/>
          <line x1="140" y1="86" x2="182" y2="96"/>
          <line x1="110" y1="150" x2="140" y2="136"/>
          <line x1="140" y1="136" x2="174" y2="148"/>
          <line x1="98" y1="190" x2="140" y2="182"/>
          <line x1="140" y1="182" x2="176" y2="190"/>
          <line x1="40" y1="140" x2="98" y2="144"/>
          <line x1="248" y1="140" x2="182" y2="136"/>
          <line x1="140" y1="40" x2="140" y2="86"/>
          <line x1="140" y1="248" x2="140" y2="182"/>
        </g>

        <!-- 轻量传统装饰：上下两笔云纹 -->
        <g fill="none" stroke="url(#goldGrad)" stroke-width="1" opacity="0.35">
          <path d="M84,62 C92,56 104,56 111,62 C116,67 116,74 109,78"/>
          <path d="M196,218 C188,224 176,224 169,218 C164,213 164,206 171,202"/>
        </g>
      </svg>
    </div>

    <div class="bottom">
      <div class="content">
        <span class="name">苜 玥</span>
        <span class="about-me">
          一个热爱编程和分享的博客作者，喜欢探索技术的边界，记录成长的点滴。
        </span>
      </div>
      <div class="bottom-bottom">
        <div class="social-links-container">
          <!-- CSDN 图标 -->
          <a href="https://blog.csdn.net/2504_93832854?spm=1000.2115.3001.5343" target="_blank" rel="noopener noreferrer" title="CSDN">
            <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="20" height="20">
              <path d="M512 0C229.248 0 0 229.248 0 512s229.248 512 512 512 512-229.248 512-512S794.752 0 512 0z m243.2 554.4c0 148-100.8 226.4-232 226.4H364c-132.8 0-216-96-216-232V475.2c0-136 83.2-232 216-232h159.2c131.2 0 232 78.4 232 226.4v28.8H659.2v-32.8c0-54.4-29.6-97.6-95.2-97.6h-136c-67.2 0-96 48-96 107.2v79.2c0 59.2 28.8 107.2 96 107.2h136c65.6 0 95.2-43.2 95.2-97.6v-32.8h96v32.8z"></path>
            </svg>
          </a>
          <!-- 稀土掘金 图标 -->
          <a href="https://juejin.cn/user/3775076277187753" target="_blank" rel="noopener noreferrer" title="稀土掘金">
            <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="20" height="20">
              <path d="M512.5 87L285.7 266.3l226.8 215.2 225.3-215.2zM231.5 360.7L26.3 568.5l486.2 368.5 486.2-368.5-205.2-207.8-281 268.4z"></path>
            </svg>
          </a>
          <!-- Gitee/码云 图标 -->
          <a href="https://gitee.com/dq_hlu/yuntan-blog" target="_blank" rel="noopener noreferrer" title="Gitee">
            <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="20" height="20">
              <path d="M512 1024C230.4 1024 0 793.6 0 512S230.4 0 512 0s512 229.222 512 512-229.222 512-512 512z m259.2-569.6H480c-12.8 0-25.6 12.8-25.6 25.6v64c0 12.8 12.8 25.6 25.6 25.6h176c12.8 0 25.6 12.8 25.6 25.6v12.8c0 41.6-35.2 76.8-76.8 76.8h-240c-12.8 0-25.6-12.8-25.6-25.6V416c0-41.6 35.2-76.8 76.8-76.8h355.2c12.8 0 25.6-12.8 25.6-25.6v-64c0-12.8-12.8-25.6-25.6-25.6H416c-105.6 0-192 86.4-192 192v256c0 105.6 86.4 192 192 192h188.8c105.6 0 192-86.4 192-192V480c0-12.8-12.8-25.6-25.6-25.6z"></path>
            </svg>
          </a>
          <!-- GitHub 图标 -->
          <a href="https://github.com/HYL435/yuntan-community" target="_blank" rel="noopener noreferrer" title="GitHub">
            <svg viewBox="0 0 496 512" xmlns="http://www.w3.org/2000/svg">
              <path
                d="M165.9 397.4c0 2-2.3 3.6-5.2 3.6-3.3.3-5.6-1.3-5.6-3.6 0-2 2.3-3.6 5.2-3.6 3-.3 5.6 1.3 5.6 3.6zm-31.1-4.5c-.7 2 1.3 4.3 4.3 4.9 2.6 1 5.6 0 6.2-2s-1.3-4.3-4.3-5.2c-2.6-.7-5.5.3-6.2 2.3zm44.2-1.7c-2.9.7-4.9 2.6-4.6 4.9.3 2 2.9 3.3 5.9 2.6 2.9-.7 4.9-2.6 4.6-4.6-.3-1.9-3-3.2-5.9-2.9zM244.8 8C106.1 8 0 113.3 0 252c0 110.9 69.8 205.8 169.5 239.2 12.8 2.3 17.3-5.6 17.3-12.1 0-6.2-.3-40.4-.3-61.4 0 0-70 15-84.7-29.8 0 0-11.4-29.1-27.8-36.6 0 0-22.9-15.7 1.6-15.4 0 0 24.9 2 38.6 25.8 21.9 38.6 58.6 27.5 72.9 20.9 2.3-16 8.8-27.1 16-33.7-55.9-6.2-112.3-14.3-112.3-110.5 0-27.5 7.6-41.3 23.6-58.9-2.6-6.5-11.1-33.3 2.6-67.9 20.9-6.5 69 27 69 27 20-5.6 41.5-8.5 62.8-8.5s42.8 2.9 62.8 8.5c0 0 48.1-33.6 69-27 13.7 34.7 5.2 61.4 2.6 67.9 16 17.7 25.8 31.5 25.8 58.9 0 96.5-58.9 104.2-114.8 110.5 9.2 7.9 17 22.9 17 46.4 0 33.7-.3 75.4-.3 83.6 0 6.5 4.6 14.4 17.3 12.1C428.2 457.8 496 362.9 496 252 496 113.3 383.5 8 244.8 8zM97.2 352.9c-1.3 1-1 3.3.7 5.2 1.6 1.6 3.9 2.3 5.2 1 1.3-1 1-3.3-.7-5.2-1.6-1.6-3.9-2.3-5.2-1zm-10.8-8.1c-.7 1.3.3 2.9 2.3 3.9 1.6 1 3.6.7 4.3-.7.7-1.3-.3-2.9-2.3-3.9-2-.6-3.6-.3-4.3.7zm32.4 35.6c-1.6 1.3-1 4.3 1.3 6.2 2.3 2.3 5.2 2.6 6.5 1 1.3-1.3.7-4.3-1.3-6.2-2.2-2.3-5.2-2.6-6.5-1zm-11.4-14.7c-1.6 1-1.6 3.6 0 5.9 1.6 2.3 4.3 3.3 5.6 2.3 1.6-1.3 1.6-3.9 0-6.2-1.4-2.3-4-3.3-5.6-2z"
              ></path>
            </svg>
          </a>
        </div>
        <button class="button">联系我</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="BloggerCard">
</script>

<style scoped>
.card {
  width: 280px;
  height: 280px;
  background: #F0F7FF;
  border-radius: 32px;
  padding: 3px;
  position: relative;
  box-shadow: rgba(33, 150, 243, 0.15) 0px 70px 30px -50px;
  transition: all 0.5s ease-in-out;
}

:global(html.dark) .card {
  background: #1E1E1E;
  box-shadow: rgba(0, 0, 0, 0.3) 0px 70px 30px -50px;
}

.card .mail {
  position: absolute;
  right: 2rem;
  top: 1.4rem;
  background: transparent;
  border: none;
}

.card .mail svg {
  stroke: #2196F3;
  stroke-width: 3px;
}

.card .mail svg:hover {
  stroke: #2196F3;
}

:global(html.dark) .card .mail svg,
:global(html.dark) .card .mail svg:hover {
  stroke: #FFFFFF;
}

/* 修改了这里：增加了默认背景色 */
.card .profile-pic {
  position: absolute;
  width: calc(100% - 6px);
  height: calc(100% - 6px);
  top: 3px;
  left: 3px;
  border-radius: 29px;
  z-index: 1;
  border: 0px solid #2196F3;
  overflow: hidden;
  transition: all 0.5s ease-in-out 0.2s, z-index 0.5s ease-in-out 0.2s;
  
  /* 深色仿古底色，配合线条阴阳与河图洛书图案 */
  background: radial-gradient(ellipse at 50% 50%, #2a1a0a 0%, #0d0804 100%);
}

/* 适配暗黑模式的头像背景 */
:global(html.dark) .card .profile-pic {
  background: radial-gradient(ellipse at 50% 50%, #2a1a0a 0%, #0d0804 100%);
}

.card .profile-pic img {
  -o-object-fit: cover;
  object-fit: cover;
  width: 100%;
  height: 100%;
  -o-object-position: 0px 0px;
  object-position: 0px 0px;
  transition: all 0.5s ease-in-out 0s;
}

.card .profile-pic svg {
  width: 100%;
  height: 100%;
  -o-object-fit: cover;
  object-fit: cover;
  -o-object-position: 0px 0px;
  object-position: 0px 0px;
  transform-origin: 45% 20%;
  transition: all 0.5s ease-in-out 0s;
}

.card .bottom {
  position: absolute;
  bottom: 3px;
  left: 3px;
  right: 3px;
  background: #F0F7FF;
  top: 80%;
  border-radius: 29px;
  z-index: 2;
  box-shadow: rgba(96, 75, 74, 0.1882352941) 0px 5px 5px 0px inset;
  overflow: hidden;
  transition: all 0.5s cubic-bezier(0.645, 0.045, 0.355, 1) 0s;
}

.card .bottom .content {
  position: absolute;
  bottom: 0;
  left: 1.5rem;
  right: 1.5rem;
  height: 160px;
}

.card .bottom .content .name {
  display: block;
  font-size: 1.2rem;
  color: #1E293B;
  font-weight: bold;
}

.card .bottom .content .about-me {
  display: block;
  font-size: 0.9rem;
  color: #64748B;
  margin-top: 1rem;
}

.card .bottom .bottom-bottom {
  position: absolute;
  bottom: 1rem;
  left: 1.5rem;
  right: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card .bottom .bottom-bottom .social-links-container {
  display: flex;
  gap: 1rem;
}

.card .bottom .bottom-bottom .social-links-container svg {
  height: 20px;
  fill: #2196F3;
  filter: drop-shadow(0 5px 5px rgba(165, 132, 130, 0.1333333333));
}

.card .bottom .bottom-bottom .social-links-container svg:hover {
  fill: #2196F3;
  transform: scale(1.2);
}

.card .bottom .bottom-bottom .button {
  background: #2196F3;
  color: #FFFFFF;
  border: none;
  border-radius: 20px;
  font-size: 0.6rem;
  padding: 0.4rem 0.6rem;
  box-shadow: rgba(165, 132, 130, 0.1333333333) 0px 5px 5px 0px;
}

.card .bottom .bottom-bottom .button:hover {
  background: #2196F3;
  color: #FFFFFF;
}

:global(html.dark) .card .bottom {
  background: #1E1E1E;
}

:global(html.dark) .card .bottom .content .name {
  color: #FFFFFF;
}

:global(html.dark) .card .bottom .content .about-me {
  color: #D1D5DB;
}

:global(html.dark) .card .bottom .bottom-bottom .social-links-container svg,
:global(html.dark) .card .bottom .bottom-bottom .social-links-container svg:hover {
  fill: #FFFFFF;
}

:global(html.dark) .card .bottom .bottom-bottom .button,
:global(html.dark) .card .bottom .bottom-bottom .button:hover {
  background: #FFFFFF;
  color: #1E1E1E;
}

.card:hover {
  border-top-left-radius: 55px;
}

.card:hover .bottom {
  top: 20%;
  border-radius: 80px 29px 29px 29px;
  transition: all 0.5s cubic-bezier(0.645, 0.045, 0.355, 1) 0.2s;
}

.card:hover .profile-pic {
  width: 100px;
  height: 100px;
  aspect-ratio: 1;
  top: 10px;
  left: 10px;
  border-radius: 50%;
  z-index: 3;
  border: 7px solid #2196F3;
  box-shadow: rgba(96, 75, 74, 0.1882352941) 0px 5px 5px 0px;
  transition: all 0.5s ease-in-out, z-index 0.5s ease-in-out 0.1s;
}

:global(html.dark) .card:hover .profile-pic {
  border: 7px solid #FFFFFF;
}

.card:hover .profile-pic:hover {
  transform: scale(1.3);
  border-radius: 0px;
}

.card:hover .profile-pic img {
  transform: scale(2.5);
  -o-object-position: 0px 25px;
  object-position: 0px 25px;
  transition: all 0.5s ease-in-out 0.5s;
}

.card:hover .profile-pic svg {
  transform: scale(1.15);
  transition: all 0.5s ease-in-out 0.5s;
}

</style>

<style>
/* 确保特定选择器优先级 */
html.dark .card {
  background: #1E1E1E;
  box-shadow: rgba(0, 0, 0, 0.3) 0px 70px 30px -50px;
}

html.dark .card .mail svg,
html.dark .card .mail svg:hover {
  stroke: #FFFFFF;
}

html.dark .card .profile-pic {
  border: 2px solid #c9922a;
  /* 确保暗色模式下背景也不黑 */
  background: radial-gradient(ellipse at 50% 50%, #2a1a0a 0%, #0d0804 100%);
}

html.dark .card:hover .profile-pic {
  border: 7px solid #FFFFFF;
}

html.dark .card .bottom {
  background: #1E1E1E;
}

html.dark .card .bottom .content .name {
  color: #FFFFFF;
}

html.dark .card .bottom .content .about-me {
  color: #D1D5DB;
}

html.dark .card .bottom .bottom-bottom .social-links-container svg,
html.dark .card .bottom .bottom-bottom .social-links-container svg:hover {
  fill: #FFFFFF;
}

html.dark .card .bottom .bottom-bottom .button,
html.dark .card .bottom .bottom-bottom .button:hover {
  background: #FFFFFF;
  color: #1E1E1E;
}
</style>