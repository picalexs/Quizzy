# Step 1: Build the React app
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Step 2: Serve the React app using a simple HTTP server (for example, `serve`)
FROM node:22-alpine
WORKDIR /app
COPY --from=build /app/build /app/build
RUN npm install -g serve

# Serve the app on port 80
CMD ["serve", "-s", "build", "-l", "80"]
