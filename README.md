# Morse Translator

A full-stack Morse code translator built with Angular 20 and Spring Boot 4. It supports accounts, per-user translation history, text and `.txt` file translation, and a Morse learning guide.

## Run in development

Install Java 17 or newer, Maven 3.6.3 or newer, and a supported Node.js version (20.19+, 22.12+, or 24.x).

Open two PowerShell windows in this project folder.

**Terminal 1 — Spring Boot API and database:**

```powershell
mvn spring-boot:run
```

**Terminal 2 — Angular app:**

```powershell
cd frontend
npm install
npm start
```

Open [http://localhost:4200](http://localhost:4200). Create an account on the welcome screen; translations will be saved to that account's history.

## Build a single Spring Boot app

From the project folder, build Angular and copy its browser files into Spring Boot's static resources, then package and run the jar:

```powershell
cd frontend
npm install
npm run build
cd ..
New-Item -ItemType Directory -Force -Path src\main\resources\static | Out-Null
Copy-Item frontend\dist\morse-translator\browser\* src\main\resources\static\ -Recurse -Force
mvn package
java -jar target\morse-translator-2.0.0.jar
```

Then open [http://localhost:8080](http://localhost:8080).

## Features

- Register, sign in, and sign out with username/password accounts. Passwords are stored as BCrypt hashes.
- Translate text to International Morse or decode Morse back to text.
- Upload a plain text file up to 50 KB, translate it, and download the `.morse.txt` or `.decoded.txt` result.
- Review the latest 50 translations for the signed-in account, restore an earlier translation, delete individual items, or clear that account's history.
- Learn Morse with a short practice guide and an A–Z reference.
- Persistent H2 database files are created under `data/`. Each history query and deletion is scoped to the authenticated user's account.

## Project structure

- `src/main/java/` — Spring Boot REST API, account security, Morse codec, and history persistence.
- `src/main/resources/application.yml` — local H2 database and session configuration.
- `frontend/` — Angular app and development proxy to Spring Boot.
