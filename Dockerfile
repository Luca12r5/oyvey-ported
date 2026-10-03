# LEGO server: backend API + website in one container.
#   docker build -t lego-server .
#   docker run -p 8787:8787 -v lego-data:/data -e LEGO_PUBLIC_ORIGIN=https://deine-domain.de lego-server
# Data (SQLite database, uploads) lives in /data – mount a volume there.
FROM node:22-slim
WORKDIR /app
ENV NODE_ENV=production \
    LEGO_HOST=0.0.0.0 \
    LEGO_PORT=8787 \
    LEGO_DATA_DIR=/data \
    LEGO_TRUST_PROXY=1 \
    LEGO_SECURE_COOKIES=1
# No npm install needed: the backend only uses Node built-ins and the shared package.
COPY packages/shared/package.json packages/shared/package.json
COPY packages/shared/src packages/shared/src
COPY apps/backend/package.json apps/backend/package.json
COPY apps/backend/src apps/backend/src
COPY apps/website/public apps/website/public
RUN mkdir -p node_modules/@lego /data \
 && ln -s ../../packages/shared node_modules/@lego/shared \
 && chown -R node:node /data
USER node
VOLUME ["/data"]
EXPOSE 8787
HEALTHCHECK --interval=30s --timeout=5s CMD node -e "fetch('http://127.0.0.1:8787/api/public/status').then(r=>process.exit(r.ok?0:1)).catch(()=>process.exit(1))"
CMD ["node", "--disable-warning=ExperimentalWarning", "apps/backend/src/server.ts"]
