## Project Overview

This is a Spring Boot project for "AjouEvent" (https://ajouevent.com).
It is a web application that provides customized push notifications for Ajou University announcements. Users can subscribe to specific categories (e.g., general, scholarship, department, dormitory) and keywords. Core features include subscription-based notification routing, event banners, weekly trending posts, bookmarking, and Google Calendar integration.

## Important Note for AI Agent

As the AI Agent, you **must not** perform any actions or make any changes to the codebase that have not been explicitly instructed by the user. Adhere strictly to the user's commands and avoid proactive modifications or additions.

## General Rules

- Before deleting files, always get developer approval.
- Do not touch `git push`
- **YAML**: Do **not** add quotes (`"` or `'`) around values. Write plain unquoted values (e.g., `url: jdbc:mysql://...` not `url: "jdbc:mysql://..."`). Spotless enforces `MINIMIZE_QUOTES: true` and `WRITE_DOC_START_MARKER: false`.
- **JSON**: Use 2-space indentation and keep keys sorted alphabetically. Spotless enforces this via `gson().indentWithSpaces(2).sortByKeys()`.
- **Java**: Remove unused imports, trim trailing whitespace, and end files with a newline. Spotless enforces this automatically.
- **Markdown / misc files** (`.properties`, `.xml`, `.sql`, `.sh`): Trim trailing whitespace and end with a newline.

## V1 Reference Code

- V1 코드는 `chore/ajou-event-v1` 브랜치의 `ajou-event-v1/` 디렉토리에 위치함
- V2 작업 시 V1 구현을 참고할 때 해당 브랜치를 확인할 것
