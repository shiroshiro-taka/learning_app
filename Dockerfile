# --- ステージ1: ビルド ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
# キャッシュを効率的に使うため、依存関係のみ先に解決
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# --- ステージ2: 実行 ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 修正ポイント: 
# 1. ワイルドカードを避け、生成されるファイル名を特定する
# 2. または、コピー先をファイル名ではなくディレクトリ (./) にする
COPY --from=build /build/target/app-name-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]