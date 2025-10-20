# Stage 1: Build the frontend
FROM node:20-alpine AS build

# Set working directory
WORKDIR /app

# Copy package.json and package-lock.json
COPY package.json package-lock.json ./

# Install dependencies
RUN npm ci

# Copy the rest of the frontend code
COPY tsconfig.json webpack.config.js jest.config.js jest.setup.tsx ./
COPY src/main/webapp ./src/main/webapp

# Build the frontend
RUN npm run build

# Stage 2: Serve the frontend
FROM nginx:alpine

# Copy the built frontend from the build stage
COPY --from=build /app/target/classes/static /usr/share/nginx/html

# Expose port 3000
EXPOSE 3000

# Configure nginx to listen on port 3000
RUN echo 'server { \
    listen 3000; \
    location / { \
        root /usr/share/nginx/html; \
        try_files $uri $uri/ /index.html; \
    } \
}' > /etc/nginx/conf.d/default.conf

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -q -O - http://localhost:3000 || exit 1