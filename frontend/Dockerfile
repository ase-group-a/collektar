# ---- build stage ----
FROM node:20 AS builder
WORKDIR /app

RUN corepack enable && corepack prepare pnpm@latest --activate

COPY . .
ENV CI=true

RUN pnpm install --frozen-lockfile
RUN pnpm build

# ---- production stage ----
FROM node:20 AS runner
WORKDIR /app
ENV NODE_ENV=production

RUN corepack enable && corepack prepare pnpm@latest --activate

COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/package.json ./package.json
COPY --from=builder /app/next.config.ts ./next.config.ts
COPY --from=builder /app/node_modules ./node_modules

EXPOSE 3000
CMD ["pnpm", "start"]