# ---------- Stage 1: Copy static assets ----------
FROM node:20-alpine AS build

WORKDIR /app

COPY . .

# Placeholder for a real build step if the frontend
# grows into a framework-based application later.


# ---------- Stage 2: Serve with nginx ----------
FROM nginx:1.27-alpine AS runtime

COPY --from=build /app/index.html /usr/share/nginx/html/index.html
COPY --from=build /app/register.html /usr/share/nginx/html/register.html
COPY --from=build /app/dashboard.html /usr/share/nginx/html/dashboard.html
COPY --from=build /app/css /usr/share/nginx/html/css
COPY --from=build /app/js /usr/share/nginx/html/js

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget -q -O- http://localhost:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
