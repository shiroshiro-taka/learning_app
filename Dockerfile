# --- ステージ1: ビルド ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /build

# 先にpom.xmlをコピーして依存関係をキャッシュ（ビルド高速化）
COPY pom.xml .
RUN mvn dependency:go-offline

# ソースコードをコピー（srcディレクトリを指定）
COPY src ./src

# テストをスキップしてjarを作成
RUN mvn clean package -DskipTests

# --- ステージ2: 実行 ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# ステージ1のWORKDIR (/build) からjarをコピー
COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]