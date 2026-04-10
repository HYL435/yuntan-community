<script setup lang="ts" name="Header">
import { ref, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import OptionButton from "@/components/common/OptionButton.vue";
import DarkButton from "@/components/common/DarkButton.vue";
import UserProfile from "@/components/common/UserProfile.vue";
import BackTopButton from "@/components/common/BackTopButton.vue";
import { getCategories } from "@/api/category";
import http from "@/api/http";

const showTransparentHeader = ref(true);
const showStickyHeader = ref(false);
const isMenuOpen = ref(false);
const showAboutAuthorEntry = ref(false);
const router = useRouter();

const checkIsAdmin = () => {
  const raw = localStorage.getItem('auth_token') || ''
  if (!raw) return false
  try {
    const token = raw.replace(/^Bearer\s+/i, '')
    const parts = token.split('.')
    if (parts.length < 2) return false
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4)
    const payload = JSON.parse(atob(padded))

    let roleVal: any = null
    if (payload.role !== undefined) roleVal = payload.role
    else if (payload.roles !== undefined) roleVal = payload.roles
    else if (payload.authority !== undefined) roleVal = payload.authority
    else if (payload.authorities !== undefined) roleVal = payload.authorities

    if (Array.isArray(roleVal) && roleVal.length > 0) roleVal = roleVal[0]
    if (roleVal && typeof roleVal === 'object' && roleVal.role !== undefined) roleVal = roleVal.role

    const roleNum = Number(roleVal)
    return !Number.isNaN(roleNum) && (roleNum === 0 || roleNum === 1)
  } catch {
    return false
  }
}

const handleScroll = () => {
  const scrollTop = window.scrollY || document.documentElement.scrollTop;
  showTransparentHeader.value = scrollTop < 10;
  showStickyHeader.value = scrollTop > 170;
};

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value;
  document.body.style.overflow = isMenuOpen.value ? "hidden" : "";
};

const handleOptionClick = async (linkName: string, option: string) => {
  if (linkName === "文章") {
    if (!option) return;
    if (option === "首页") { router.push("/"); return; }
    if (option === "标签") { 
      try {
        const firstTag = await getFirstTag();
        if (firstTag) {
          router.push(`/tag/${encodeURIComponent(firstTag)}`);
        }
      } catch (error) {
        console.error('获取第一个标签失败:', error);
      }
      return; 
    }
    // 文章菜单中的分类项统一跳转到分类页
    router.push({
      path: '/categories',
      query: { name: option }
    })
    return;
  }
  if (linkName === "关于") {
    if (option === "关于本站") { router.push('/about/site'); return; }
    if (option === "关于博主") { router.push('/about/author'); return; }
    return;
  }
  if (linkName === "社交") {
    if (option === "留言板") { router.push("/message-board"); return; }
    if (option === "话题") { router.push('/topics'); return; }
    if (option === "友链") { router.push('/friend-links'); return; }
    return;
  }
  if (linkName === "更多") {
    if (option === "藏书阁") { router.push('/bookshelf'); return; }
    if (option === "工具箱") { router.push('/toolbox'); return; }
    return;
  }
};

// 获取第一个标签名称
const getFirstTag = async (): Promise<string | null> => {
  try {
    const res = await http.get('/front/tags');
    const tags = Array.isArray(res.data?.data) ? res.data.data : [];
    if (tags.length > 0 && tags[0].tagName) {
      return tags[0].tagName;
    }
    return null;
  } catch (error) {
    console.error('获取标签列表失败:', error);
    return null;
  }
};

const navLinks = ref([
  { name: "文章", href: "#super_container", options: ["首页"] },
  { name: "关于", href: "#about", options: ["关于本站", "关于博主"] },
  { name: "社交", href: "#contact", options: ["留言板", "话题"] },
  { name: "更多", href: "#more", options: ["藏书阁", "工具箱"] },
]);

// 从后端获取分类列表
const loadCategoriesIntoNav = async () => {
  try {
    const categories = await getCategories();
    if (Array.isArray(categories) && categories.length > 0) {
      const categoryNames = categories.map(cat => cat.categoryName);
      // 在首页后插入所有分类，最后是"标签"选项
      const articleNavIndex = navLinks.value.findIndex(link => link.name === "文章");
      if (articleNavIndex !== -1) {
        navLinks.value[articleNavIndex].options = ["首页", ...categoryNames, "标签"];
      }
    }
  } catch (error) {
    console.error('获取分类失败:', error);
  }
};

onMounted(() => {
  showAboutAuthorEntry.value = checkIsAdmin();
  handleScroll();
  window.addEventListener("scroll", handleScroll);
  loadCategoriesIntoNav();
});

onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll);
  document.body.style.overflow = "";
});
</script>

<template>
  <div>
    <!-- Header 1: 透明导航 -->
    <header
      class="header-slide fixed top-0 left-0 w-full z-30 bg-transparent py-6 transition-transform duration-1000 ease-[cubic-bezier(0.19,1,0.22,1)]"
      :class="showTransparentHeader ? 'translate-y-0' : '-translate-y-full'"
    >
      <div class="container mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between">
          <RouterLink to="/" class="flex items-center">
            <span class="brand-title text-5xl text-white">雲壇</span>
          </RouterLink>

          <div class="hidden md:flex items-center gap-8">
            <nav>
              <ul class="flex items-center gap-8">
                <li v-for="link in navLinks" :key="link.name" class="relative group h-full flex items-center">
                  <a :href="link.href" class="header-main-link header-main-link--transparent relative group/link text-lg font-medium transition-colors py-2">
                    {{ link.name }}
                    <span class="nav-underline absolute -bottom-1 left-0 h-0.5 bg-white"></span>
                  </a>
                  <div v-if="link.options" class="nav-dropdown-wrapper">
                    <div class="nav-dropdown-card">
                      <div class="card-content">
                         <template v-for="opt in link.options" :key="opt">
                           <OptionButton
                             v-if="!(link.name === '关于' && opt === '关于博主' && !showAboutAuthorEntry)"
                             @click="handleOptionClick(link.name, opt)"
                           >
                             {{ opt }}
                           </OptionButton>
                         </template>
                      </div>
                    </div>
                  </div>
                </li>
              </ul>
            </nav>
            <DarkButton />
            <UserProfile />
          </div>
          <button @click="toggleMenu" class="md:hidden text-white p-2 rounded-lg hover:bg-white/10 transition-colors" aria-label="打开菜单">
            <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
              <line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/>
            </svg>
          </button>
        </div>
      </div>
    </header>

    <!-- Header 2: 白色悬浮导航 -->
    <header
      class="header-slide fixed top-0 left-0 w-full z-40 bg-[#F7F9FE] shadow-md py-4 transition-transform duration-700 ease-in-out header-light"
      :class="showStickyHeader ? 'translate-y-0' : '-translate-y-full'"
    >
      <div class="container mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between">      
          <RouterLink to="/" class="flex items-center">
            <span class="brand-title text-5xl text-black">雲壇</span>
          </RouterLink>

          <div class="hidden md:flex items-center gap-8">
            <nav>
              <ul class="flex items-center gap-8">
                <li v-for="link in navLinks" :key="link.name" class="relative group h-full flex items-center">
                  <a :href="link.href" class="header-main-link header-main-link--light relative group/link text-lg font-medium transition-colors py-2">
                    {{ link.name }}
                    <span class="nav-underline absolute -bottom-1 left-0 h-0.5 bg-black"></span>
                  </a>
                  <div v-if="link.options" class="nav-dropdown-wrapper">
                    <div class="nav-dropdown-card">
                      <div class="card-content">
                         <OptionButton
                           v-for="opt in link.options"
                           :key="opt"
                           @click="handleOptionClick(link.name, opt)"
                         >
                           {{ opt }}
                         </OptionButton>
                      </div>
                    </div>
                  </div>
                </li>
              </ul>
            </nav>

            <BackTopButton />
            <UserProfile />
          </div>
          <button @click="toggleMenu" class="md:hidden text-gray-800 p-2 rounded-lg hover:bg-black/5 transition-colors" aria-label="打开菜单">
            <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
              <line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/>
            </svg>
          </button>
        </div>
      </div>
    </header>

    <!-- Mobile Menu -->
    <div
      class="fixed inset-0 bg-brand-black z-50 transition-transform duration-500 ease-[cubic-bezier(0.77,0,0.175,1)]"
      :class="isMenuOpen ? 'translate-y-0' : '-translate-y-full'"
    >
      <!-- 关闭按钮 -->
      <button @click="toggleMenu" class="absolute top-5 right-6 text-white p-2 rounded-lg hover:bg-white/10 transition-colors" aria-label="关闭菜单">
        <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>

      <!-- 可滚动内容区 -->
      <div class="w-full h-full flex flex-col overflow-y-auto pt-16 pb-8 px-8">
        <ul class="flex flex-col gap-2">
          <li v-for="link in navLinks" :key="link.name" class="border-b border-white/10 py-4">
            <div class="text-white/50 text-xs font-semibold uppercase tracking-widest mb-3">{{ link.name }}</div>
            <div class="flex flex-wrap gap-2">
              <template v-for="opt in link.options" :key="opt">
                <button
                  v-if="!(link.name === '关于' && opt === '关于博主' && !showAboutAuthorEntry)"
                  @click="handleOptionClick(link.name, opt); toggleMenu()"
                  class="px-4 py-2 text-base text-white rounded-lg hover:bg-white/15 transition-colors font-medium text-left"
                >
                  {{ opt }}
                </button>
              </template>
            </div>
          </li>
        </ul>
        <div class="mt-auto pt-8 flex items-center justify-center gap-6">
          <DarkButton />
          <UserProfile />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bg-brand-black { background-color: #1f1f1f; }

.header-slide {
  transition: transform 1s cubic-bezier(0.19,1,0.22,1), translate 1s cubic-bezier(0.19,1,0.22,1) !important;
  will-change: transform;
}

.header-main-link,
.header-main-link:visited {
  text-decoration: none;
}

.brand-title {
  font-family: 'STXingkai', 'FZShuTi', 'HanziPen SC', 'KaiTi', 'Kaiti SC', 'STKaiti', 'DFKai-SB', 'LiSu', cursive;
  font-weight: 600;
  letter-spacing: 0.34em;
  line-height: 1;
  text-rendering: optimizeLegibility;
  -webkit-font-smoothing: antialiased;
}

.header-main-link--transparent,
.header-main-link--transparent:visited {
  color: rgba(241, 245, 249, 0.95);
  text-shadow: 0 1px 14px rgba(15, 23, 42, 0.45);
}

.header-main-link--transparent:hover {
  color: #ffffff;
}

.header-main-link--light,
.header-main-link--light:visited {
  color: #0f172a;
}

.header-main-link--light:hover {
  color: #1d4ed8;
}

.nav-underline {
  width: 100%;
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s ease;
}

.group\/link:hover .nav-underline,
.group:hover .nav-underline {
  transform: scaleX(1);
}

/* 1. 下拉菜单定位桥梁 */
.nav-dropdown-wrapper {
  position: absolute;
  left: 50%;
  top: 100%;
  transform: translateX(-50%);
  padding-top: 15px;
  visibility: hidden;
  opacity: 0;
  transition: all 0.2s ease-in-out;
  z-index: 50;
  pointer-events: none;
}

.nav-dropdown-wrapper > * {
  pointer-events: auto;
}

.group:hover .nav-dropdown-wrapper {
  visibility: visible;
  opacity: 1;
}

/* 
  2. 卡片外壳 (默认浅色模式)
*/
.nav-dropdown-card {
  width: fit-content;
  max-width: 160px;
  min-width: 100px;
  background: #3b5998; /* 浅色模式下的蓝底 */
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  position: relative;
  /* 浅色模式下的青色阴影 */
  box-shadow: 0px 6px 26px rgba(6, 182, 212, 0.25), 0px 0px 0px 1px rgba(6, 182, 212, 0.3);
  border-radius: 8px;
  border: 1px solid rgba(6, 182, 212, 0.5); /* 浅色模式下的边框 */
  transition: background 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease;
}

/* 
  3. 内容层 (默认浅色模式)
*/
.card-content {
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(12px);
  z-index: 2;
  padding: 10px 5px;
  display: flex;
  flex-direction: column;
  gap: 5px;
  justify-content: center;
  align-items: center;
  width: calc(100% - 4px);
  height: calc(100% - 4px);
  margin: 2px;
  border-radius: 6px;
  position: relative;
  transition: background 0.3s ease;
}

/* Hover时的内部光晕 */
.card-content::before {
  opacity: 0;
  transition: opacity 300ms;
  content: " ";
  display: block;
  background: white;
  width: 5px;
  height: 50px;
  position: absolute;
  filter: blur(50px);
  overflow: hidden;
  pointer-events: none;
}
.nav-dropdown-card:hover .card-content::before {
  opacity: 1;
}

/* 
  4. 旋转的彩色长条 (默认浅色模式：亮青色+白色)
*/
.nav-dropdown-card::before {
  opacity: 0;
  content: " ";
  position: absolute;
  display: block;
  width: 170%;
  height: 170%;
  background: conic-gradient(
    from 0deg,
    transparent 0deg,
    rgba(6, 182, 212, 0.6) 90deg, /* 亮青色 */
    rgba(6, 182, 212, 1) 120deg, /* 纯青色 */
    rgba(255, 255, 255, 1) 150deg, /* 纯白高光 */
    rgba(6, 182, 212, 1) 180deg, /* 纯青色 */
    rgba(6, 182, 212, 0.6) 210deg,
    transparent 270deg,
    transparent 360deg
  );
  transition: opacity 300ms;
  animation: rotation_9018 3000ms infinite linear;
  animation-play-state: paused;
  z-index: 0;
}

/* 只要下拉菜单显示出来，就让它旋转 */
.nav-dropdown-card:hover::before, 
.group:hover .nav-dropdown-card::before {
  opacity: 1;
  animation-play-state: running;
}

/* 底部模糊层 */
.nav-dropdown-card::after {
  position: absolute;
  content: " ";
  display: block;
  width: 100%;
  height: 100%;
  background: #17171733;
  backdrop-filter: blur(50px);
  z-index: 0;
  pointer-events: none;
}

@keyframes rotation_9018 {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 深色模式样式通过全局 style.css 控制 */
</style>