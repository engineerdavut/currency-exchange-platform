FROM node:18-alpine AS base

# Install dependencies
FROM base AS deps
WORKDIR /app
COPY package.json package-lock.json* ./
# Dockerfile'ınızda şu satırı ekleyin (RUN npm ci satırından önce)
RUN npm config set registry https://registry.npmjs.org/

RUN npm ci

# Build the app
FROM base AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
ARG NEXT_PUBLIC_API_URL
# ARG NEXT_PUBLIC_ACCOUNT_AUTH_SERVICE_URL
# ARG NEXT_PUBLIC_ACCOUNT_BASE_URL
# ARG NEXT_PUBLIC_EXCHANGE_SERVICE_URL
# ARG NEXT_PUBLIC_TEST_URL

ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
# ENV NEXT_PUBLIC_ACCOUNT_BASE_URL=$NEXT_PUBLIC_ACCOUNT_BASE_URL
# ENV NEXT_PUBLIC_EXCHANGE_SERVICE_URL=$NEXT_PUBLIC_EXCHANGE_SERVICE_URL
# ENV NEXT_PUBLIC_TEST_URL=$NEXT_PUBLIC_TEST_URL
RUN npm run build

# Production image
FROM base AS runner
WORKDIR /app
ENV NODE_ENV production
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
EXPOSE 3000
CMD ["node", "server.js"]
