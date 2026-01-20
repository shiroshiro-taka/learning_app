Markdown			
# Java OCJP Silver 学習支援システム			
Java資格（OCJP Silver）の合格をサポートするための、フルスタック学習プラットフォームです。			
問題演習、模擬試験、苦手分析、管理者による教材管理まで一貫した機能を提供します。			
## 🚀 主な機能			
### 👤 ユーザー機能			
- **分野別・ランダム演習**: 重点学習や全範囲の網羅が可能です。			
- **模擬試験エンジン**: リアルタイム解答保存（Ajax）とタイマー機能を備えた本番形式の試験。			
- **苦手問題分析**: 正答率60%以下の問題を自動抽出し、効率的な復習を支援。			
- **学習ダッシュボード**: 進捗率と正答率を視覚的にグラフ化（成績管理）。			
### 🔑 管理者機能			
- **教材管理 (CRUD)**: 分野、問題、選択肢、模擬試験構成の完全な管理。			
- **ユーザー・問い合わせ管理**: 利用状況の把握とユーザーサポート。			
## 🛠 技術スタック			
- **Backend**: Java 17 / Spring Boot 3 / Spring Security / Spring Data JPA			
- **Frontend**: Thymeleaf / Tailwind CSS / JavaScript (jQuery)			
- **Database**: MySQL (Production), H2 (Development)			
- **Tooling**: highlight.js (コード強調), MathJax (数式表示), Docker			
- **Deployment**: Render			
## 📸 スクリーンショット			
(ここにホーム画面やダッシュボードの画像を貼る)			
## 📦 セットアップ・起動			
1. リポジトリをクローン			
2. MySQLでデータベースを作成			
3. `src/main/resources/application.properties` を環境に合わせて修正			
4. `./mvnw spring-boot:run` で起動			
