<script setup lang="ts" name="LoginView">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import BackHomeButton from '@/components/common/BackHomeButton.vue';
import { useNotification } from '@/composables/useNotification';
import { validateLoginUser } from '@/utils/validators';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const { success, error, warning } = useNotification();
const AUTH_EXPIRED_NOTICE_KEY = 'auth_expired_notice';
const REMEMBER_LOGIN_KEY = 'remember_login_credentials';
const LAST_VISITED_ROUTE_KEY = 'last_visited_route';

interface RememberLoginData {
  account: string;
  password: string;
  rememberMe: boolean;
}

const username = ref('');
const password = ref('');
const rememberMe = ref(false);
const passwordVisible = ref(false);
const errorMessage = ref('');
const isLoading = ref(false);

const getPostLoginTarget = () => {
  const redirect = route.query.redirect;
  if (typeof redirect === 'string' && redirect.startsWith('/') && redirect !== '/login') {
    return redirect;
  }

  const remembered = localStorage.getItem(LAST_VISITED_ROUTE_KEY) || '/';
  if (remembered.startsWith('/') && remembered !== '/login' && remembered !== '/register' && remembered !== '/network-error') {
    return remembered;
  }

  return '/';
};

const toFriendlyLoginError = (rawMessage: string) => {
  const normalized = rawMessage?.trim();
  if (!normalized) {
    return '登录失败，请稍后重试';
  }

  const statusMatch = normalized.match(/status code\s*(\d{3})/i);
  if (statusMatch) {
    const statusCode = Number(statusMatch[1]);
    if (statusCode === 502 || statusCode === 503) {
      return '服务器暂时不可用，请稍后再试';
    }
    if (statusCode === 500) {
      return '服务器开小差了，请稍后再试';
    }
    if (statusCode === 401) {
      return '账号或密码错误，请重新输入';
    }
    if (statusCode === 429) {
      return '请求过于频繁，请稍后再试';
    }
    return '登录失败，请稍后重试';
  }

  const lower = normalized.toLowerCase();
  if (lower.includes('network error')) {
    return '网络连接异常，请检查网络后重试';
  }

  return normalized;
};

const loadRememberedLogin = () => {
  const raw = localStorage.getItem(REMEMBER_LOGIN_KEY);
  if (!raw) {
    return;
  }

  try {
    const saved: RememberLoginData = JSON.parse(raw);
    if (saved.rememberMe && saved.account) {
      username.value = saved.account;
      password.value = saved.password ?? '';
      rememberMe.value = true;
    }
  } catch {
    localStorage.removeItem(REMEMBER_LOGIN_KEY);
  }
};

const persistRememberedLogin = (account: string, plainPassword: string) => {
  if (!rememberMe.value) {
    localStorage.removeItem(REMEMBER_LOGIN_KEY);
    return;
  }

  const payload: RememberLoginData = {
    account,
    password: plainPassword,
    rememberMe: true,
  };
  localStorage.setItem(REMEMBER_LOGIN_KEY, JSON.stringify(payload));
};

const handleSubmit = async () => {
  const account = username.value.trim();
  const isEmail = account.includes('@');
  const result = validateLoginUser({
    username: isEmail ? undefined : account,
    email: isEmail ? account : undefined,
    password: password.value
  });

  if (!result.valid) {
    errorMessage.value = result.message ?? '登录信息不合法。';
    return;
  }

  errorMessage.value = '';
  isLoading.value = true;

  try {
    // 调用登录 API
    const loginResult = await userStore.login({
      username: isEmail ? undefined : account,
      email: isEmail ? account : undefined,
      password: password.value
    });

    if (loginResult.success) {
      persistRememberedLogin(account, password.value);
      success('登录成功');
      const targetPath = getPostLoginTarget();
      localStorage.setItem(LAST_VISITED_ROUTE_KEY, targetPath);
      // 延迟跳转，让用户看到成功提示
      setTimeout(() => {
        router.push(targetPath);
      }, 500);
    } else {
      const friendlyMessage = toFriendlyLoginError(loginResult.message);
      errorMessage.value = friendlyMessage;
      error(friendlyMessage);
    }
  } catch (errorObj) {
    const rawMessage = errorObj instanceof Error ? errorObj.message : '登录失败，请重试';
    const friendlyMessage = toFriendlyLoginError(rawMessage);
    errorMessage.value = friendlyMessage;
    error(friendlyMessage);
  } finally {
    isLoading.value = false;
  }
};

const togglePasswordVisibility = () => {
  passwordVisible.value = !passwordVisible.value;
};

// 简单的深色模式同步逻辑
const syncDarkMode = () => {
  // 这里不需要额外的响应式变量控制样式，完全交给CSS变量处理
};

let observer: MutationObserver | null = null;
onMounted(() => {
  loadRememberedLogin();

  const needExpiredNotice = localStorage.getItem(AUTH_EXPIRED_NOTICE_KEY) === '1';
  if (needExpiredNotice) {
    localStorage.removeItem(AUTH_EXPIRED_NOTICE_KEY);
    warning('登录已过期，请重新登录');
  }

  syncDarkMode();
  observer = new MutationObserver(syncDarkMode);
  observer.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] });
});

onUnmounted(() => {
  observer?.disconnect();
});
</script>

<template>
  <div class="login-page">
    <BackHomeButton />
    <div class="auth-wrap">
      <h1 class="page-title">欢迎登录</h1>
      <form class="form" @submit.prevent="handleSubmit">
      
      <!-- 账号输入 -->
      <div class="flex-column">
        <label>账号</label>
      </div>
      <div class="inputForm">
        <!-- SVG 使用 fill="currentColor" 跟随文字颜色 -->
        <svg height="20" viewBox="0 0 32 32" width="20" xmlns="http://www.w3.org/2000/svg" style="fill: currentColor">
          <g id="Layer_3" data-name="Layer 3"><path d="m30.853 13.87a15 15 0 0 0 -29.729 4.082 15.1 15.1 0 0 0 12.876 12.918 15.6 15.6 0 0 0 2.016.13 14.85 14.85 0 0 0 7.715-2.145 1 1 0 1 0 -1.031-1.711 13.007 13.007 0 1 1 5.458-6.529 2.149 2.149 0 0 1 -4.158-.759v-10.856a1 1 0 0 0 -2 0v1.726a8 8 0 1 0 .2 10.325 4.135 4.135 0 0 0 7.83.274 15.2 15.2 0 0 0 .823-7.455zm-14.853 8.13a6 6 0 1 1 6-6 6.006 6.006 0 0 1 -6 6z"></path></g>
        </svg>
        <input v-model.trim="username" type="text" name="username" class="input" placeholder="请输入邮箱或用户名">
      </div>
      
      <!-- 密码输入 -->
      <div class="flex-column">
        <label>密码</label>
      </div>
      <div class="inputForm">
        <svg height="20" viewBox="-64 0 512 512" width="20" xmlns="http://www.w3.org/2000/svg" style="fill: currentColor">
          <path d="m336 512h-288c-26.453125 0-48-21.523438-48-48v-224c0-26.476562 21.546875-48 48-48h288c26.453125 0 48 21.523438 48 48v224c0 26.476562-21.546875 48-48 48zm-288-288c-8.8125 0-16 7.167969-16 16v224c0 8.832031 7.1875 16 16 16h288c8.8125 0 16-7.167969 16-16v-224c0-8.832031-7.1875-16-16-16zm0 0"></path><path d="m304 224c-8.832031 0-16-7.167969-16-16v-80c0-52.929688-43.070312-96-96-96s-96 43.070312-96 96v80c0 8.832031-7.167969 16-16 16s-16-7.167969-16-16v-80c0-70.59375 57.40625-128 128-128s128 57.40625 128 128v80c0 8.832031-7.167969 16-16 16zm0 0"></path>
        </svg>        
        <input v-model.trim="password" name="password" :type="passwordVisible ? 'text' : 'password'" class="input" placeholder="请输入密码">
        
        <button
          type="button"
          class="toggle-password"
          :class="{ 'is-active': passwordVisible }"
          @click="togglePasswordVisibility"
        >
          <svg viewBox="0 0 576 512" height="1em" xmlns="http://www.w3.org/2000/svg">
            <path d="M288 32c-80.8 0-145.5 36.8-192.6 80.6C48.6 156 17.3 208 2.5 243.7c-3.3 7.9-3.3 16.7 0 24.6C17.3 304 48.6 356 95.4 399.4C142.5 443.2 207.2 480 288 480s145.5-36.8 192.6-80.6c46.8-43.5 78.1-95.4 93-131.1c3.3-7.9 3.3-16.7 0-24.6c-14.9-35.7-46.2-87.7-93-131.1C433.5 68.8 368.8 32 288 32zM144 256a144 144 0 1 1 288 0 144 144 0 1 1 -288 0zm144-64c0 35.3-28.7 64-64 64c-7.1 0-13.9-1.2-20.3-3.3c-5.5-1.8-11.9 1.6-11.7 7.4c.3 6.9 1.3 13.8 3.2 20.7c13.7 51.2 66.4 81.6 117.6 67.9s81.6-66.4 67.9-117.6c-11.1-41.5-47.8-69.4-88.6-71.1c-5.8-.2-9.2 6.1-7.4 11.7c2.1 6.4 3.3 13.2 3.3 20.3z"></path>
          </svg>
        </button>
      </div>
      
      <!-- 底部选项 -->
      <div class="flex-row">
        <div class="checkbox-wrap">
          <label class="check-container">
            <input v-model="rememberMe" type="checkbox">
            <svg viewBox="0 0 64 64" height="2em" width="2em">
              <path d="M 0 16 V 56 A 8 8 90 0 0 8 64 H 56 A 8 8 90 0 0 64 56 V 8 A 8 8 90 0 0 56 0 H 8 A 8 8 90 0 0 0 8 V 16 L 32 48 L 64 16 V 8 A 8 8 90 0 0 56 0 H 8 A 8 8 90 0 0 0 8 V 56 A 8 8 90 0 0 8 64 H 56 A 8 8 90 0 0 64 56 V 16" pathLength="575.0541381835938" class="path"></path>
            </svg>
          </label>
          <span class="checkbox-text">记住我</span>
        </div>
        <RouterLink to="/forgot-password" class="span text-blue-500 hover:underline">忘记密码？</RouterLink>
      </div>
      
      <button class="button-submit" type="submit" :disabled="isLoading">{{ isLoading ? '登录中...' : '登录' }}</button>
      <p v-if="errorMessage" class="register-text auth-error">{{ errorMessage }}</p>
      <p class="register-text">还没有账号？<RouterLink class="register-link" to="/register">立即注册</RouterLink></p>
      </form>
    </div>
  </div>
</template>

<style scoped src="./login-shared.css"></style>