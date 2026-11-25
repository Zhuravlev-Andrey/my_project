# Memory Bank

Краткая операционная память проекта для AI-агентов. Не заменяет OpenSpec.

## Источники правды

- Поведение системы — `openspec/specs/` (см. `openspec/specs/<cap>/spec.md`).
- Работа в процессе — `openspec/changes/`.
- Схема OpenSpec — `spec-driven`: proposal → specs → design → tasks.

## Файлы

- `roadmap.md` — этапы жизненного цикла, таблица статусов и правила перехода.
- `projectbrief.md` — цель, границы, ключевые требования.
- `productContext.md` — зачем продукт, пользователи, UX-цели.
- `techContext.md` — стек, команды, ограничения.
- `systemPatterns.md` — архитектура, паттерны, ADR.
- `activeContext.md` — только текущее состояние (перезаписывается).
- `progress.md` — сводка «работает / осталось / проблемы».

## Правила

1. Начинай сессию с `activeContext.md` → `progress.md` → `projectbrief.md` → `roadmap.md`.
2. Работай строго в рамках текущего этапа по Exit-критерию; статусы обновляй только в
   таблице `roadmap.md` и в `activeContext.md`.
3. Перед proposal/design/tasks сверяйся с `systemPatterns.md` и `techContext.md`.
4. После change и перед `openspec archive` обнови `activeContext.md` и `progress.md`.
5. По команде «update memory bank» перечитай и актуализируй все файлы.
6. Архитектурные решения фиксируй в `systemPatterns.md` (что, почему, дата).
7. Не храни здесь секреты, токены и персональные данные.
