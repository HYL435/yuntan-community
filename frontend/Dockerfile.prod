FROM node:20-alpine AS builder

WORKDIR /app

COPY frontend/package.json frontend/pnpm-lock.yaml ./
RUN corepack enable && pnpm install --frozen-lockfile

COPY frontend/index.html ./
COPY frontend/public ./public
COPY frontend/src ./src
COPY frontend/vite.config.* ./
COPY frontend/tsconfig*.json ./

RUN pnpm build

FROM nginx:stable-alpine

RUN rm -f /etc/nginx/conf.d/default.conf
COPY deploy/nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]