import http from './http';

/**
 * 文章点赞（后端: POST /front/likes/{articleId}）
 */
export const likeArticleApi = (articleId: number | string) => {
  return http.post(`/front/likes/${articleId}`);
};

/**
 * 文章收藏（后端: POST /front/collects/{articleId}）
 */
export const collectArticleApi = (articleId: number | string) => {
  return http.post(`/front/collects/${articleId}`);
};