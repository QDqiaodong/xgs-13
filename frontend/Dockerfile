ARG DOCKER_REGISTRY=docker.1ms.run
FROM ${DOCKER_REGISTRY}/library/node:18-alpine AS deps
WORKDIR /app
COPY package*.json ./
RUN --mount=type=cache,target=/root/.npm npm config set registry https://registry.npmmirror.com && npm install

FROM ${DOCKER_REGISTRY}/library/node:18-alpine AS build
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY package*.json ./
COPY index.html ./
COPY src ./src
COPY vite.config.js ./
COPY nginx.conf ./
RUN npm run build

FROM ${DOCKER_REGISTRY}/library/nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
