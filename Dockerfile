# --- ステージ1: ビルド ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
COPY . .
# テストをスキップしてjarを作成
RUN mvn clean package -DskipTests

# --- ステージ2: 実行 ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# ビルドステージから生成されたjarファイルをコピー
COPY --from=build /target/*.jar app.jar

# ポート番号（Spring Bootのデフォルトは8080）
EXPOSE 8080

# アプリケーションの起動
ENTRYPOINT ["java", "-jar", "app.jar"]