import http from './http';

export interface CategoryFrontVO {
  id: number | string;
  categoryName: string;
}

export interface CategoryArticleVO {
  id: number | string;
  title: string;
  summary: string;
  coverImg: string;
  category: string;
  tags: string[];
  publishTime: string | number[];
  likeCount: number;
  commentCount: number;
  collectCount: number;
  viewCount: number;
}

export interface CategoryArticlePageResult {
  records: CategoryArticleVO[];
  total: number;
}

// 获取所有分类，返回 CategoryFrontVO 数组
export async function getCategories(): Promise<CategoryFrontVO[]> {
  console.log('准备请求分类接口 /front/categories');
  return http.get('/front/categories').then(res => {
    console.log('分类接口原始响应:', res);
    if (Array.isArray(res.data)) return res.data;
    if (res.data && Array.isArray(res.data.data)) return res.data.data;
    return [];
  });
}

const normalizeTags = (raw: any): string[] => {
  if (Array.isArray(raw)) {
    return raw
      .map((item) => {
        if (typeof item === 'string') return item;
        return item?.tagName || item?.name || '';
      })
      .filter(Boolean);
  }
  if (typeof raw === 'string') {
    return raw
      .split(',')
      .map((s) => s.trim())
      .filter(Boolean);
  }
  return [];
};

const normalizeArticle = (raw: any): CategoryArticleVO => ({
  id: raw?.id ?? raw?.articleId ?? '',
  title: raw?.title ?? raw?.articleTitle ?? '',
  summary: raw?.summary ?? raw?.description ?? raw?.intro ?? '',
  coverImg: raw?.coverImg ?? raw?.cover ?? raw?.coverUrl ?? '',
  category: raw?.category ?? raw?.categoryName ?? '',
  tags: normalizeTags(raw?.tags || raw?.tagNames || raw?.tagList || raw?.tagName),
  publishTime: raw?.publishTime ?? raw?.createTime ?? raw?.createdAt ?? '',
  likeCount: Number(raw?.likeCount ?? raw?.likes ?? 0),
  commentCount: Number(raw?.commentCount ?? raw?.comments ?? 0),
  collectCount: Number(raw?.collectCount ?? raw?.bookmarks ?? 0),
  viewCount: Number(raw?.viewCount ?? raw?.views ?? raw?.heat ?? 0),
});

const extractRecords = (res: any): any[] => {
  const payload = res?.data?.data ?? res?.data ?? res;
  if (Array.isArray(payload?.records)) return payload.records;
  if (Array.isArray(payload?.list)) return payload.list;
  if (Array.isArray(payload)) return payload;
  return [];
};

const extractTotal = (res: any, fallbackLen: number): number => {
  const payload = res?.data?.data ?? res?.data ?? res;
  const totalRaw = payload?.total ?? payload?.totalCount;
  const total = Number(totalRaw);
  return Number.isFinite(total) && total >= 0 ? total : fallbackLen;
};

// 根据分类分页获取文章列表
export async function getArticlesByCategory(
  categoryId: number | string,
  pageNo = 1,
  pageSize = 8
): Promise<CategoryArticlePageResult> {
  const catId = String(categoryId);
  const res = await http.get('/front/articles/page/categoryOrTags', {
    params: {
      pageNo,
      pageSize,
      categoryId: catId,
    },
  });

  const recordsRaw = extractRecords(res);
  const records = recordsRaw.map(normalizeArticle);
  const total = extractTotal(res, records.length);
  return { records, total };
}
